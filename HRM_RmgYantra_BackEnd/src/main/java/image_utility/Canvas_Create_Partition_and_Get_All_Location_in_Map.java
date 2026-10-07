package image_utility;


import java.io.PrintWriter;
import java.io.StringWriter;
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

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

public class Canvas_Create_Partition_and_Get_All_Location_in_Map implements Nlp {

	@InputParams({
		@InputParam(name="Canvas Element", type="org.openqa.selenium.WebElement"),
		@InputParam(name="Row Count", type="java.lang.String"),
		@InputParam(name="Column Count", type="java.lang.String"),
		@InputParam(name="Width Padding", type="java.lang.String"),
		@InputParam(name="Height Padding", type="java.lang.String")
	})
	@ReturnType(name = "Cooridinates", type = "java.util.Map")

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		Map<String, String> map=new LinkedHashMap<>();
		try {
			WebElement canvasElement = (WebElement) attributes.get("Canvas Element");
			String rowCountStr = (String) attributes.get("Row Count");
			String widthPaddingStr = (String) attributes.get("Width Padding");
			String heightPaddingStr = (String) attributes.get("Height Padding");
			String columnCountStr=(String) attributes.get("Column Count");
			map=partitionOfCanvasElement(canvasElement, widthPaddingStr, heightPaddingStr, rowCountStr, columnCountStr, map);
			nlpResponseModel.setMessage("Sucessfully Created Partition in Canvas Element");
			nlpResponseModel.setStatus(CommonConstants.pass);
		}catch (Exception e) {
			StringWriter sw=new StringWriter();
			PrintWriter pw=new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setMessage("Failed to Create canvas Partition ==> "+sw.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Cooridinates", map);
		return nlpResponseModel;
	}

	private Map<String, String> partitionOfCanvasElement(WebElement canvasElement, String widthPaddingstr,
			String heightPaddingstr, String rowCountstr, String columnCountstr, Map<String, String> map) {
		Dimension size = canvasElement.getSize();
		int widthPadding=Integer.parseInt(widthPaddingstr);
		int heightPadding=Integer.parseInt(heightPaddingstr);
		int rowCount=Integer.parseInt(rowCountstr);
		int columnCount=Integer.parseInt(columnCountstr);

		int canvasWidth = size.width-(widthPadding*2);
		int canvasHeight = size.height-(heightPadding*2);


		double smallestWidth=canvasWidth;
		double smallestHeight=canvasHeight;
		smallestWidth=smallestWidth/(columnCount-1);
		smallestHeight=smallestHeight/(rowCount-1);
		System.out.println(smallestWidth+","+smallestHeight);
		double x=widthPadding;
		double y=heightPadding;
		System.out.println(x+","+y);
		int countX=1;
		for (double i = 0; i <=canvasWidth; i+=smallestWidth) {
			int countY=1;
			for (double  j= 0; j <=canvasHeight; j+=smallestHeight) {
				x=Math.round(i-(canvasWidth/2.0));
				y=Math.round(j-(canvasHeight/2.0));
				map.put(countY+","+countX,(int)x+","+(int)y);
				countY++;
			} 
			countX++; 
		}
		return map;
	}
	
	
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
} 
