
package callLogs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class PlaceCallAndCaptureLogs implements Nlp {

	private static int lastState;
	private static Instant callStart;
	private static volatile boolean disconnectHandled = false;
	private static volatile boolean callEverConnected = false;

	private static String lastNetwork = "";
	private static String lastPrimaryCell = "";
	public static JSONObject obj;

	@InputParams({ @InputParam(name = "Phone Num", type = "java.lang.String"),
			@InputParam(name = "Duration", type = "java.lang.Integer") })
	@ReturnType(name = "Network data", type = "org.json.JSONObject")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

		String num = (String) programElementsInput.get("Phone Num");
		int seconds = (int) programElementsInput.get("Duration");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		obj = new JSONObject();

		try {
			lastState = getCallState();
			int flag = 1;

			while (!disconnectHandled) {

				int currentState = getCallState();
				if (flag == 1) {
					placeCall(num, seconds);
					flag++;
				}

				// Call connected
				if (currentState == 2 && !callEverConnected) {
					callEverConnected = true;
					callStart = Instant.now();

					obj.put("callState", "Call connected");
					printNetworkDetails();
					trackServingCell();
				}
				// Detect call state changes
				if (currentState != lastState) {

					// Incoming ringing
					if (lastState == 0 && currentState == 1) {

						obj.put("callState", "Incomming Call Identified");
						disconnectHandled = false;
					}

					// Call disconnected AFTER being connected (manual OR auto)
					if (currentState == 0 && callEverConnected && !disconnectHandled) {

						long duration = (callStart != null)
								? Instant.now().getEpochSecond() - callStart.getEpochSecond()
								: -1;

						obj.put("callState", "Call disconnected");
						obj.put("Duration", duration);

						disconnectHandled = true;
						callEverConnected = false;
						callStart = null;

						break;
					}

					// Disconnected without answer
					if ((lastState == 1 || lastState == 2) && currentState == 0 && callStart == null
							&& !disconnectHandled) {
						obj.put("callState", "Call disconnected without answer");
						disconnectHandled = true;
					}

					lastState = currentState;
				}

				// Detect network changes
				String currentNetwork = getNetworkType();
				if (!currentNetwork.equals(lastNetwork)) {
					obj.put("Network changed to: ", currentNetwork);
					lastNetwork = currentNetwork;
					trackServingCell();
				}

				// Log tower changes during active call
				if (callStart != null) {
					String currentPrimaryCell = readLineContaining("adb shell dumpsys telephony.registry",
							"mRegistered=YES").toString();
					if (!currentPrimaryCell.isEmpty() && !currentPrimaryCell.equals(lastPrimaryCell)) {

						obj.put("Tower changed during call: ", trackServingCell());
						lastPrimaryCell = currentPrimaryCell;
						trackServingCell();
					}
				}

				Thread.sleep(500);
			}

			flag = 1;
			disconnectHandled = false;
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("Network data", obj);
		return nlpResponseModel;
	}

	// ---------------- CALL STATE ----------------
	private static int getCallState() throws Exception {
		return extractInt(readLineContaining("adb shell dumpsys telephony.registry", "mCallState="), "mCallState");
	}

	// ---------------- NETWORK DETAILS ----------------
	private static void printNetworkDetails() throws Exception {

		obj.put("Network", getNetworkType());
		obj.put("Technology", getMobileTech());
		obj.put("Carrier", getCarrierInfo());
		obj.put("Signal Strength", getSignalStrength());

		String incoming = extractString(
				readLineContaining("adb shell dumpsys telephony.registry", "mCallIncomingNumber="),
				"mCallIncomingNumber");

		if (!incoming.isEmpty())
			obj.put("Incoming Number: ", incoming);
	}

	private static String getNetworkType() throws Exception {
		String dump = runCmd("adb shell dumpsys connectivity");
		
		if (dump.contains("IWLAN") || dump.contains("WIFI") && dump.contains("CONNECTED"))
			return "WIFI";
		else
			return "MOBILE";
	}

	private static String getMobileTech() {
		String line = readLineContaining("adb shell dumpsys telephony.registry", "mServiceState=");
		int tech = Integer.parseInt(line.split("getRilDataRadioTechnology=")[1].split("[(]")[0].trim());

		return switch (tech) {
		case 1, 2 -> "2G";
		case 3, 8, 9, 10 -> "3G";
		case 14 -> "4G (LTE / VoLTE)";
		case 20 -> "5G (NR)";
		case 18 -> "Wi-Fi Calling (IWLAN)";
		default -> "UNKNOWN";
		};
	}

	private static String getCarrierInfo() {
		String dump = runCmd("adb shell dumpsys telephony.registry");
		Matcher m = Pattern.compile("mOperatorAlphaLong=([^]+)").matcher(dump);
		return m.find() ? m.group(1).trim() : "UNKNOWN";
	}

	// ---------------- SIGNAL ----------------
	private static JSONObject getSignalStrength() {

		String line = readLineContaining("adb shell dumpsys telephony.registry", "mSignalStrength=");
		if (line.isEmpty())
			return null;

		JSONObject objSignal1 = parseTech(line, "mLte", new String[] { "rssi", "rsrp", "rsrq", "rssnr", "level" });
		JSONObject objSignal2 = parseTech(line, "mNr", new String[] { "ssRsrp", "ssSinr", "level" });
		return (objSignal1 != null) ? objSignal1 : objSignal2;

	}

	private static JSONObject parseTech(String src, String tech, String[] keys) {
		JSONObject obj3 = new JSONObject();
		JSONObject obj4 = new JSONObject();
		if (!src.contains(tech + "="))
			return null;

		String section = src.split(tech + "=")[1].split(",m")[0];
		obj3.put(tech.replace("m", ""), obj4);

		for (String key : keys) {
			String val = extractValue(section, key);
			if (isValid(val)) {
				obj4.put(key, val);
			}
		}
		return obj3;
	}

	// ---------------- CELL TOWER ----------------
	private static JSONObject trackServingCell() {
		JSONObject obj2 = new JSONObject();
		String dump = runCmd("adb shell dumpsys telephony.registry");
		if (!dump.contains("CellInfoLte"))
			return obj2;

		String[] cells = dump.split("CellInfoLte\\{");
		boolean primaryPrinted = false;

		for (String cell : cells) {
			if (!cell.contains("mRegistered=YES"))
				continue;

			// Extract values using split instead of regex
			String ci = extractValueSplit(cell, "mCi=");
			String pci = extractValueSplit(cell, "mPci=");
			String tac = extractValueSplit(cell, "mTac=");
			String mcc = extractValueSplit(cell, "mMcc=");
			String mnc = extractValueSplit(cell, "mMnc=");

			if (!isValid(ci) || ci.equals(lastPrimaryCell))
				lastPrimaryCell = ci;

			obj.put("Serving LTE Cell:", obj2);
			obj2.put("CID", ci);
			obj2.put("PCI", pci);
			obj2.put("TAC", tac);
			obj2.put("MCC", mcc);
			obj2.put("MNC", mnc);

			primaryPrinted = true;
		}

		if (!primaryPrinted) {
			// System.out.println("No primary serving LTE cell found.");
		}
		return obj2;
	}

	private static String extractValueSplit(String src, String key) {
		int idx = src.indexOf(key);
		if (idx == -1)
			return "N/A";

		int end = src.indexOf(' ', idx);
		if (end == -1)
			end = src.length();

		return src.substring(idx + key.length(), end).trim();
	}

	// ---------------- UTIL ----------------
	private static boolean isValid(String v) {
		return v != null && !v.isEmpty() && !v.equals("2147483647") && !v.equals("N/A");
	}

	private static int extractInt(String src, String key) {
		try {
			return Integer.parseInt(src.split(key + "=")[1].split("[,)]")[0].trim());
		} catch (Exception e) {
			return -1;
		}
	}

	private static String extractString(String src, String key) {
		try {
			return src.split(key + "=")[1].trim();
		} catch (Exception e) {
			return "";
		}
	}

	private static String extractValue(String src, String key) {
		Matcher m = Pattern.compile(key + "=(-?\\d+)").matcher(src);
		return m.find() ? m.group(1) : "N/A";
	}

	private static String readLineContaining(String cmd, String contains) {
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(Runtime.getRuntime().exec(cmd).getInputStream()))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains(contains))
					return line;
			}
		} catch (Exception ignored) {
		}
		return "";
	}

	private static String runCmd(String cmd) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(Runtime.getRuntime().exec(cmd).getInputStream()))) {
			String line;
			while ((line = br.readLine()) != null)
				sb.append(line).append("\n");
		} catch (Exception ignored) {
		}
		return sb.toString();
	}

	private static void placeCall(String num, int seconds) {
		try {
			Runtime.getRuntime().exec("adb shell am start -a android.intent.action.CALL -d tel:"+num);

			new Thread(() -> {
				try {
					Thread.sleep(seconds * 1000);
					if (!disconnectHandled) {
						Runtime.getRuntime().exec("adb shell input keyevent KEYCODE_ENDCALL");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}