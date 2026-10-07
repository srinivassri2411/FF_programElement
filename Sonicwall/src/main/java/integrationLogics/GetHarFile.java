package integrationLogics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;
import lombok.extern.slf4j.Slf4j;
import net.lightbody.bmp.BrowserMobProxy;

//@Slf4j

public class GetHarFile implements Nlp {
	@InputParams({ @InputParam(name = "Proxy Object", type = "net.lightbody.bmp.BrowserMobProxy") })
	@ReturnType(name = "filesReturn", type = "java.util.List")
	@Override
	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();
		return params;
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		StringBuilder sb = new StringBuilder();
		return sb;
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String filePath = "";
		String harFilteredFilePath = "";
		ArrayList<String> fileList = null;
		try {
			BrowserMobProxy proxy = (BrowserMobProxy) attributes.get("Proxy Object");
			File harFile = File.createTempFile("FF_BrowserLogs_", ".har");
			File harFilteredFile = File.createTempFile("FF_BrowserLogs_Filtered_", ".har");
			FileOutputStream fos = new FileOutputStream(harFile);
			proxy.getHar().writeTo(fos);
			fos.close();
			Thread.sleep(1000);
			proxy.stop();
			killProcess(proxy.getPort());
			filePath = harFile.getAbsolutePath();
			harFilteredFilePath = filterFailedApiCalls(filePath, harFilteredFile.getAbsolutePath());
			fileList = new ArrayList<>();
			fileList.add(filePath);
			fileList.add(harFilteredFilePath);
			nlpResponseModel.setOutputLocalFilePaths(fileList);
			nlpResponseModel.setMessage("Successfully fetched the .har");
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.getAttributes().put("filesReturn", fileList);
		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
//			log.error(exception.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed " + exception.toString());
		}
		return nlpResponseModel;
	}

	public static String filterFailedApiCalls(String originalHarPath, String harFilteredFile)
			throws IOException, JSONException {
		String harContent = FileUtils.readFileToString(new File(originalHarPath), "UTF-8");

		JSONObject harJson = new JSONObject(harContent);
		JSONObject log = harJson.getJSONObject("log");
		JSONArray entries = log.getJSONArray("entries");
		JSONArray failedEntries = new JSONArray();
		for (int i = 0; i < entries.length(); i++) {
			JSONObject entry = entries.getJSONObject(i);
			int status = entry.getJSONObject("response").getInt("status");
			if (status >= 205) {
				failedEntries.put(entry);
			}
		}

		JSONObject filteredHar = new JSONObject();
		JSONObject filteredLog = new JSONObject();
		filteredLog.put("version", log.getString("version"));
		filteredLog.put("creator", log.getJSONObject("creator"));

		if (log.has("browser")) {
			filteredLog.put("browser", log.getJSONObject("browser"));
		}

		if (log.has("pages")) {
			filteredLog.put("pages", log.getJSONArray("pages"));
		}

		filteredLog.put("entries", failedEntries);
		filteredHar.put("log", filteredLog);

		Path outputFilePath = Paths.get(harFilteredFile);

		Files.write(outputFilePath, filteredHar.toString(2).getBytes());

		return outputFilePath.toString();
	}

	public interface IPHlpAPI extends Library {
		IPHlpAPI INSTANCE = Native.load("Iphlpapi", IPHlpAPI.class, W32APIOptions.DEFAULT_OPTIONS);

		int GetExtendedTcpTable(Pointer pTcpTable, IntByReference pdwSize, boolean order, int ulAf, int tableClass,
				int reserved);
	}

	public static void killProcess(Integer port) throws IOException {
		String operatingSystem = System.getProperty("os.name").toLowerCase();

		if (operatingSystem.contains("win")) {
			int pid = getPidForPort(port);
			if (pid > 0) {
				System.out.println("**************id is " + pid);
				Process process = Runtime.getRuntime().exec("tasklist /FI \"PID eq " + pid + "\"");
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					System.out.println("Process details for PID " + pid + ":");
					reader.lines().forEach(System.out::println);
				} catch (IOException e) {
					System.err.println("Unable to fetch tasklist info for PID: " + pid);
				}
				Runtime.getRuntime().exec("taskkill /F /PID " + pid);
				System.out.println("Killed process with PID: " + pid + " on port: " + port);
			} else {
				System.out.println("No process found listening on port " + port);
			}
		} else {
			String command = "lsof -t -i:" + port;
			Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				String pid;
				while ((pid = reader.readLine()) != null) {
					pid = pid.trim();
					if (!pid.isEmpty()) {
						System.out.println("**************id is " + pid);
						Runtime.getRuntime().exec("kill -9 " + pid);
						System.out.println("Killed PID: " + pid + " on port: " + port);
						return;
					}
				}
			}
			System.out.println("No process found listening on port " + port);
		}
	}

	private static int getPidForPort(int targetPort) throws IOException {
		final int AF_INET = 2; // IPv4
		final int TCP_TABLE_OWNER_PID_ALL = 5;
		final int bufferSize = 65536;

		Pointer buffer = new Memory(bufferSize);
		IntByReference sizePtr = new IntByReference(bufferSize);

		int result = IPHlpAPI.INSTANCE.GetExtendedTcpTable(buffer, sizePtr, true, AF_INET, TCP_TABLE_OWNER_PID_ALL, 0);
		if (result != 0) {
			throw new IOException("GetExtendedTcpTable failed with code: " + result);
		}

		int numEntries = buffer.getInt(0);
		int rowSize = 24;
		int offset = 4;

		for (int i = 0; i < numEntries; i++) {
			int state = buffer.getInt(offset);
			int localAddr = buffer.getInt(offset + 4);
			int localPort = Short.reverseBytes(buffer.getShort(offset + 8)) & 0xFFFF;
			int pid = buffer.getInt(offset + 20);

			if (localPort == targetPort && state == 2 /* LISTEN */) {
				return pid;
			}

			offset += rowSize;
		}

		return -1;
	}
}