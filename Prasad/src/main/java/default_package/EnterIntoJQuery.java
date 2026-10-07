
package default_package;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class EnterIntoJQuery implements Nlp {
    @InputParams({@InputParam(name = "Calendar WebElement", type = "org.openqa.selenium.WebElement"),
    	@InputParam(name = "Text to Enter", type = "java.lang.String")})
    

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          WebElement calendarWebElement = (WebElement) programElementsInput.get("Calendar WebElement");
          String texttoEnter = (String) programElementsInput.get("Text to Enter");
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	  setInputValueUsingJS(nlpRequestModel.getWebDriver(), calendarWebElement, texttoEnter);
              nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully entered text: " + texttoEnter);

          }
          catch(Exception e) {
        		StringWriter sw = new StringWriter();
    			e.printStackTrace(new PrintWriter(sw));
    			nlpResponseModel.setStatus(CommonConstants.fail);
    			nlpResponseModel.setMessage("Execution failed: " + sw.toString());
          }

          return nlpResponseModel;
      }
    public static void setInputValueUsingJS(
            WebDriver driver,
            WebElement element,
            String value) {

        JavascriptExecutor js = (JavascriptExecutor) driver;

        String script =
                "let input = arguments[0];" +
                "let value = arguments[1];" +

                "Object.getOwnPropertyDescriptor(" +
                "HTMLInputElement.prototype," +
                "'value'" +
                ").set.call(input, value);" +

                "input.dispatchEvent(new Event('input', { bubbles: true }));" +
                "input.dispatchEvent(new Event('change', { bubbles: true }));";

        js.executeScript(script, element, value);
    }
  } 