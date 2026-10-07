
package data_migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class LoadBulkRecordsFromJsonToMysql implements Nlp {

	static long BatchSize;
	int ThreadSize;
	int QUEUE_SIZE;

	static String DB_URL;
	static String DB_USER;
	static String DB_PASS;
	static String table;
	static String OUTPUT_DIR;

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final Map<String, TableConfig> TABLES = new LinkedHashMap<>();
	private static final Map<String, BlockingQueue<JsonNode>> QUEUES = new LinkedHashMap<>();

	private static final JsonNode POISON = new ObjectMapper().createObjectNode();

	private static final AtomicLong START = new AtomicLong();
	private static final AtomicLong END = new AtomicLong();

	// table -> PK
	private static final Map<String, String> tablePrimaryKeys = new LinkedHashMap<>();

	// table -> (column -> jsonPath)
	private static final Map<String, Map<String, String>> tableColumns = new LinkedHashMap<>();

	// childTable -> (parentTable -> parentPK)
	private static final Map<String, Map<String, String>> foreignKeys = new LinkedHashMap<>();

	// record-scope inferred relations
	private static final Map<String, String> recordScopeRelations = new LinkedHashMap<>();

	// table -> root json path
	private static final Map<String, String> tableJsonPaths = new LinkedHashMap<>();

	private static final Map<String, String> propertyFilePaths = new LinkedHashMap<>();

	// root field name
	private static String rootFieldName = null;

	@InputParams({ @InputParam(name = "Json File Path", type = "java.lang.String"),
			@InputParam(name = "Property Files Folder Path", type = "java.lang.String"),
			@InputParam(name = "Batch Size", type = "java.lang.String"),
			@InputParam(name = "Thread Size", type = "java.lang.String"),
			@InputParam(name = "Queue Size", type = "java.lang.String"),
			@InputParam(name = "Host", type = "java.lang.String"),
			@InputParam(name = "Port", type = "java.lang.String"),
			@InputParam(name = "Database", type = "java.lang.String"),
			@InputParam(name = "Username", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String")})
	@ReturnType(name = "Logs", type = "org.json.JSONObject")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		
		resetState();

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

		String jsonFilePath = (String) programElementsInput.get("Json File Path");
		OUTPUT_DIR = (String) programElementsInput.get("Property Files Folder Path");
		String host = (String) programElementsInput.get("Host");
		String port = (String) programElementsInput.get("Port");
		String database = (String) programElementsInput.get("Database");
		DB_USER = (String) programElementsInput.get("Username");
		DB_PASS = (String) programElementsInput.get("Password");
		BatchSize = Long.parseLong(programElementsInput.get("Batch Size").toString());
		QUEUE_SIZE = Integer.parseInt(programElementsInput.get("Queue Size").toString());
		ThreadSize = Integer.parseInt(programElementsInput.get("Thread Size").toString());
		
		DB_URL = "jdbc:mysql://" + host + ":" + port + "/" + database + "?rewriteBatchedStatements=true"
				+ "&useServerPrepStmts=false" + "&cachePrepStmts=true" + "&prepStmtCacheSize=250"
				+ "&prepStmtCacheSqlLimit=2048";

		JSONObject logs = new JSONObject();

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			// Your program element business logic starts here ...

			long totalStart = System.currentTimeMillis();
			File jsonFile = new File(jsonFilePath);
			JsonNode root = mapper.readTree(jsonFile);

			// detect root field
			if (root.isObject() && root.fieldNames().hasNext()) {
				rootFieldName = root.fieldNames().next();
			} else if (root.isArray()) {
				rootFieldName = "root";
			}

			if (isAnonymousRoot(root)) {
			}

			// start analysis from root field node
			JsonNode startNode = root.isObject() ? root.get(rootFieldName) : root;
			String startPath = rootFieldName;
			if (startNode.isArray()) {
				startPath += "[*]";
			}

			analyzeNode(startNode, startPath, null);
			applyRecordScopeFKs();
			writePropertyFiles();

			/* ---------- LOAD TABLE METADATA ---------- */
			for (String tableName : propertyFilePaths.keySet()) {
				String path = propertyFilePaths.get(tableName);
				loadTable(path);
			}

			/* ---------- ENSURE TABLES (DROP + CREATE) ---------- */
			try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
			    ensureTables(c);
			}

			/* ---------- START WORKERS ---------- */
			ExecutorService exec = Executors.newCachedThreadPool();
			CountDownLatch latch = new CountDownLatch(TABLES.size());

			for (TableConfig cfg : TABLES.values()) {

				BlockingQueue<JsonNode> q = new LinkedBlockingQueue<>(QUEUE_SIZE);
				QUEUES.put(cfg.tableName, q);

				
				for (int i = 0; i < ThreadSize; i++) {
				    exec.submit(new GenericWorker(cfg, q, latch));
				}

			}

			/* ---------- STREAM JSON ---------- */
			ObjectMapper mapper = new ObjectMapper();
			JsonFactory factory = mapper.getFactory();

			int dispatched = 0;

			try (JsonParser parser = factory.createParser(new File(jsonFilePath))) {

				parser.nextToken(); // START_OBJECT or START_ARRAY

				while (parser.nextToken() != JsonToken.END_OBJECT && parser.currentToken() != null) {

					// Skip until root field
					if (rootFieldName != null && parser.getCurrentName() != null
							&& !rootFieldName.equals(parser.getCurrentName()))
						continue;

					parser.nextToken(); // START_ARRAY

					while (parser.nextToken() == JsonToken.START_OBJECT) {

						JsonNode node = mapper.readTree(parser);

						// Dispatch to all tables dynamically
						dispatchAllTables(node);

						dispatched++;
					}
				}
			}

			/* ---------- POISON ---------- */
			for (BlockingQueue<JsonNode> q : QUEUES.values()) {
				for (int i = 0; i < ThreadSize; i++) {
					q.put(POISON);
				}
			}

			latch.await();
			END.set(System.nanoTime());
			exec.shutdown();

			double totalTime = (System.currentTimeMillis() - totalStart) / 1000.0;

			logs.put("Dispaatched Records", dispatched);
			logs.put("Total Time Taken (sec)", totalTime);

			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setMessage("ERROR: "+e);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("Logs", logs);
		return nlpResponseModel;
	}

	/* ---------------- ROOT CHECK ---------------- */

	private static boolean isAnonymousRoot(JsonNode root) {

		if (!root.isObject() && !root.isArray())
			return true;

		Iterator<JsonNode> it = root.elements();
		while (it.hasNext()) {
			JsonNode v = it.next();
			if (v.isObject() || v.isArray())
				return false;
		}
		return true;
	}

	/* ---------------- JSON ANALYSIS ---------------- */

	private static void analyzeNode(JsonNode node, String jsonPath, String parentTable) {

		if (node.isArray()) {
			for (JsonNode record : node) {
				detectRecordScopeRelations(record);
				analyzeNode(record, jsonPath, parentTable);
			}
			return;
		}

		if (!node.isObject())
			return;

		Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

		while (fields.hasNext()) {

			Map.Entry<String, JsonNode> entry = fields.next();
			String fieldName = entry.getKey();
			JsonNode value = entry.getValue();

			String currentPath = jsonPath == null ? fieldName : jsonPath + "." + fieldName;

			// Object table
			if (value.isObject()) {

				detectPrimaryKey(fieldName, value);
				detectStructuralFK(fieldName, parentTable);
				tableJsonPaths.putIfAbsent(fieldName, currentPath);

				analyzeNode(value, currentPath, fieldName);
			}

			// Array of objects table
			else if (value.isArray() && value.size() > 0 && value.get(0).isObject()) {

				detectPrimaryKey(fieldName, value.get(0));
				detectStructuralFK(fieldName, parentTable);
				tableJsonPaths.putIfAbsent(fieldName, currentPath + "[*]");

				analyzeNode(value, currentPath + "[*]", fieldName);
			}

			// Primitive column
			else if (parentTable != null && tablePrimaryKeys.containsKey(parentTable)) {

				tableColumns.putIfAbsent(parentTable, new LinkedHashMap<>());
				tableColumns.get(parentTable).putIfAbsent(fieldName, currentPath);
			}
		}
	}

	/* ---------------- PK DETECTION ---------------- */

	private static void detectPrimaryKey(String table, JsonNode node) {

		if (tablePrimaryKeys.containsKey(table))
			return;

		Iterator<String> fields = node.fieldNames();
		while (fields.hasNext()) {

			String field = fields.next();
			String f = field.toLowerCase();

			if (f.equals("id") || f.endsWith("id") || f.endsWith("no") || f.endsWith("num") || f.endsWith("code")) {

				tablePrimaryKeys.put(table, field);
				return;
			}
		}
	}

	/* ---------------- STRUCTURAL FK ---------------- */

	private static void detectStructuralFK(String child, String parent) {

		if (parent == null)
			return;
		if (!tablePrimaryKeys.containsKey(parent))
			return;

		foreignKeys.putIfAbsent(child, new LinkedHashMap<>());
		foreignKeys.get(child).put(parent, tablePrimaryKeys.get(parent));

		addFKColumn(child, parent);
	}

	/* ---------------- RECORD-SCOPE FK ---------------- */

	private static void detectRecordScopeRelations(JsonNode recordNode) {

		if (!recordNode.isObject())
			return;

		List<String> objectTables = new ArrayList<>();
		List<String> arrayTables = new ArrayList<>();

		recordNode.fields().forEachRemaining(e -> {

			if (e.getValue().isObject() && tablePrimaryKeys.containsKey(e.getKey())) {
				objectTables.add(e.getKey());
			}

			if (e.getValue().isArray() && e.getValue().size() > 0 && e.getValue().get(0).isObject()
					&& tablePrimaryKeys.containsKey(e.getKey())) {
				arrayTables.add(e.getKey());
			}
		});

		if (objectTables.size() == 1) {
			String parent = objectTables.get(0);
			for (String child : arrayTables) {
				recordScopeRelations.putIfAbsent(child, parent);
			}
		}
	}

	private static void applyRecordScopeFKs() {

		recordScopeRelations.forEach((child, parent) -> {

			if (foreignKeys.containsKey(child))
				return;

			String parentPK = tablePrimaryKeys.get(parent);
			if (parentPK == null)
				return;

			foreignKeys.putIfAbsent(child, new LinkedHashMap<>());
			foreignKeys.get(child).put(parent, parentPK);

			addFKColumn(child, parent);
		});
	}

	/* ---------------- FK COLUMN ---------------- */

	private static void addFKColumn(String child, String parent) {

		String parentPK = tablePrimaryKeys.get(parent);

		tableColumns.putIfAbsent(child, new LinkedHashMap<>());
		tableColumns.get(child).putIfAbsent(parentPK, parent + "." + parentPK);
	}

	/* ---------------- PROPERTY FILE GENERATION ---------------- */

	private static void writePropertyFiles() {

		File dir = new File(OUTPUT_DIR);
		if (!dir.exists())
			dir.mkdirs();

		for (String table : tablePrimaryKeys.keySet()) {

			Properties props = new Properties();

			String pk = tablePrimaryKeys.get(table);
			String jsonPath = tableJsonPaths.getOrDefault(table, table).replace(rootFieldName + "[*].", "")
					.replace(rootFieldName + ".", "");

			String parentTable = "";
			String parentPK = "";

			if (foreignKeys.containsKey(table)) {
				Map.Entry<String, String> fk = foreignKeys.get(table).entrySet().iterator().next();
				parentTable = fk.getKey();
				parentPK = fk.getValue();
			}

			props.setProperty("__table", table);
			props.setProperty("__pk", pk);
			props.setProperty("__jsonPath", jsonPath);
			props.setProperty("__parentTable", parentTable);
			props.setProperty("__parentKeyColumn", parentPK);

			Map<String, String> cols = tableColumns.getOrDefault(table, Map.of());

			for (String col : cols.keySet()) {
				props.setProperty(col, col);
			}

			File file = new File(dir, table + ".properties");

			try (FileOutputStream fos = new FileOutputStream(file)) {
				props.store(fos, "Auto-generated mapping for table: " + table);

				// Store in LinkedHashMap
				propertyFilePaths.put(table, file.getAbsolutePath());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/* ================= TABLE CONFIG ================= */

	static class TableConfig {
		String tableName;
		String primaryKey;
		String jsonPath;
		String parentTable;
		String parentKeyColumn;

		LinkedHashMap<String, String[]> columnMap;
		List<String> columns;
	}

	private static void loadTable(String filePath) throws Exception {

		Properties p = new Properties();
		try (FileInputStream fis = new FileInputStream(filePath)) {
			p.load(fis);
		}

		TableConfig cfg = new TableConfig();

		cfg.tableName = p.getProperty("__table");
		cfg.primaryKey = p.getProperty("__pk");
		cfg.jsonPath = p.getProperty("__jsonPath");
		cfg.parentTable = p.getProperty("__parentTable");
		cfg.parentKeyColumn = p.getProperty("__parentKeyColumn");

		LinkedHashMap<String, String[]> map = new LinkedHashMap<>();

		for (String key : p.stringPropertyNames()) {
			if (key.startsWith("__"))
				continue;

			String path = p.getProperty(key).replace("$.", "");
			map.put(key, path.contains(".") ? path.split("\\.") : new String[] { path });
		}

		cfg.columnMap = map;
		cfg.columns = new ArrayList<>();
		cfg.columns.add(cfg.primaryKey);
		for (String col : map.keySet()) {
			if (!col.equals(cfg.primaryKey)) {
				cfg.columns.add(col);
			}
		}

		TABLES.put(cfg.tableName, cfg);
	}

	/* ================= JSON DISPATCH ================= */

	private static void dispatchAllTables(JsonNode record) throws InterruptedException {

		Map<String, String> parentIds = new HashMap<>();

		for (TableConfig cfg : TABLES.values()) {

			List<JsonNode> nodes = resolvePath(record, cfg.jsonPath);

			for (JsonNode n : nodes) {

				ObjectNode row = n.deepCopy();

				// inject parent FK
				if (cfg.parentTable != null) {
					String parentId = parentIds.get(cfg.parentTable);
					if (parentId != null) {
						row.put(cfg.parentKeyColumn, parentId);
					}
				}

				QUEUES.get(cfg.tableName).put(row);

				// capture PK for children
				String pkVal = getValue(row, cfg.columnMap.get(cfg.primaryKey));
				if (pkVal != null) {
					parentIds.put(cfg.tableName, pkVal);
				}
			}
		}
	}

	/* ================= JSON PATH ENGINE ================= */

	private static List<JsonNode> resolvePath(JsonNode root, String path) {

		List<JsonNode> current = List.of(root);

		for (String token : path.split("\\.")) {

			List<JsonNode> next = new ArrayList<>();

			if (token.endsWith("[*]")) {
				String name = token.substring(0, token.length() - 3);

				for (JsonNode n : current) {
					JsonNode arr = n.path(name);
					if (arr.isArray())
						arr.forEach(next::add);
				}
			} else {
				for (JsonNode n : current) {
					JsonNode child = n.path(token);
					if (!child.isMissingNode()) {
						next.add(child);
					}
				}
			}
			current = next;
		}
		return current;
	}

	/* ================= WORKER ================= */

	static class GenericWorker implements Runnable {

		private final TableConfig cfg;
		private final BlockingQueue<JsonNode> queue;
		private final CountDownLatch latch;

		GenericWorker(TableConfig cfg, BlockingQueue<JsonNode> queue, CountDownLatch latch) {
			this.cfg = cfg;
			this.queue = queue;
			this.latch = latch;
		}

		public void run() {

			try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

				conn.setAutoCommit(false);
				PreparedStatement ps = conn
						.prepareStatement(buildInsertSQL(cfg.tableName, cfg.columns, cfg.primaryKey));

				int count = 0;

				while (true) {

					JsonNode node = queue.take();
					if (node == POISON)
						break;

					START.compareAndSet(0, System.nanoTime());

					String pkVal = getValue(node, cfg.columnMap.get(cfg.primaryKey));
					if (pkVal == null)
						continue;

					ps.setString(1, pkVal);

					for (int i = 1; i < cfg.columns.size(); i++) {
						String col = cfg.columns.get(i);
						ps.setString(i + 1, getValue(node, cfg.columnMap.get(col)));
					}

					ps.addBatch();

					if (++count % BatchSize == 0) {
						ps.executeBatch();
						conn.commit();
					}
				}

				ps.executeBatch();
				conn.commit();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		}
	}

	/* ================= SQL HELPERS ================= */

	private static void ensureTables(Connection conn) throws SQLException {

	    for (TableConfig cfg : TABLES.values()) {

	        if (tableExists(conn, cfg.tableName)) {
	            System.out.println("Dropping table: " + cfg.tableName);
	            dropTable(conn, cfg.tableName);
	        }

	        System.out.println("Creating table: " + cfg.tableName);
	        createTable(conn, cfg);
	    }
	}

	
	private static boolean tableExists(Connection conn, String table) throws SQLException {

	    ResultSet rs = conn.getMetaData().getTables(
	            conn.getCatalog(),
	            null,
	            table,
	            new String[]{"TABLE"}
	    );

	    return rs.next();
	}

	private static void dropTable(Connection conn, String table) throws SQLException {
	    conn.createStatement().execute("DROP TABLE IF EXISTS `" + table + "`");
	}

	private static void createTable(Connection conn, TableConfig cfg) throws SQLException {

	    StringBuilder sql = new StringBuilder();
	    sql.append("CREATE TABLE `").append(cfg.tableName).append("` (");

	    // PK
	    sql.append("`").append(cfg.primaryKey).append("` VARCHAR(255) NOT NULL,");

	    // Other columns
	    for (String col : cfg.columns) {
	        if (col.equals(cfg.primaryKey)) continue;
	        sql.append("`").append(col).append("` TEXT,");
	    }

	    // PRIMARY KEY
	    sql.append("PRIMARY KEY (`").append(cfg.primaryKey).append("`)");

	    sql.append(") ENGINE=InnoDB");

	    conn.createStatement().execute(sql.toString());
	}

	private static String buildInsertSQL(String table, List<String> cols, String pk) {

		StringJoiner c = new StringJoiner(",");
		StringJoiner v = new StringJoiner(",");
		StringJoiner u = new StringJoiner(",");

		for (String col : cols) {
			c.add("`" + col + "`");
			v.add("?");
			if (!pk.equals(col)) {
				u.add("`" + col + "`=VALUES(`" + col + "`)");
			}
		}

		return "INSERT INTO " + table + " (" + c + ") VALUES (" + v + ")" + " ON DUPLICATE KEY UPDATE " + u;
	}

	private static String getValue(JsonNode node, String[] tokens) {
		JsonNode cur = node;
		for (String t : tokens) {
			if (cur == null || cur.isNull())
				return null;
			cur = cur.path(t);
		}
		return cur.isMissingNode() || cur.isNull() ? null : cur.asText();
	}
	
	private static void resetState() {

	    TABLES.clear();
	    QUEUES.clear();

	    tablePrimaryKeys.clear();
	    tableColumns.clear();
	    foreignKeys.clear();
	    recordScopeRelations.clear();
	    tableJsonPaths.clear();
	    propertyFilePaths.clear();

	    START.set(0);
	    END.set(0);

	    rootFieldName = null;
	}


}