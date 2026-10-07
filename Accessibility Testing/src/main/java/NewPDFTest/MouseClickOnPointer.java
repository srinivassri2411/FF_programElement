package NewPDFTest;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;




public class MouseClickOnPointer implements Nlp {

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
		try {
			System.setProperty("java.awt.headless", "false");
			Robot robot = new Robot();

			clickAtCurrentPosition(robot);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully clicked at the current mouse position.");

		} catch (Throwable e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to click on the current mouse position. " + e);
		}

		return nlpResponseModel;
	}

	public static void clickAtCurrentPosition(Robot robot) throws Throwable {

		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		Thread.sleep(500);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

}
