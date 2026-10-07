package parsing_utility;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class JSONToExcel implements Nlp {
    @InputParams({@InputParam(name = "Json File Stream", type = "java.io.InputStream")})
  

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
          InputStream stream = (InputStream) attributes.get("Json File Stream");
         
          System.setProperty("java.awt.headless", "false");
          try {
        	  list=List.of("\"\"","null");
        	  excelFileName="";
        	  excelFilePath="";
        	  keyCellIndex=0;
        	  valueCellIndex=2;
        	  labelCellIndex=1;
        	  cellCount=0;
        	  actCellCount=0;
        	  flag=true;
        	  sh=null;
        	  CellStyle styleNULL = null,styleHeader = null,styleValue=null;

        	  checkJson(stream);
        	  nlpResponseModel.setStatus(CommonConstants.pass);
  			nlpResponseModel.setMessage("Successfully Converted to JSON to Excel and stored at "+excelFilePath);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			//log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed Converted to JSON to Excel "+ exceptionAsString);
		}
          System.setProperty("java.awt.headless", "true");

        
          return nlpResponseModel;
      }
      
 static Map<String, List<String>> map=new LinkedHashMap<>();
	static List<String> list=List.of("\"\"","null");
	static String excelFileName;
	static String excelFilePath="";
	static int keyCellIndex=0;
	static int valueCellIndex=2;
	static int labelCellIndex=1;
	static int cellCount=0;
	static int actCellCount=0;
	boolean flag=true;
	Sheet sh=null;
	CellStyle styleNULL = null,styleHeader = null,styleValue=null;
	public static void checkJson(InputStream stream) throws Exception {
	 // Get your JSON input stream
		StringBuilder json = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
		    String line;
		    while ((line = reader.readLine()) != null) {
		    	json.append(line);
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		}
String initialJSON=json.toString();
Instant now = Instant.now();
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMHHmmss").withZone(ZoneOffset.systemDefault());
String timestamp = formatter.format(now);
String fileName = "Parsed_excel_" + timestamp + ".xlsx";
Path excelFileName = Path.of(System.getProperty("user.dir").replaceAll("\\\\", "/")+"/"+fileName);

excelFilePath=excelFileName.toString();
	
			long startTime = System.currentTimeMillis();
			
			if(initialJSON.getClass().toString().contains("String")){
				int cut = 0;
				for (int i = 0; i < initialJSON.length(); i++) {
					if (initialJSON.charAt(i) == '[' || initialJSON.charAt(i) == '{') {
						cut = i - 1;
						break;

					}
				}
				if(cut>=0)
					initialJSON = initialJSON.substring(cut);
			}
			String jsonContent=initialJSON;
			//String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)), StandardCharsets.UTF_8);
			String unescapedString = jsonContent.replace("\"{", "{").replace("}\"", "}").replace("\\\"", "\"");
			
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode root = objectMapper.readTree(unescapedString);
			
			extractKeyValue(root, "");
			////System.out.println(map);
			Map<String, List<String>> m=new TreeMap<>(map);
			////System.out.println(m);
			Map<String, List<String>> fm=new LinkedHashMap<>();
			for(Entry<String, List<String>> entry:m.entrySet()) {
				String index=entry.getKey().substring(0,3).replaceAll("[^\\d]", "");
				String spath=entry.getKey().replaceAll("\\[\\d+\\]", "[*]");
				 List<String> list = fm.get(spath);
			     if (list==null) list=new ArrayList<>();	
			            list.add(index+entry.getValue().toString());
				fm.put(spath, list);
	         }
			////System.out.println(fm);
			
			 writeInExcel(fm);
			

			double timeInSec=(System.currentTimeMillis()-startTime)/1000;
			//System.out.println("Total Execution Time ::::::: "+timeInSec+" sec");

			//System.out.println("Total Cell count ::::::: "+(actCellCount));
		
	}

	
	public static void extractKeyValue(JsonNode node, String path ) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                String newPath = path.isEmpty() ? key : path + "." + key;
                extractKeyValue(value, newPath);
            });
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                JsonNode arrayElement = node.get(i);
                extractKeyValue(arrayElement, path + "[" + i + "]");
            }
        } else {
            //System.out.println(path + ": " + node);
            String resultPath = path.substring(0,3)+path.substring(3).replaceAll("\\[\\d+\\]", "[*]");
            
            List<String> list = map.get(resultPath );
     if (list==null) list=new ArrayList<>();	
	
            list.add(node.toString());
            map.put(resultPath,list );
        }
    }
	
	
	
	
	public void writeInputPage(String excelFilePath, String sheetName, Map<String, List<String>> fmap) throws Exception {
		FileInputStream fis = new FileInputStream(excelFilePath);
        Workbook wb = new XSSFWorkbook(fis); 
    	  
    	   Sheet shPre = wb.createSheet(sheetName);
		
		CellStyle styleValue = wb.createCellStyle();
		styleValue.setBorderTop(BorderStyle.THIN);
		styleValue.setBorderBottom(BorderStyle.THIN);
		styleValue.setBorderLeft(BorderStyle.THIN);
		styleValue.setBorderRight(BorderStyle.THIN);
		
		CellStyle styleHeader=wb.createCellStyle();
		styleHeader.setBorderTop(BorderStyle.MEDIUM);
		styleHeader.setBorderBottom(BorderStyle.MEDIUM);
		styleHeader.setBorderLeft(BorderStyle.MEDIUM);
		styleHeader.setBorderRight(BorderStyle.MEDIUM);  
		
		Font font = wb.createFont();
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setBold(true);
		styleHeader.setFont(font);
		styleHeader.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		CellStyle styleNULL=wb.createCellStyle();
		styleNULL.setBorderTop(BorderStyle.THIN);
		styleNULL.setBorderBottom(BorderStyle.THIN);
		styleNULL.setBorderLeft(BorderStyle.THIN);
		styleNULL.setBorderRight(BorderStyle.THIN);
		styleNULL.setFillForegroundColor(IndexedColors.RED1.getIndex());
		styleNULL.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		int position=1;
		Cell a = shPre.createRow(0).createCell(0);
		a.setCellValue("Position");
		a.setCellStyle(styleHeader);
		Cell b = shPre.getRow(0).createCell(1);
		b.setCellValue("Label");
		b.setCellStyle(styleHeader);
		Cell c = shPre.getRow(0).createCell(2);
		c.setCellValue("JSON PATH");
		c.setCellStyle(styleHeader);
		for(Entry<String, List<String>> entry:fmap.entrySet()) {
			String key = entry.getKey(); 
			String[] labelArr = key.replace("[*]", "").split("\\.");
			String label=labelArr[labelArr.length-1];
			Cell x = shPre.createRow(position).createCell(0);
			x.setCellValue(position);
			x.setCellStyle(styleValue);
			Cell y = shPre.getRow(position).createCell(1);
			y.setCellValue(label);
			y.setCellStyle(styleValue);
			Cell z = shPre.getRow(position).createCell(2);
			z.setCellValue(key);
			z.setCellStyle(styleValue);
			position++;
		}
		wb.write(new FileOutputStream(excelFilePath));
		wb.close();
	}

	public Workbook openWorkBook(String excelFilePath, String sheetName,boolean flagSheet) throws Exception {
		 sh=null;
		   Workbook wb = null;
		  styleNULL = null;
		  styleHeader = null;
		  styleValue=null;
		  System.gc();
			FileInputStream fis = new FileInputStream(excelFilePath);
	         wb = new XSSFWorkbook(fis); 
	    	  
	    	 if(flagSheet) {
	    		 sh = wb.createSheet(sheetName);
	    	 }else {
	    		 sh = wb.getSheet(sheetName);
	    	 }
			
			 styleValue = wb.createCellStyle();
			styleValue.setBorderTop(BorderStyle.THIN);
			styleValue.setBorderBottom(BorderStyle.THIN);
			styleValue.setBorderLeft(BorderStyle.THIN);
			styleValue.setBorderRight(BorderStyle.THIN);
			
			 styleHeader=wb.createCellStyle();
			styleHeader.setBorderTop(BorderStyle.MEDIUM);
			styleHeader.setBorderBottom(BorderStyle.MEDIUM);
			styleHeader.setBorderLeft(BorderStyle.MEDIUM);
			styleHeader.setBorderRight(BorderStyle.MEDIUM);  
			
			Font font = wb.createFont();
			font.setColor(IndexedColors.BLACK.getIndex());
			font.setBold(true);
			styleHeader.setFont(font);
			styleHeader.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			 styleNULL=wb.createCellStyle();
			styleNULL.setBorderTop(BorderStyle.THIN);
			styleNULL.setBorderBottom(BorderStyle.THIN);
			styleNULL.setBorderLeft(BorderStyle.THIN);
			styleNULL.setBorderRight(BorderStyle.THIN);
			styleNULL.setFillForegroundColor(IndexedColors.RED.getIndex());
			styleNULL.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			return wb;
		
	}
	
	
