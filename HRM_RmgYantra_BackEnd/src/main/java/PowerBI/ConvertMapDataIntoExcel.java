package PowerBI;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j

public class ConvertMapDataIntoExcel implements Nlp {
	@InputParams({@InputParam(name = "Excel location to store", type = "java.lang.String"),
		@InputParam(name = "DB response", type = "java.lang.String")})

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
		String path = (String) attributes.get("Excel location to store");
//		String tableName = (String) attributes.get("Table name");
		String dataResponse = (String) attributes.get("DB response");
		try
		{
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet("data");
			//		XSSFSheet sheet = wb.createSheet("project");
//			String dataResponse="[{project_id=TY_PROJ_13812, created_by=Robert, created_on=01/08/2024, project_name=REBI_87358, status=Completed, team_size=0}, {project_id=TY_PROJ_13818, created_by=XYZ, created_on=01/08/2024, project_name=Demo44, status=, team_size=0}, {project_id=TY_PROJ_13819, created_by=Robert, created_on=01/08/2024, project_name=FireFlink_79865, status=Completed, team_size=0}, {project_id=TY_PROJ_13820, created_by=Robert, created_on=01/08/2024, project_name=MAngo_25518, status=Created, team_size=0}, {project_id=TY_PROJ_13821, created_by=Robert, created_on=01/08/2024, project_name=FireFlink_25154, status=Completed, team_size=0}, {project_id=TY_PROJ_13822, created_by=Deepak, created_on=01/08/2024, project_name=FireFlink_77797, status=Completed, team_size=1}, {project_id=TY_PROJ_13823, created_by=deepak, created_on=01/08/2024, project_name=Orange_12, status=Created, team_size=10}, {project_id=TY_PROJ_13824, created_by=deepak, created_on=01/08/2024, project_name=Orange_1, status=Created, team_size=10}, {project_id=TY_PROJ_13825, created_by=Likith, created_on=01/08/2024, project_name=Tiger, status=201, team_size=10}, {project_id=TY_PROJ_13826, created_by=null, created_on=01/08/2024, project_name=Orange123, status=null, team_size=0}, {project_id=TY_PROJ_13827, created_by=Mrv, created_on=01/08/2024, project_name=ABB, status=created, team_size=7}, {project_id=TY_PROJ_13828, created_by=Mrv, created_on=01/08/2024, project_name=ABB_1777, status=created, team_size=7}, {project_id=TY_PROJ_13829, created_by=Likith, created_on=01/08/2024, project_name=string, status=Created, team_size=0}]";
			String[] data = dataResponse.split("}");
			String[] datas=data[0].split(",");
			List header=new LinkedList<String>();
			XSSFRow row = sheet.createRow(0);
			for (int i = 0; i < datas.length; i++) {

				String[] value = datas[i].split("=");

				XSSFCell cell = sheet.getRow(0).createCell(i);
				cell.setCellValue(value[0].replace("[{", "").trim());
				//			header.add(value[0].replace("[{", "").trim());
			}
			for (int i = 1; i < data.length; i++) {
				XSSFRow createrow = sheet.createRow(i);
				System.out.println(data[0]);
				datas=data[0].split(",");
				for (int j = 0; j < datas.length; j++) {
					XSSFCell cell = createrow.createCell(j);
//					System.out.println(datas[j]);
					String[] v=datas[j].split("=");
					cell.setCellValue(v[1]);
					
				}
				//			System.out.println(data[0].split(","));
			}
			try (FileOutputStream fileOut = new FileOutputStream(path)) {
				wb.write(fileOut);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				wb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Data is successfully added to the excel");	
		}
		catch(Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			//Log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to convert "+exceptionAsString);	
		}
		return nlpResponseModel;
	}
} 