//
//package Business_logic;
//
//import java.io.File;
//
//
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Map;
//
//import org.opencv.core.Core;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.Point;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//
//import com.jayway.jsonpath.JsonPath;
//import com.tyss.optimize.common.util.CommonConstants;
//import com.tyss.optimize.nlp.util.Nlp;
//import com.tyss.optimize.nlp.util.NlpException;
//import com.tyss.optimize.nlp.util.NlpRequestModel;
//import com.tyss.optimize.nlp.util.NlpResponseModel;
//import com.tyss.optimize.nlp.util.annotation.InputParam;
//import com.tyss.optimize.nlp.util.annotation.InputParams;
//import com.tyss.optimize.nlp.util.annotation.ReturnType;
//
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//public class accessibilityForMobile implements Nlp {
//	public static String appPackage=null;
//	public static String appActivity=null;
//	public static String api=null;
//	@InputParams({@InputParam(name = "appPackage", type = "java.lang.String"), @InputParam(name = "appActivity", type = "java.lang.String"),
//		 @InputParam(name = "API key", type = "java.lang.String")})
//	@ReturnType(name = "Sum of Two numbers", type = "java.lang.String")
//
//	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
//
//		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
//		String appP = (String) programElementsInput.get("appPackage");
//		String appA = (String) programElementsInput.get("appActivity");
//		 api = (String) programElementsInput.get("API key");
//
//		NlpResponseModel nlpResponseModel = new NlpResponseModel();
//		try {
//			appPackage=appP;
//			appActivity=appA;
//			// Your program element business logic starts here ...
//			String command="adb shell am start -n "+appPackage+"/"+appActivity;
//	    	System.out.println(command);
//	    	excuteCommand("adb shell am start -n "+appPackage+"/"+appActivity);
//	    	Thread.sleep(5000);
//	    	takeScreenshot("adb exec-out screencap -p");
////	    	excuteCommand("adb exec-out screencap -p > C:\\Users\\User\\Downloads\\Screen.png");
//	    	Thread.sleep(2000);
//	    	String coord=getCoordinates();
//	    	int x=(int) Double.parseDouble((coord.split(":"))[0]);
//	    	int y=(int) Double.parseDouble((coord.split(":"))[1]);
////	    	int y=Integer.parseInt((coord.split(":"))[1]);
//	    	excuteCommand("adb shell input tap "+x+" "+y);
//	    	Thread.sleep(10000);
//	    	String body=getScanDetails();
//
//			nlpResponseModel.setStatus(CommonConstants.pass);
//			//Uncomment the below line and modify the message, only if you want to override the pass message defined for the program element.
//			nlpResponseModel.setMessage("Successfully fetched the details");
//		}
//		catch(Exception e) {
//			// Your program element Exception handling goes here ...
//			nlpResponseModel.setStatus(CommonConstants.fail);
//
//			//Uncomment the below line and modify the message, only if you want to override the fail message defined for the program element.
//			nlpResponseModel.setMessage("Failed to fetch details");
//		}
//
//		// Your program element business logic ends here ...
//		nlpResponseModel.getAttributes().put("Sum of Two numbers", getScanDetails());
//		return nlpResponseModel;
//	}
//	public static String getCoordinates() {
//		String load=System.getProperty("user.home");
//		System.load(load+"\\Downloads\\opencv_java342.dll");
//
//		// Paths to the images
////         destination = Paths.get(System.getProperty("user.home"), "Screen.png").toString();
//
//		String fullImagePath =  Paths.get(System.getProperty("user.home"), "Screen.png").toString(); // Your screenshot
//		String iconImagePath = load+"\\Downloads\\man.png";   // Image of the icon to match
//
//		// Load the full image and icon image
//		Mat fullImage = Imgcodecs.imread(fullImagePath);
//		Mat iconImage = Imgcodecs.imread(iconImagePath);
//
//		// Create result matrix
//		int result_cols = fullImage.cols() - iconImage.cols() + 1;
//		int result_rows = fullImage.rows() - iconImage.rows() + 1;
//		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
//
//		// Match the template (icon) in the full image
//		Imgproc.matchTemplate(fullImage, iconImage, result, Imgproc.TM_CCOEFF_NORMED);
//
//		// Find the best match location
//		Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
//		Point matchLoc = mmr.maxLoc;
//
//		// Output the coordinates
//		System.out.println("Coordinates of the icon: x = " + matchLoc.x + ", y = " + matchLoc.y);
//
//		return matchLoc.x+":"+matchLoc.y ;
//
//	}
//	public static String  getResultID() {
//		String url = "https://axe-mobile-backend.deque.com/attest/results/summaries";
//		String apiKey = api;
//
//		OkHttpClient client = new OkHttpClient();
//		Request request = new Request.Builder()
//				.url(url)
//				.get()
//				.addHeader("X-Api-Key", apiKey)
//				.addHeader("Content-Type", "application/json")
//				.build();
//		String resultId=null;
//		try (okhttp3.Response response = client.newCall(request).execute()) {
//			if (response.isSuccessful() && response.body() != null) {
//				String responseBody = response.body().string();
//				resultId = JsonPath.parse(responseBody).read("$[0].key.resultId", String.class);
//
//				System.out.println("Result ID: " + resultId);
//
//			} else {
//				System.out.println("Error: " + response.code());
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return resultId;
//
//	}
//	public static String getScanDetails() {
//		String resultId=getResultID();
//		String url = "https://axe-mobile-backend.deque.com/attest/result/axe/ef09a929-3362-4760-b4f1-19e2896e7d79/"+appPackage+"/"+resultId;
//		String apiKey = api;
//
//		OkHttpClient client = new OkHttpClient();
//		Request request = new Request.Builder()
//				.url(url)
//				.get()
//				.addHeader("X-Api-Key", apiKey)
//				.addHeader("Content-Type", "application/json")
//				.build();
//		String responseBody=null;
//		try (Response response = client.newCall(request).execute()) {
//			if (response.isSuccessful() && response.body() != null) {
//				 responseBody = response.body().string();
//
//				// Use JSONPath to extract the resultId (adjust the path as per your JSON structure)
//				//                String resultId = JsonPath.parse(responseBody).read("$[0].key.resultId", String.class);
//				//
//				//                System.out.println("Result ID: " + resultId);
//				System.out.println(responseBody);
//
//			} else {
//				System.out.println("Error: " + response.code());
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return responseBody;
//
//	}
//	public static void excuteCommand( String command) {
//		try {
//			System.out.println(command);
//			Runtime.getRuntime().exec(command);
//			System.out.println("Execued the command");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//
//	}
//	public static void takeScreenshot(String command) throws IOException, InterruptedException {
//		Process process = Runtime.getRuntime().exec(command);
//
//		// Read the process's input stream (the PNG data)
//		InputStream inputStream = process.getInputStream();
//		String load=System.getProperty("user.home");
//
//		File outputFile = new File(load+"\\Screen.png");
//		FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
//
//		// Write the input stream to the output file
//		byte[] buffer = new byte[4096];
//		int bytesRead;
//		while ((bytesRead = inputStream.read(buffer)) != -1) {
//			fileOutputStream.write(buffer, 0, bytesRead);
//		}
//
//		// Close streams
//		fileOutputStream.close();
//		inputStream.close();
//
//		// Wait for the process to complete
//		int exitCode = process.waitFor();
//		if (exitCode == 0) {
//			System.out.println("Screenshot saved to: " + outputFile.getAbsolutePath());
//		} else {
//			System.out.println("Command failed with exit code: " + exitCode);
//		}
//
//	} 
//	public static void main(String[] args) throws NlpException {
//		NlpRequestModel nlp = new NlpRequestModel();
//
//		Map<String, Object> programElementsInput = nlp.getAttributes();
//		
//		programElementsInput.put("API key", "b2340b8d-8eec-4d89-8b69-52c346adea18");
//		programElementsInput.put("appPackage", "com.tyss.rmgyantra");
//		programElementsInput.put("appActivity", "com.tyss.rmgyantra.LoginActivity");
//		new accessibilityForMobile().execute(nlp);
//	}
//}
package Business_logic;

