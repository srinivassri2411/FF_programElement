package specializedAutomation2;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class GetCssValueFromShadowDOM implements Nlp {

    @InputParams({
        @InputParam(name = "Element", type = "org.openqa.selenium.WebElement"),
        @InputParam(name = "CssSelector", type = "java.lang.String"),
        @InputParam(name = "CssAttribute", type = "java.lang.String")
    })

    @ReturnType(name = "CssValue", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

        WebElement element = (WebElement) programElementsInput.get("Element");
        String cssSelector = (String) programElementsInput.get("CssSelector");
        String cssAttribute = (String) programElementsInput.get("CssAttribute");

        String returnValue = null;

        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {
            // Step 1: Get Shadow Root
            SearchContext shadowRoot = element.getShadowRoot();

            // Step 2: Find inner element
            WebElement innerElement = shadowRoot.findElement(By.cssSelector(cssSelector));

            // Step 3: Get CSS value
            returnValue = innerElement.getCssValue(cssAttribute);

            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Successfully fetched CSS value from Shadow DOM");

        } catch (Exception e) {
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to get CSS value: " + e.getMessage());
        }

        nlpResponseModel.getAttributes().put("CssValue", returnValue);
        return nlpResponseModel;
    }
}