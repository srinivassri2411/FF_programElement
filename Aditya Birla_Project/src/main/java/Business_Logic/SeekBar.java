package Business_Logic;

import java.io.PrintWriter;

import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j

public class SeekBar implements Nlp {
	private static final Logger log = LoggerFactory.getLogger(SuperCalculatorCheck.class);
	@InputParams({ @InputParam(name = "slider", type = "org.openqa.selenium.WebElement"),
		 @InputParam(name = "optionToSelect", type = "java.lang.String"),
	 @InputParam(name = "optionsList", type = "java.util.List")})

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
		WebElement slider = (WebElement) attributes.get("slider");
		List<String> optionsList = (List) attributes.get("optionsList");
		String optionToSelect = (String) attributes.get("optionToSelect");
		WebDriver driver = nlpRequestModel.getWebDriver();
		Duration implicitWait = driver.manage().timeouts().getImplicitWaitTimeout();
		try {
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
			
			 Map<String, Integer> optionValueMap = new LinkedHashMap<>();
	            int stepSize = 2; 
	            for (int i = 0; i < optionsList.size(); i++) {
	                optionValueMap.put(optionsList.get(i), i * stepSize);
	            }

	           
	            if (!optionValueMap.containsKey(optionToSelect)) {
	                throw new IllegalArgumentException("Option '" + optionToSelect + "' is not present in the provided options list.");
	            }

	            
	            int sliderWidth = slider.getSize().getWidth();

	           
	            int desiredValue = optionValueMap.get(optionToSelect);

	            
	            double percentage = (double) desiredValue / (optionValueMap.get(optionsList.get(optionsList.size() - 1)));

	            
	            int xOffset = (int) (sliderWidth * percentage);
                Thread.sleep(2000);
	            
	            Actions actions = new Actions(driver);
	            actions.clickAndHold(slider).moveByOffset(xOffset - (sliderWidth / 2), 0).release().perform();

				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage(optionToSelect+" is Selected ");
			
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            log.info(exceptionAsString);
      	    nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Failed to select "+optionToSelect+" "+exceptionAsString);
			
		}
		finally {
			driver.manage().timeouts().implicitlyWait(implicitWait);
		}

		return nlpResponseModel;
	}
	

}