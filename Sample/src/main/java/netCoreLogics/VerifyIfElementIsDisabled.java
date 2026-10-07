package netCoreLogics;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP9335eaf4-8aa7-4c90-8e3f-b505321818c0")
public class VerifyIfElementIsDisabled implements Nlp {
	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement") })
	@ReturnType(name = "Return", type = "java.lang.Boolean")
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
		WebElement element = (WebElement) attributes.get("Element");
		boolean isDisabled = false;
		try {
			isDisabled = checkElement(element);
			if (isDisabled) {
				nlpResponseModel.setMessage("Given element is disabled");
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else {
				nlpResponseModel.setMessage("Given element is not disabled");
				nlpResponseModel.setStatus(CommonConstants.fail);
			}

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setMessage("Failed to verify " + sw.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Return", isDisabled);
		return nlpResponseModel;
	}

	public boolean checkElement(WebElement element) {
		boolean isDisabled = false;
		String disabledAttribute = element.getAttribute("disabled");
		String ariaDisabled = element.getAttribute("aria-disabled");
		String readOnly = element.getAttribute("readonly");
		String classAttribute = element.getAttribute("class");
		String pointerEvents = element.getCssValue("pointer-events");
		String opacity = element.getCssValue("opacity");

		WebElement parentElement = element.findElement(By.xpath(".."));
		String parentDisabledAttribute = parentElement.getAttribute("disabled");

		if ((disabledAttribute != null && disabledAttribute.equals("true"))
				|| (ariaDisabled != null && ariaDisabled.equals("true"))
				|| (readOnly != null && readOnly.equals("true"))
				|| (classAttribute != null && classAttribute.contains("disabled"))
				|| (pointerEvents != null && pointerEvents.equals("none"))
				|| (opacity != null && Double.parseDouble(opacity) < 1.0)
				|| (parentDisabledAttribute != null && parentDisabledAttribute.equals("true"))
				|| (!element.isEnabled())) {
			isDisabled = true;
		}
		return isDisabled;
//		if (isDisabled) {
//			System.out.println("The element is disabled.");
//		} else {
//			System.out.println("The element is enabled.");
//		}
	}
}