package Business_Logics;

import java.io.StringReader;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.android.AndroidDriver;

public class New implements Nlp {
	@InputParams({ @InputParam(name = "tagName", type = "java.lang.String"),
			@InputParam(name = "locatorName", type = "java.lang.String"),
			@InputParam(name = "LocatorValue", type = "java.lang.String"),
			@InputParam(name = "AttributeName", type = "java.lang.String") })
	@ReturnType(name = "AttributeValue", type = "java.lang.String")

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
		String tagName = (String) attributes.get("tagName");
		String locatorName = (String) attributes.get("locatorName");
		String LocatorValue = (String) attributes.get("LocatorValue");
		String AttributeName = (String) attributes.get("AttributeName");
		String AttributeValue = "";

		try {
			 DesiredCapabilities capabilities = new DesiredCapabilities();
		        capabilities.setCapability("platformName", "Android");
		        capabilities.setCapability("appium:automationName", "UiAutomator2");
		        capabilities.setCapability("appium:appPackage", "com.okta.android.auth");
		        capabilities.setCapability("appium:appActivity", "com.okta.android.auth.activity.SplashActivity");
		        capabilities.setCapability("appium:noReset", "true");

		        // Initialize the AndroidDriver
		        AndroidDriver driver = new AndroidDriver(
		            new URL("http://localhost:4723"), capabilities);
		Thread.sleep(3000);
//			AndroidDriver driver = nlpRequestModel.getAndroidDriver();
			AttributeValue = extractOtpToken(driver, tagName, locatorName, LocatorValue, AttributeName);

			if (AttributeValue != null) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Attribute value is " + AttributeValue);
				System.out.println("Attribute value is " + AttributeValue);
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Attribute value is not found ");
			}
		}

		catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to capture attribute value " + e);
		}

		nlpResponseModel.getAttributes().put("AttributeValue", AttributeValue.replaceAll("\\s+", ""));
		return nlpResponseModel;
	}

	public static String extractOtpToken(AndroidDriver driver, String tagName, String locatorName, String LocatorValue,
			String AttributeName) throws Exception {

		String xmlHierarchy = "";
		while (!xmlHierarchy.contains("list_item_token_totp_text_view")) {
			xmlHierarchy = driver.getPageSource();
			System.out.println("recheck XML");
			Thread.sleep(1000);
		}
		System.out.println("XML captured");
		String xmlString = xmlHierarchy;
		Document document = parseXml(xmlString);
		if (document != null) {
			NodeList nodeList = document.getElementsByTagName(tagName);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);
				String locValue = element.getAttribute(locatorName);
				if (LocatorValue.equals(locValue)) {
					Node nextSibling = element.getNextSibling();
					// through siblings
					while (nextSibling != null) {
						if (nextSibling.getNodeType() == Node.ELEMENT_NODE) {
							Element siblingElement = (Element) nextSibling;
							if (siblingElement.getAttribute("resource-id").contains("list_item_token_totp_text_view")) {
								String totp = siblingElement.getAttribute(AttributeName);

								try {
									String time = siblingElement.getAttribute("content-desc");
									String[] timeWords = time.split("\\.")[1].trim().split(" ");

									int remainingSeconds = Integer.parseInt(timeWords[0]);
									if (remainingSeconds < 5) {
										System.out.println("Time is less than " + remainingSeconds
												+ " seconds, recapture the OTP!");
										xmlHierarchy = "";
										Thread.sleep(remainingSeconds * 1000);
										return extractOtpToken(driver, tagName, locatorName, LocatorValue,
												AttributeName);

									} else if (remainingSeconds >= 5) {
										System.out.println(
												"OTP is still valid. Remaining time: " + remainingSeconds + " seconds");
										System.out.println(" -->   " + totp);
										return totp;

									}
								} catch (Exception e) {
									return siblingElement.getAttribute(AttributeName);

								}

							}
						}
						nextSibling = nextSibling.getNextSibling();
					}
					return element.getAttribute(AttributeName);
				}
			}
		}
		return null;

	}

	public static Document parseXml(String xmlString) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlString));
			return builder.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static void main1(String[] args) throws Throwable {
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setCapability("appium:platformName", "Android");
		cap.setCapability("appium:DeviceName", "a13nnxxF");
		cap.setCapability("appium:appPackage", "com.okta.android.auth");
		cap.setCapability("appium:appActivity", "com.okta.android.auth.activity.SplashActivity");
		cap.setCapability("appium:noReset", "true");
		AndroidDriver driver = new AndroidDriver(new URL("http://localhost:4723/wd/hub"), cap);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(25));

		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		nlpRequestModel.setAndroidDriver(driver);

//		String accountname = "aamcfireflink@canada.testinator.com";
		String LocatorValue = "dev-68580665.okta.com. Shiva.Prasad@skybeyo.com.";
		nlpRequestModel.getAttributes().put("tagName", "android.widget.LinearLayout");
		nlpRequestModel.getAttributes().put("locatorName", "content-desc");
		nlpRequestModel.getAttributes().put("LocatorValue", LocatorValue);
		nlpRequestModel.getAttributes().put("AttributeName", "text");

		New er = new New();
		NlpResponseModel sed = er.execute(nlpRequestModel);
		String totp = (String) sed.getAttributes().get("AttributeValue");
		System.out.println("totop " + totp);
	}


}