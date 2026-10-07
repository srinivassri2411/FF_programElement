
package data_migration;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONObject;

public class ValidateJsonWithMysqlDb implements Nlp {

	/* ================= DB CONFIG ================= */
	static String DB_URL;
	static String DB_USER;
	static String DB_PASS;
	static String htmlReportPath;
	static JSONObject logs;

	private static final Map<String, TableConfig> TABLES = new LinkedHashMap<>();
	static int THREADS_PER_TABLE;
	static int BATCH_SIZE;

	/* ================= VALIDATION METRICS ================= */
	private static AtomicLong TOTAL = new AtomicLong(0);
	private static AtomicLong MATCHED = new AtomicLong(0);
	private static AtomicLong MISMATCHED = new AtomicLong(0);
	private static List<String[]> MISMATCH_ROWS = Collections.synchronizedList(new ArrayList<>());

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final LinkedHashSet<Map<String, Object>> reportCards = new LinkedHashSet<Map<String, Object>>();

	/* ================= HIKARI DATA SOURCE ================= */
	private static HikariDataSource dataSource;

	@InputParams({ @InputParam(name = "Host", type = "java.lang.String"),
			@InputParam(name = "Port", type = "java.lang.Long"),
			@InputParam(name = "Database", type = "java.lang.String"),
			@InputParam(name = "Username", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String"),
			@InputParam(name = "Thread Size", type = "java.lang.Integer"),
			@InputParam(name = "Batch Size", type = "java.lang.Integer"),
			@InputParam(name = "Json File Path", type = "java.lang.String"),
			@InputParam(name = "Property File Path", type = "java.lang.String"),
			@InputParam(name = "HTML Report Path", type = "java.lang.String") })
	@ReturnType(name = "Logs", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		
		resetState();

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String host = (String) programElementsInput.get("Host");
		Long port = (Long) programElementsInput.get("Port");
		String database = (String) programElementsInput.get("Database");
		DB_USER = (String) programElementsInput.get("Username");
		DB_PASS = (String) programElementsInput.get("Password");
		THREADS_PER_TABLE = (int) programElementsInput.get("Thread Size");
		BATCH_SIZE = (int) programElementsInput.get("Batch Size");
		String jsonFilePath = (String) programElementsInput.get("Json File Path");
		String propertyFilePath = (String) programElementsInput.get("Property File Path");
		htmlReportPath = (String) programElementsInput.get("HTML Report Path");

		DB_URL = "jdbc:mysql://" + host + ":" + port + "/" + database;
		

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		logs = new JSONObject();
		
		try {
			// Your program element business logic starts here ...
			long startTime = System.currentTimeMillis();

			// ---------- Initialize HikariCP ----------
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(DB_URL);
			config.setUsername(DB_USER);
			config.setPassword(DB_PASS);
			config.setMaximumPoolSize(THREADS_PER_TABLE * TABLES.size() + 5);
			dataSource = new HikariDataSource(config);

			// ---------- LOAD PROPERTY FILES ----------
			File schemaDir = new File(propertyFilePath);
			for (File propFile : schemaDir.listFiles((d, n) -> n.endsWith(".properties"))) {
				loadTable(propFile.getAbsolutePath());
			}

			// ---------- STREAM JSON ONCE ----------
			Map<String, Map<String, Map<String, String>>> allTablesMap = new ConcurrentHashMap<>();
			for (String t : TABLES.keySet())
				allTablesMap.put(t, new ConcurrentHashMap<>());

			JsonFactory factory = MAPPER.getFactory();
			try (JsonParser parser = factory.createParser(new File(jsonFilePath))) {
				parser.nextToken(); // START_OBJECT or START_ARRAY
				while (parser.nextToken() != null) {
					if (parser.currentToken() == JsonToken.START_OBJECT) {
						JsonNode record = MAPPER.readTree(parser);

						for (TableConfig cfg : TABLES.values()) {
							List<JsonNode> nodes = resolvePath(record, cfg.jsonPath);
							for (JsonNode n : nodes) {
								String pkVal = getValue(n, cfg.columnMap.get(cfg.primaryKey));
								if (pkVal != null) {
									Map<String, String> flatMap = new HashMap<>();
									for (String col : cfg.columns) {
										if (cfg.parentKeyColumn != null && col.equals(cfg.parentKeyColumn))
											continue;
										flatMap.put(col, getValue(n, cfg.columnMap.get(col)));
									}
									allTablesMap.get(cfg.tableName).put(pkVal, flatMap);
								}
							}
						}
					}
				}
			}

			// ---------- VALIDATE TABLES IN PARALLEL ----------
			ExecutorService tableExec = Executors.newFixedThreadPool(TABLES.size());
			CountDownLatch tableLatch = new CountDownLatch(TABLES.size());

			for (TableConfig cfg : TABLES.values()) {
				Map<String, Map<String, String>> tableJson = allTablesMap.get(cfg.tableName);
				tableExec.submit(() -> {
					try {
						validateTable(cfg, tableJson);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						tableLatch.countDown();
					}
				});
			}

			tableLatch.await();
			tableExec.shutdown();
			dataSource.close();

			double duration = (System.currentTimeMillis() - startTime) / 1000.0;
			generateHtmlReport(duration);
			logs.put("Validation Duration", duration + " sec");

			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Error: ");
			throw new NlpException(e.getMessage());

		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("Logs", logs);
		return nlpResponseModel;
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
			if (!col.equals(cfg.primaryKey))
				cfg.columns.add(col);
		}

		TABLES.put(cfg.tableName, cfg);
	}

	/* ================= VALIDATE TABLE ================= */
	private static void validateTable(TableConfig cfg, Map<String, Map<String, String>> tableJson) throws Exception {
		List<String> allPKs = new ArrayList<>(tableJson.keySet());
		int totalBatches = (allPKs.size() + BATCH_SIZE - 1) / BATCH_SIZE;

		// Per-table counters
		AtomicLong tableTotal = new AtomicLong(0);
		AtomicLong tableMatched = new AtomicLong(0);
		AtomicLong tableMismatched = new AtomicLong(0);

		ExecutorService batchExec = Executors.newFixedThreadPool(THREADS_PER_TABLE);
		CountDownLatch batchLatch = new CountDownLatch(totalBatches);

		for (int i = 0; i < totalBatches; i++) {
			int start = i * BATCH_SIZE;
			int end = Math.min(start + BATCH_SIZE, allPKs.size());
			List<String> batchPKs = allPKs.subList(start, end);

			batchExec.submit(() -> {
				List<String[]> localMismatches = new ArrayList<>();
				try (Connection conn = dataSource.getConnection()) {
					validateBatch(cfg, batchPKs, tableJson, conn, localMismatches, tableTotal, tableMatched,
							tableMismatched);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					MISMATCH_ROWS.addAll(localMismatches);
					batchLatch.countDown();
				}
			});
		}

		batchLatch.await();
		batchExec.shutdown();

		Map<String, Object> tableData = new LinkedHashMap<String, Object>();

		// Print per-table stats
		tableData.put("Table", cfg.tableName);
		tableData.put("Total Records", tableTotal.get());
		tableData.put("Matched", tableMatched.get());
		tableData.put("Mismatched", tableMismatched.get());

		reportCards.add(tableData);
		
	}

	private static void validateBatch(TableConfig cfg, List<String> pkList, Map<String, Map<String, String>> pkJsonMap,
			Connection conn, List<String[]> localMismatches, AtomicLong tableTotal, AtomicLong tableMatched,
			AtomicLong tableMismatched) throws SQLException {

		if (pkList.isEmpty())
			return;

		// Build SQL for batch
		StringBuilder sql = new StringBuilder(
				"SELECT * FROM " + cfg.tableName + " WHERE `" + cfg.primaryKey + "` IN (");
		sql.append("?,".repeat(pkList.size()));
		sql.setLength(sql.length() - 1);
		sql.append(")");

		try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < pkList.size(); i++)
				ps.setString(i + 1, pkList.get(i));

			try (ResultSet rs = ps.executeQuery()) {
				ResultSetMetaData meta = rs.getMetaData();
				Map<String, Map<String, String>> dbRows = new HashMap<>();

				while (rs.next()) {
					Map<String, String> rowMap = new HashMap<>();
					for (int i = 1; i <= meta.getColumnCount(); i++) {
						rowMap.put(meta.getColumnName(i), rs.getString(i));
					}
					dbRows.put(rs.getString(cfg.primaryKey), rowMap);
				}

				// Compare JSON vs DB
				for (String pk : pkList) {
					Map<String, String> jsonRow = pkJsonMap.get(pk);
					Map<String, String> dbRow = dbRows.get(pk);

					if (dbRow == null) {
						localMismatches.add(
								new String[] { cfg.tableName, pk, "ROW_NOT_FOUND", "Exists in JSON", "Missing in DB" });
						tableMismatched.incrementAndGet();
						MISMATCHED.incrementAndGet(); // global
					} else {
						boolean rowMatch = true;
						for (String col : cfg.columns) {
							if (cfg.parentKeyColumn != null && col.equals(cfg.parentKeyColumn))
								continue;

							String jsonVal = jsonRow.get(col);
							String dbVal = dbRow.get(col);

							if (!Objects.equals(jsonVal, dbVal)) {
								rowMatch = false;
								localMismatches.add(new String[] { cfg.tableName, pk, col, jsonVal, dbVal });
							}
						}
						if (rowMatch) {
							tableMatched.incrementAndGet();
							MATCHED.incrementAndGet(); // global
						} else {
							tableMismatched.incrementAndGet();
							MISMATCHED.incrementAndGet(); // global
						}
					}
					tableTotal.incrementAndGet();
					TOTAL.incrementAndGet(); // global
				}
			}
		}
	}

	/* ================= JSON PATH RESOLUTION ================= */
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
					if (!child.isMissingNode())
						next.add(child);
				}
			}
			current = next;
		}
		return current;
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

	/* ================= HTML REPORT ================= */
	private static void generateHtmlReport(double duration) throws Exception {

		double matchPct = TOTAL.get() == 0 ? 0 : (MATCHED.get() * 100.0 / TOTAL.get());
		double mismatchPct = TOTAL.get() == 0 ? 0 : (MISMATCHED.get() * 100.0 / TOTAL.get());

		try (PrintWriter out = new PrintWriter(htmlReportPath)) {

			out.println("<!DOCTYPE html><html><head><title>JSON vs DB Validation</title>"
					+ "<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>" + "<style>"
					+ "body{font-family:Arial;background:#f4f6f9}"

					// base card
					+ ".card{background:#fff;padding:16px;margin:20px;border-radius:10px;"
					+ "box-shadow:0 2px 6px rgba(0,0,0,.1)}"

					// summary cards only
					+ ".stat-card{width:260px;text-align:center}"

					+ ".flex{display:flex;gap:20px;justify-content:center;flex-wrap:wrap}"

					// rows inside card
					+ ".row{display:flex;justify-content:space-between;" + "margin:8px 0;font-size:15px}"

					+ ".label{font-weight:600;color:#555}"

					// table
					+ "table{border-collapse:collapse;width:100%}"
					+ "th,td{border:1px solid #ddd;padding:8px;text-align:center}" + "th{background:#f44336;color:#fff}"

					+ "</style></head><body>");

			out.println("<h1 style=\"text-align:center\">JSON vs DB Validation Report</h1>");

			/* ================= SUMMARY CARDS ================= */
			out.println("<div class=\"flex\">");

			for (Map<String, Object> cards : reportCards) {

				String table = cards.get("Table").toString();
				TOTAL = toAtomicLong(cards.get("Total Records"));
				MATCHED = toAtomicLong(cards.get("Matched"));
				MISMATCHED = toAtomicLong(cards.get("Mismatched"));

				out.println("<div class=\"card stat-card\">" + "<h3>" + table + "</h3>" +

						"<div class=\"row\">" + "<span class=\"label\">Total</span>" + "<span>" + TOTAL.get()
						+ "</span>" + "</div>" +

						"<div class=\"row\">" + "<span class=\"label\">Matched</span>" + "<span style=\"color:green\">"
						+ MATCHED.get() + "</span>" + "</div>" +

						"<div class=\"row\">" + "<span class=\"label\">Mismatched</span>" + "<span style=\"color:red\">"
						+ MISMATCHED.get() + "</span>" + "</div>" + "</div>");
			}

			out.println("</div>");

			/* ================= PIE CHART ================= */
			out.println("<div class=\"card\" style=\"width:260px;margin:auto;text-align:center\">"
					+ "<canvas id=\"pie\"></canvas></div>");

			out.println("<script>" + "new Chart(document.getElementById('pie'),{" + "type:'pie',"
					+ "data:{labels:['Matched','Mismatched']," + "datasets:[{data:[" + matchPct + "," + mismatchPct
					+ "]," + "backgroundColor:['#4caf50','#f44336']}]},"
					+ "options:{plugins:{legend:{position:'bottom'}}}" + "});</script>");

			out.println("<p style=\"text-align:center\">Execution Time: " + duration + " sec</p>");

			/* ================= MISMATCH TABLE ================= */
			out.println("<div class=\"card\" style=\"width:95%;margin:auto\">" + "<h3>Mismatches</h3><table>"
					+ "<tr><th>Table</th><th>PK</th><th>Field</th><th>JSON</th><th>DB</th></tr>");

			for (String[] r : MISMATCH_ROWS) {
				out.println("<tr>");
				for (String c : r) {
					out.println("<td>" + c + "</td>");
				}
				out.println("</tr>");
			}

			out.println("</table></div></body></html>");
		}

	}

	private static AtomicLong toAtomicLong(Object obj) {
		if (obj instanceof AtomicLong) {
			return (AtomicLong) obj;
		} else if (obj instanceof Number) { // Long, Integer, etc.
			return new AtomicLong(((Number) obj).longValue());
		} else if (obj instanceof String) {
			return new AtomicLong(Long.parseLong((String) obj));
		} else {
			return new AtomicLong(0); // fallback
		}
	}
	
	private static void resetState() {

	    // Clear table configs
	    TABLES.clear();

	    // Reset counters
	    TOTAL.set(0);
	    MATCHED.set(0);
	    MISMATCHED.set(0);

	    // Clear collections
	    MISMATCH_ROWS.clear();
	    reportCards.clear();

	    // Close datasource if exists
	    if (dataSource != null && !dataSource.isClosed()) {
	        dataSource.close();
	    }
	    dataSource = null;
	}

}