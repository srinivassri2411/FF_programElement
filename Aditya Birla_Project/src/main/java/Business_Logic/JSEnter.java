package Business_Logic;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
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
import com.tyss.optimize.nlp.util.annotation.ReturnType;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Slf4j
public class JSEnter implements Nlp {
	private static final Logger log = LoggerFactory.getLogger(SuperCalculatorCheck.class);
@InputParams({ @InputParam(name = "WebElement", type = "org.openqa.selenium.WebElement"),@InputParam(name = "Text to Enter", type = "java.lang.String")})
// @ReturnType(name = "boolean", type = "java.lang.Boolean")
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
Boolean b=null;
WebElement ele = (WebElement) attributes.get("WebElement");
String textToEnter=(String) attributes.get("Text to Enter");
try {
WebDriver driver = nlpRequestModel.getWebDriver();
  String script =
            "var element = arguments[0];" +
            "var newValue = arguments[1];" +
            "element.value = newValue;" +
            "var event = new Event('input', { bubbles: true });" +
            "element.dispatchEvent(event);";
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript(script, ele, textToEnter);
        nlpResponseModel.setStatus(CommonConstants.pass);
nlpResponseModel.setMessage("Passed");

} catch (Exception e) {
StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
          //  log.info(exceptionAsString);
           nlpResponseModel.setStatus(CommonConstants.fail);
nlpResponseModel.setMessage("Failed "+exceptionAsString);

}
// nlpResponseModel.getAttributes().put("boolean",b );
return nlpResponseModel;
}
}