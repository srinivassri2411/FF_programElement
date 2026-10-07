package Business_Logics;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.openqa.selenium.WebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;

public class CloseAllBrowsersOneByOneNew implements Nlp {

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			driver.getWindowHandles().forEach(handle -> {
				try {
					driver.switchTo().window(handle).close();
				} catch (Exception ignored) {
				}
			});
			Exception e = null;
			try {
				driver.quit();
			} catch (Exception e1) {
				e = e1;
			}
			nlpResponseModel.setMessage("Closed all browser sucessfully " + e);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception exception) {
			StringWriter writer = new StringWriter();
			exception.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to close all browser " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		return nlpResponseModel;
	}
}
