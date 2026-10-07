package Mac;
 
import java.awt.Robot;
import java.awt.event.InputEvent;
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
 
import io.appium.java_client.mac.Mac2Driver;
 
public class ClickOnElmentUsingLocation implements Nlp {
	@InputParams({ @InputParam(name = "driver", type = "io.appium.java_client.mac.Mac2Driver"),
			@InputParam(name = "Xpath", type = "java.lang.String") })
//	@ReturnType(name = "XCoordinate", type = "java.lang.Integer")
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
		String xpath = (String) attributes.get("Xpath");
		int xCoordinate=0;
		try {
			Thread.sleep(1000);
			WebElement element = driver.findElement(By.xpath(xpath));
			 xCoordinate = element.getLocation().getX();
			 int w = element.getRect().getWidth();
			 int h= element.getRect().getHeight();
			 int yCoordinate = element.getLocation().getY();
				System.setProperty("java.awt.headless", "false");
				Robot robot = new Robot();
				Thread.sleep(1000);
				robot.mouseMove(xCoordinate+w/2, yCoordinate+h/2);
				Thread.sleep(500);
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); // Left click
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully fetched click action" + xCoordinate+" : "+yCoordinate);
//			nlpResponseModel.getAttributes().put("xCoordinate", xCoordinate);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to perform action " + exceptionAsString);
		}
//		nlpResponseModel.getAttributes().put("XCoordinate", xCoordinate);
		return nlpResponseModel;
	}
}
