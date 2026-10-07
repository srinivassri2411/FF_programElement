	package Mac;

	import java.io.PrintWriter;
	import java.io.StringWriter;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Map;

	import org.openqa.selenium.Keys;
	import org.openqa.selenium.interactions.Actions;

	import com.tyss.optimize.common.util.CommonConstants;
	import com.tyss.optimize.nlp.util.Nlp;
	import com.tyss.optimize.nlp.util.NlpException;
	import com.tyss.optimize.nlp.util.NlpRequestModel;
	import com.tyss.optimize.nlp.util.NlpResponseModel;
	import com.tyss.optimize.nlp.util.annotation.InputParam;
	import com.tyss.optimize.nlp.util.annotation.InputParams;

	import io.appium.java_client.mac.Mac2Driver;

	public class ZoomOperationsOnMac implements Nlp {

	    @InputParams({ 
	        @InputParam(name = "driver", type = "io.appium.java_client.mac.Mac2Driver"),
	        @InputParam(name = "operation", type = "java.lang.String"), // ZoomIn or ZoomOut
	        @InputParam(name = "numberOfTimes", type = "int") // Number of times to perform the operation
	    })

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
	        Mac2Driver driver = (Mac2Driver) attributes.get("driver");
	        String operation = (String) attributes.get("operation");
	        int numberOfTimes = (int) attributes.get("numberOfTimes");

	        // Validate the number of times to perform the operation
	        if (numberOfTimes <= 0) {
	            nlpResponseModel.setStatus(CommonConstants.fail);
	            nlpResponseModel.setMessage("Invalid number of times. It should be a positive integer.");
	            return nlpResponseModel;
	        }

	        try {
	            Actions actions = new Actions(driver);

	            for (int i = 0; i < numberOfTimes; i++) {
	                if ("ZoomIn".equalsIgnoreCase(operation)) {
	                    actions.keyDown(Keys.COMMAND).sendKeys(Keys.ADD).keyUp(Keys.COMMAND).perform();
	                } else if ("ZoomOut".equalsIgnoreCase(operation)) {
	                    actions.keyDown(Keys.COMMAND).sendKeys(Keys.SUBTRACT).keyUp(Keys.COMMAND).perform();
	                } else {
	                    throw new IllegalArgumentException("Invalid operation: " + operation);
	                }
	            }

	            nlpResponseModel.setStatus(CommonConstants.pass);
	            nlpResponseModel.setMessage("Successfully performed " + operation + " operation " + numberOfTimes + " times.");
	        } catch (Exception e) {
	            StringWriter sw = new StringWriter();
	            e.printStackTrace(new PrintWriter(sw));
	            String exceptionAsString = sw.toString();
	            nlpResponseModel.setStatus(CommonConstants.fail);
	            nlpResponseModel.setMessage("Failed to perform action: " + exceptionAsString);
	        }

	        return nlpResponseModel;
	    }

	    // Optional: You can add a main method here for standalone testing
	}


