package canvas_Utility;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class DivideCanvasElementInPartitions implements Nlp {
	@InputParams({ @InputParam(name = "Canvas Element", type = "org.openqa.selenium.WebElement"),
		@InputParam(name = "Row Count", type = "java.lang.String"),
		@InputParam(name = "Column Count", type = "java.lang.String"),
		@InputParam(name = "Width Padding", type = "java.lang.String"),
		@InputParam(name = "Height Padding", type = "java.lang.String")})
	@ReturnType(name = "partitions", type = "java.util.List")

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
		String rowCount = (String) attributes.get("Row Count");
		String columnCount = (String) attributes.get("Column Count");
		String widthPadding = (String) attributes.get("Width Padding");
		String heightPadding = (String) attributes.get("Height Padding");

		// Your program element business logic goes here ...
		Map<String, String> partitions = partitionOfCanvasElement(canvasElement, rowCount, columnCount, widthPadding, heightPadding);
		nlpResponseModel.getAttributes().put("partitions", partitions);
		nlpResponseModel.setMessage("Divided given canvas element.");
		nlpResponseModel.setStatus(CommonConstants.pass);
		return nlpResponseModel;
	}

	private static Map<String, String> partitionOfCanvasElement(WebElement canvasElement, String rowCountStr, String columnCountStr, String widthPaddingStr,
			String heightPaddingStr) {
		Map<String, String> map = new LinkedHashMap<>();

		Dimension size = canvasElement.getSize();
		int widthPadding = Integer.parseInt(widthPaddingStr);
		int heightPadding = Integer.parseInt(heightPaddingStr);

		int canvasWidth = size.width - (widthPadding * 2);
		int canvasHeight = size.height - (heightPadding * 2);

		int rowCount = Integer.parseInt(rowCountStr);
		int columnCount = Integer.parseInt(columnCountStr);

		double smallestWidth = canvasWidth;
		double smallestHeight = canvasHeight;
		smallestWidth = smallestWidth / (columnCount - 1);
		smallestHeight = smallestHeight / (rowCount - 1);
		System.out.println(smallestWidth + "," + smallestHeight);
		double x = widthPadding;
		double y = heightPadding;
		System.out.println(x + "," + y);
		int countX = 1;
		for (double i = 0; i <= canvasWidth; i += smallestWidth) {
			int countY = 1;
			for (double j = 0; j <= canvasHeight; j += smallestHeight) {
				x = Math.round(i - (canvasWidth / 2.0));
				y = Math.round(j - (canvasHeight / 2.0));
				map.put(countY + "," + countX, (int) x + "," + (int) y);
				countY++;
			}
			countX++;
		}
		return map;
	}

}