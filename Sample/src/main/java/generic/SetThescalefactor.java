package generic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP6c65b19e-ea90-4197-97a8-0a847720e8b2")
public class SetThescalefactor implements Nlp {
	@InputParams({ @InputParam(name = "Desired Capabilities", type = "org.openqa.selenium.remote.DesiredCapabilities"),
			@InputParam(name = "Zoom Percentage", type = "java.lang.String") })
	@ReturnType(name = "Result", type = "org.openqa.selenium.remote.DesiredCapabilities")

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
		DesiredCapabilities capabilities = (DesiredCapabilities) attributes.get("Desired Capabilities");
		String scaleFactor = (String) attributes.get("Zoom Percentage");

		try {
			capabilities = setZoom(capabilities, capabilities.getBrowserName(), scaleFactor);
			nlpResponseModel.setMessage("Successfully set the scale factor");
			nlpResponseModel.setStatus(CommonConstants.pass);

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setMessage("Failed to set ==> " + sw.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Result", capabilities);
		return nlpResponseModel;
	}

	public static DesiredCapabilities setZoom(DesiredCapabilities capabilities, String browserName, String resolution) {
		browserName = getCorrectedBrowserName(browserName);
		switch (browserName) {
		case "chrome":
			return capabilities.merge(new ChromeOptions().addArguments("--force-device-scale-factor=" + resolution));
		case "firefox":
			return capabilities.merge(new FirefoxOptions().addPreference("layout.css.devPixelsPerPx", resolution));
		case "MicrosoftEdge":
			System.out.println("block");
			return capabilities.merge(new EdgeOptions().addArguments("--force-device-scale-factor=" + resolution));
		default:
			throw new InvalidArgumentException("Invalid Browser");
		}
	}

	public static String getCorrectedBrowserName(String rawBrowserName) {
		String browserName = rawBrowserName.trim().toLowerCase().replace(" ", "").replace("_", "");
		if (StringUtils.equalsAny(browserName, new CharSequence[] { "chrome", "googlechrome" }))
			return "chrome";
		if (StringUtils.equalsAny(browserName, new CharSequence[] { "firefox", "mozillafirefox" }))
			return "firefox";
		if (StringUtils.equalsAny(browserName, new CharSequence[] { "edge", "msedge", "microsoftedge" }))
			return "MicrosoftEdge";
		if (StringUtils.equalsAny(browserName, new CharSequence[] { "safari" }))
			return "safari";
		return rawBrowserName;
	}
}