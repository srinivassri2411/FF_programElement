package canvas_Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Dimension;
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


public class DragAndDropObject implements Nlp {
	@InputParams({ @InputParam(name = "Canvas Element", type = "org.openqa.selenium.WebElement"),
		@InputParam(name = "Origin_x_y", type = "java.lang.String"),
		@InputParam(name = "Destination_x_y", type = "java.lang.String"),
		@InputParam(name = "OriginPadding_x_y", type = "java.lang.String"),
		@InputParam(name = "DestinationPadding_x_y", type = "java.lang.String") })

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
		WebElement canvasElement = (WebElement) attributes.get("Canvas Element");
		String origin_x_y = (String) attributes.get("Origin_x_y");
		String destination_x_y = (String) attributes.get("Destination_x_y");
		String originPadding_x_y = (String) attributes.get("OriginPadding_x_y");
		String destinationPadding_x_y = (String) attributes.get("DestinationPadding_x_y");

		// Your program element business logic goes here ...
		WebDriver driver = nlpRequestModel.getWebDriver();
		dragAndDropObject(driver, canvasElement, origin_x_y, destination_x_y, originPadding_x_y, destinationPadding_x_y);
		nlpResponseModel.setMessage("Drag and Drop operation is successful.");
		nlpResponseModel.setStatus(CommonConstants.pass);
		return nlpResponseModel;
	}

	private static void dragAndDropObject(WebDriver driver, WebElement canvasEle, String origin_x_y,
			String destination_x_y, String originPadding_x_y, String destinationPadding_x_y) {
		Actions act = new Actions(driver);

		String[] origin_posArr = origin_x_y.split(",");
		int orign_cordX = (int) Double.parseDouble(origin_posArr[0].toString());
		int orign_cordY = (int) Double.parseDouble(origin_posArr[1].toString());

		String[] dest_posArr = destination_x_y.split(",");
		int dest_cordX = (int) Double.parseDouble(dest_posArr[0].toString());
		int dest_cordY = (int) Double.parseDouble(dest_posArr[1].toString());

		String[] orignPadding_posArr = originPadding_x_y.split(",");
		int originPadding_cordX = (int) Double.parseDouble(orignPadding_posArr[0].toString());
		int originPadding_cordY = (int) Double.parseDouble(orignPadding_posArr[1].toString());

		String[] destPadding_posArr = destinationPadding_x_y.split(",");
		int destPadding_cordX = (int) Double.parseDouble(destPadding_posArr[0].toString());
		int destPadding_cordY = (int) Double.parseDouble(destPadding_posArr[1].toString());

		int startX = orign_cordX + originPadding_cordX;
		int startY = orign_cordY + originPadding_cordY;

		int endX = dest_cordX + destPadding_cordX;
		int endY = dest_cordY + destPadding_cordY;

		act.moveToElement(canvasEle, startX, startY).clickAndHold().moveToElement(canvasEle, endX, endY).release()
		.build().perform();

		Dimension size = canvasEle.getSize();
		act.moveToElement(canvasEle, -(size.width / 2), endY).click().perform();
	}

}