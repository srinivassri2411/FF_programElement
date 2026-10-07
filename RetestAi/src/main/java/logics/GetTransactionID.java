
package logics;

import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetTransactionID implements Nlp {
	@InputParams({ @InputParam(name = "timeoutinSeconds", type = "java.lang.String") })
	@ReturnType(name = "result", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String timeoutinSeconds = (String) programElementsInput.get("timeoutinSeconds");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = getTnxId(nlpRequestModel.getWebDriver(), Long.parseLong(timeoutinSeconds));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully fetched Transaction ID");
		} catch (Exception e) {
			throw new NlpException(e.getMessage());
		}
		nlpResponseModel.getAttributes().put("result", returnValue);
		return nlpResponseModel;
	}

	public static String getTnxId(WebDriver driver, long seconds) throws InterruptedException {
		JavascriptExecutor js = (JavascriptExecutor) driver;

		js.executeScript("(function() {" + "   window.__txnId = null;"
				+ "   const open = XMLHttpRequest.prototype.open;" + "   const send = XMLHttpRequest.prototype.send;"

				+ "   XMLHttpRequest.prototype.open = function(method, url) {" + "       this._url = url;"
				+ "       return open.apply(this, arguments);" + "   };"

				+ "   XMLHttpRequest.prototype.send = function(body) {"
				+ "       if (this._url && this._url.includes('/transactions/queryweb')) {" + "           try {"
				+ "               const json = JSON.parse(body);"
				+ "               window.__txnId = json.transactionid;"
				+ "               console.log('Intercepted TXN ID:', window.__txnId);"
				+ "           } catch (err) { console.log('Parse error:', err); }" + "       }"
				+ "       return send.apply(this, arguments);" + "   };" + "})();");

		System.out.println("XHR listener injected.");

		String txnId = null;
		for (int i = 0; i < seconds; i++) {
			txnId = (String) js.executeScript("return window.__txnId;");
			if (txnId != null && !txnId.isEmpty()) {
				break;
			}
			Thread.sleep(500);
		}

		js.executeScript("delete window.__txnId");

		if (txnId == null) {
			throw new NotFoundException("No transaction ID captured. API may not have triggered.");
		} else {
			return txnId;
		}

	}
}