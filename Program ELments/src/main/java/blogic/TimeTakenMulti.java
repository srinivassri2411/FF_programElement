//package blogic;
//
//import java.io.IOException;
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//
//import org.openqa.selenium.By;
//import org.openqa.selenium.NoSuchElementException;
//import org.openqa.selenium.StaleElementReferenceException;
//import org.openqa.selenium.WebDriver;
//
//import com.tyss.optimize.common.util.CommonConstants;
//import com.tyss.optimize.nlp.util.Nlp;
//import com.tyss.optimize.nlp.util.NlpException;
//import com.tyss.optimize.nlp.util.NlpRequestModel;
//import com.tyss.optimize.nlp.util.NlpResponseModel;
//import com.tyss.optimize.nlp.util.annotation.InputParam;
//import com.tyss.optimize.nlp.util.annotation.InputParams;
//import com.tyss.optimize.nlp.util.annotation.ReturnType;
//
//public class TimeTakenMulti implements Nlp {
//	@InputParams({ @InputParam(name = "xpaths", type = "java.util.Map"),
//			@InputParam(name = "Expected Time", type = "java.lang.String") })
//	@ReturnType(name = "returnData", type = "java.util.Map")
//
//	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
//
//		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
//		Map<String, String> xpaths = (Map<String, String>) programElementsInput.get("xpaths");
//		String exp = (String) programElementsInput.get("Expected Time");
//		Map<String, String> returnValue = null;
//		Duration impli = null;
//		WebDriver driver = null;
//		NlpResponseModel nlpResponseModel = new NlpResponseModel();
//		try {
//			driver = nlpRequestModel.getWebDriver();
//			impli = driver.manage().timeouts().getImplicitWaitTimeout();
//			returnValue = runMultiple(driver, xpaths, impli, Double.parseDouble(exp));
//			nlpResponseModel.setMessage("Fetched the time taken");
//			nlpResponseModel.setStatus(CommonConstants.pass);
//		} catch (Exception e) {
//			nlpResponseModel.setMessage("Failed to fetch " + e);
//			nlpResponseModel.setStatus(CommonConstants.fail);
//		} finally {
//			driver.manage().timeouts().implicitlyWait(impli);
//		}
//		nlpResponseModel.getAttributes().put("returnData", returnValue);
//		return nlpResponseModel;
//	}
//
//	public static Map<String, String> runMultiple(WebDriver driver, Map<String, String> xpaths, Duration impli,
//			Double exp) throws Exception {
//		ExecutorService executor = Executors.newFixedThreadPool(xpaths.size());
//		List<Future<Void>> tasks = new ArrayList<>();
//		Map<String, String> resultMap = new ConcurrentHashMap<>();
//		for (Entry<String, String> entry : xpaths.entrySet()) {
//			tasks.add(executor.submit(() -> {
//				String key = entry.getKey();
//				String xpath = entry.getValue();
//				try {
//					double timeTaken = timeTaken(driver, xpath, impli);
//					if (timeTaken >= exp) {
//						resultMap.put(key, "Failed: Element took longer than 4 seconds " + timeTaken);
//					} else {
//						resultMap.put(key, "Fetched the time taken " + timeTaken);
//					}
//				} catch (RuntimeException e) {
//					if (!e.getMessage().contains("Element not found in loop")) {
//						throw e;
//					} else {
//						resultMap.put(key, "Element not found in loop");
//					}
//				} catch (Exception e) {
//					throw e;
//				}
//				return null;
//			}));
//		}
//		for (Future<Void> task : tasks) {
//			try {
//				task.get();
//			} catch (Exception e) {
//				throw e;
//			}
//		}
//
//		executor.shutdown();
//		return resultMap;
//	}
//
//	public static Double timeTaken(WebDriver driver, String xpath, Duration impli)
//			throws InterruptedException, IOException {
//		double endTime;
//		System.out.println("Xpath is " + xpath);
//		System.out.println("fireflink implicit" + impli);
//		long timeOut = impli.toMillis();
//		driver.manage().timeouts().implicitlyWait(Duration.ofMillis(100));
//		double countTime = 0.0;
//		System.out.println("Loop End time " + timeOut);
//		while (true) {
//			countTime += 140.0;
//			if (countTime <= timeOut) {
//				try {
//					System.out.println("Try block Entered");
//					if (driver.findElement(By.xpath(xpath)).isDisplayed()) {
//						endTime = countTime;
//						return Double.parseDouble(String.valueOf(Math.round(endTime / 1000.0)));
//					} else {
//						Thread.sleep(100);
//					}
//				} catch (StaleElementReferenceException e) {
//					System.out.println("Stale element block");
//					endTime = countTime;
//					return Double.parseDouble(String.valueOf(Math.round(endTime / 1000.0)));
//				} catch (NoSuchElementException e) {
//					System.out.println("No such element block");
//				} catch (Throwable e) {
//					System.out.println("Exception occured " + e);
//					throw e;
//				}
//			} else {
//				throw new RuntimeException("Element not found in loop");
//			}
//		}
//	}
//}
package blogic;

