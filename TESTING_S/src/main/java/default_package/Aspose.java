
package default_package;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



import com.aspose.cells.Cells;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;


public class Aspose implements Nlp {
	    @InputParams({@InputParam(name = "string1", type = "java.lang.String")})
	//    @ReturnType(name = "string3", type = "java.lang.String")

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
		          String string1 = (String) attributes.get("string1");
		//          String string2 = (String) attributes.get("string2");
		try {
			String file="C:\\Users\\User\\Downloads\\iPru - Save N Grow calculator (GIFT V11+EzyGrow V05) (3).xlsx";
			Workbook wb=new Workbook(file);
			Worksheet sheet = wb.getWorksheets().get(0);
			Cells cell = sheet.getCells();
			cell.get(4, 2).putValue(string1);
			wb.calculateFormula();
			String outputPath = file;
			wb.save(outputPath, SaveFormat.XLSX);
			nlpResponseModel.setMessage("The Color is : ");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// TODO: handle exception
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			nlpResponseModel.setMessage("Failed to find color : " + exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		// Your program element business logic goes here ...

//		String string3 = "Return Value";
//		nlpResponseModel.getAttributes().put("string3", string3);
		return nlpResponseModel;
	}
} 