package shadowUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.github.sukgu.Shadow;

public class VerifyTheShadowElementIsDisplayed implements Nlp {
	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement", findElement = false) })

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
		List<Map> eles = (List<Map>) nlpRequestModel.getAttributes().get("Element");
		WebElement element = null;
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			Shadow shadow = new Shadow(driver);
			for (Map ele : eles) {
				try {
					VerifyIsDisplayed(findShadowElement(ele, driver, shadow), driver);
					break;
				} catch (NoSuchElementException e) {
					throw new NoSuchElementException("Element is not found " + e + "=>" + eles);
				} catch (Exception e) {
				}
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Element is Displayed " + eles);
		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to check the displayed element " + exception.toString());
		}
		return nlpResponseModel;
	}

	public WebElement findShadowElement(Map ele, WebDriver driver, Shadow shadow) {
		String LocatorName = (String) ele.get("LocatorName");
		String LocatorValue = (String) ele.get("LocatorValue");
		if (LocatorName.toLowerCase().contains("xpath")) {
			return shadow.findElementByXPath(LocatorValue);
		} else if (LocatorName.toLowerCase().contains("cssSelector")) {
			return shadow.findElement(LocatorValue);
		} else {
			return null;
		}
	}

	public boolean VerifyIsDisplayed(WebElement element, WebDriver driver) {
		boolean isThere = element.isDisplayed();
		System.out.println(isThere);
		return isThere;
	}

//    public static void main(String[] args) throws InterruptedException {
//    WebDriver d = new ChromeDriver();
//    d.manage().window().maximize();
//
//    d.get("https://ctmstaging.cliniops.com");
//
//    new Scanner(System.in).next();
//
//    Shadow shadow = new Shadow(d);
//
//    VerifyTheShadowElementIsDisplayedUsingElement obj =
//            new VerifyTheShadowElementIsDisplayedUsingElement();
//
//    Map<String, String> map = new HashMap<>();
//    map.put("LocatorName", "xpath");
//    map.put("LocatorValue", "//button[@data-block-id='export-pdf']");
//
//    WebElement element = obj.findShadowElement(map, d, shadow);
//    obj.VerifyIsDisplayed(element, d);
//    System.out.println("end");
//}
}