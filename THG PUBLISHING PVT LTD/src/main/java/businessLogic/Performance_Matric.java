
package businessLogic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class Performance_Matric implements Nlp {
	// @InputParams({@InputParam(name = "Test URL", type = "java.lang.String")})
	@ReturnType(name = "results", type = "java.util.Map")

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
		WebDriver driver = nlpRequestModel.getWebDriver();
		Map<String, Long> result = null;
		try {
			result = getPerformanceMatrix(driver);
			nlpResponseModel.setMessage("Successfully fetched Performance Matrics " + result);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to fetch Performance Matrics "+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("results", result);
		return nlpResponseModel;
	}

	private static long getTimeToFirstByte(JavascriptExecutor jsExecutor) {
		// Execute JavaScript to get the time to the first byte
		Object firstByteTime = jsExecutor.executeScript(
				"return window.performance.timing.responseStart - window.performance.timing.navigationStart");

		// Convert the result to milliseconds
		return Math.round(Double.parseDouble(firstByteTime.toString()));
	}

	private static long getFirstCPUIdle(JavascriptExecutor jsExecutor) {
		// Execute JavaScript to get the First CPU Idle
		Object fci = jsExecutor
				.executeScript("return window.performance.getEntriesByType('navigation')[0].domComplete");

		// Convert the result to milliseconds
		return Math.round(Double.parseDouble(fci.toString()));
	}

	private static long getFirstContentfulPaint(JavascriptExecutor jsExecutor) {
		// Execute JavaScript to get the First Contentful Paint time
		Object fcpTime = jsExecutor.executeScript("return window.performance.getEntriesByType('paint')[0].startTime");

		// Convert the result to milliseconds
		return Math.round(Double.parseDouble(fcpTime.toString()));
	}

	private static long measurePageLoadTime(JavascriptExecutor jsExecutor) {
		// Execute JavaScript to measure page load time
		Object loadEventEnd = jsExecutor.executeScript(
				"return window.performance.timing.loadEventEnd - window.performance.timing.navigationStart");

		// Convert the result to milliseconds
		return Math.round(Double.parseDouble(loadEventEnd.toString()));
	}

	private static long getNetworkTime(JavascriptExecutor jsExecutor) {
		// Execute JavaScript to get network time
		Object networkTime = jsExecutor.executeScript(
				"return window.performance.timing.responseEnd - window.performance.timing.navigationStart");

		// Convert the result to milliseconds
		return Math.round(Double.parseDouble(networkTime.toString()));
	}

	private static long getStartRenderTime(JavascriptExecutor jsExecutor) {
		// Execute JavaScript to get the Start Render time
		Object startRenderTime = jsExecutor.executeScript(
				"return window.performance.timing.domContentLoadedEventEnd - window.performance.timing.navigationStart");

		// Convert the result to milliseconds
		return Math.round(Double.parseDouble(startRenderTime.toString()));
	}

	private static long getTimeToInteractive(JavascriptExecutor jsExecutor) {
		// Execute JavaScript to get the Time to Interactive

		Object tti = jsExecutor.executeScript(
				"return window.performance.timing.domInteractive - window.performance.timing.navigationStart");

		// Convert the result to milliseconds
		return Math.round(Double.parseDouble(tti.toString()));
	}

	private static long getLatestContentfullPaint(JavascriptExecutor jsExecutor) {
		// Execute JavaScript to get the Time to Interactive
		Object lcpValue = (Long) jsExecutor.executeScript("let lcpValue = 0; "
				+ "const observer = new PerformanceObserver((list) => { " + "const entries = list.getEntries(); "
				+ "const lcpEntry = entries[entries.length - 1]; " + "lcpValue = lcpEntry.startTime; " + "}); "
				+ "observer.observe({ type: 'largest-contentful-paint', buffered: true }); " + "return lcpValue;");

		// Convert the result to milliseconds
		return Math.round(Double.parseDouble(lcpValue.toString()));
	}

	public static Map<String, Long> getPerformanceMatrix(WebDriver driver) {
		JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
		Map<String, Long> map = new LinkedHashMap<>();
		long timeOfInteractive = getTimeToInteractive(jsExecutor);
		map.put("Time of Interactive", timeOfInteractive);
		long firstByte = getTimeToFirstByte(jsExecutor);
		map.put("First Byte", firstByte);
		long fcp = getFirstContentfulPaint(jsExecutor);
		map.put("First Contenentful Paint", fcp);
		long firstCPUIdle = getFirstCPUIdle(jsExecutor);
		map.put("First CPU Idle", firstCPUIdle);
		long pageLoadTime = measurePageLoadTime(jsExecutor);
		map.put("Page Load Time", pageLoadTime);
		long pageNetworkTime = getNetworkTime(jsExecutor);
		map.put("Page Network Time", pageNetworkTime);
		long startRender = getStartRenderTime(jsExecutor);
		map.put("Start Render", startRender);
		long lcpValue = getLatestContentfullPaint(jsExecutor);
		//map.put("Largest Contentfull Paint", lcpValue);

		return map;
	}
}