public void writeOutputPage( String label, StringBuilder value) throws Exception {
	 
Cell headerCell,labelCell=null;
		
		try { 
			headerCell= sh.getRow(keyCellIndex).createCell(cellCount);
			}catch (Exception e) {
			 headerCell= sh.createRow(keyCellIndex).createCell(cellCount);
		}
		
		try {
			labelCell= sh.getRow(labelCellIndex).createCell(cellCount);
		}catch (Exception e) {
			labelCell= sh.createRow(labelCellIndex).createCell(cellCount);
		}
		
		headerCell.setCellValue(actCellCount+1);
		headerCell.setCellStyle(styleHeader);

		labelCell.setCellValue(label);
		labelCell.setCellStyle(styleHeader);
		//System.out.println(actCellCount+1);
		
			checkValueNullable(value,sh,styleValue,styleNULL);
		
	}
	public void saveData(Workbook wb,String excelFilePath) throws Exception {
		wb.write(new FileOutputStream(excelFilePath));
		wb.close();
	}
	
	
	public static void writeInExcel(Map<String, List<String>> fm) throws Exception {
		
		//excelFilePath=excelFilePath+excelFileName;
		new XSSFWorkbook().write(new FileOutputStream(excelFilePath));
		String sheet="Output";
		String sheetPre="Input";
		
		JSONToExcel fetchData=new JSONToExcel();
		fetchData.writeInputPage(excelFilePath, sheetPre, fm);
		
		int limiter=50000;
		int target=limiter;
		int count=0; 
		Workbook wb = null;
		for(Entry<String, List<String>> entry:fm.entrySet()) {
			String key = entry.getKey(); 
			StringBuilder value = new StringBuilder(entry.getValue().toString());
			String[] labelArr = key.replace("[*]", "").split("\\.");
			String label=labelArr[labelArr.length-1];
			
			if(cellCount==16382) {
				cellCount=0;
//				target=limiter;
				fetchData.saveData(wb,excelFilePath);
				count++;
				wb=null;
			}
			if (cellCount==0) {
				System.gc();
				wb=fetchData.openWorkBook(excelFilePath, sheet+count,true);
			}
			
				if(cellCount==target) {
				fetchData.saveData(wb,excelFilePath);
				wb=null;
				System.gc();
				wb=fetchData.openWorkBook(excelFilePath, sheet+count,false);
//				target+=limiter;
				}
			
		
			
			fetchData.writeOutputPage(label, value);
		cellCount++;
		actCellCount++;
		
		}
		
		fetchData.saveData(wb,excelFilePath);
		Desktop.getDesktop().open(new File(excelFilePath));
	}
	public void checkValueNullable(StringBuilder value, Sheet sh, CellStyle styleValue, CellStyle styleNull) {
		 List<String> arr = List.of(value.substring(1,value.length()-2).split("],"));
		////System.out.println(arr);
		 for (int i = 0; i < arr.size(); i++) {
			 List<String> innArr = List.of(arr.get(i).split("\\d+\\[")[1].split(","));
		////System.out.println(innArr);
			 for (int j = 0; j < innArr.size(); j++) {
		Cell valueCell=null;
		 String d = innArr.get(j).trim();
			try{
				valueCell=sh.getRow(valueCellIndex+j).createCell(cellCount);
			}catch (Exception e) {
				valueCell=sh.createRow(valueCellIndex+j).createCell(cellCount);
			}
			valueCell.setCellValue(d);
			if(!list.contains(d)) {
				valueCell.setCellStyle(styleValue);
			}else {
				valueCell.setCellStyle(styleNull);
			}
		}
		}
	}
}
