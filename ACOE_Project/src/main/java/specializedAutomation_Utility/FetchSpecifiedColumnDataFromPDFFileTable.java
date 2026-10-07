package specializedAutomation_Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class FetchSpecifiedColumnDataFromPDFFileTable implements Nlp {
	@InputParams({@InputParam(name = "PDF File Path", type = "java.lang.String"), @InputParam(name = "Page Number", type = "java.lang.Integer"), @InputParam(name = "Column Header", type = "java.lang.String")})
	@ReturnType(name = "listOfData", type = "java.util.List")

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
		String pdfFilePath = (String) attributes.get("PDF File Path");
		Integer tableNumber = (Integer) attributes.get("Page Number");
		String columnHeader = (String) attributes.get("Column Header");

		// Your program element business logic goes here ...
		List<String> listOfData = null;
		try {
			String wordFilePath = convertPdfToWord(pdfFilePath);
			listOfData = getDataFromWord(wordFilePath, tableNumber, columnHeader);
			nlpResponseModel.setMessage("Fetching data from PDF file is Successful...");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to Fetch data from PDF file..."+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("listOfData", listOfData);
		return nlpResponseModel;
	}


	private static String convertPdfToWord(String pdfFilePath) throws Exception, InterruptedException {

		// Set download directory
		String downloadPath = System.getProperty("java.io.tmpdir");
		HashMap<String, Object> chromePrefs = new HashMap<>();
		chromePrefs.put("download.default_directory", downloadPath);

		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", chromePrefs);
//		options.addArguments("--headless");
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setBrowserName("chrome");
		cap.setCapability(ChromeOptions.CAPABILITY, options);

		WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
		driver.get("https://www.ilovepdf.com/pdf_to_word");

		String fullPath = pdfFilePath;
		driver.findElement(By.xpath("//input[@type='file']")).sendKeys(pdfFilePath);
		Thread.sleep(2000);
		WebElement element = driver.findElement(By.id("processTask"));

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		wait.until(ExpectedConditions.visibilityOf(element));
		element.click();
		String fileFullPath = downloadPath + new File(fullPath).getName().replaceAll(".pdf", ".docx");
		System.out.println(fileFullPath);
		waitForDownload(fileFullPath);
		driver.close();
		return fileFullPath;
	}
	
	private static List<String> getDataFromWord(String wordFilePath, int tableNumber, String columnHeader) throws IOException {
		FileInputStream inputStream = new FileInputStream(wordFilePath);
		XWPFDocument document = new XWPFDocument(inputStream);
		List<XWPFTableRow> tableRows = document.getTables().get(tableNumber-1).getRows();
		List<String> listOfData = new LinkedList<>();
		int dataIndex = 0;
		for (int i = 0; i < tableRows.get(0).getTableCells().size(); i++) {
			if (tableRows.get(0).getTableCells().get(i).getText().trim().equalsIgnoreCase(columnHeader)) {
				dataIndex = i;
				break;
			}
		}
		for (int i = 1; i < tableRows.size(); i++) {
			listOfData.add(tableRows.get(i).getTableCells().get(dataIndex).getText().trim());
		}
		document.close();
		return listOfData;
	}

	private static void waitForDownload(String fullFilePath) throws IOException, InterruptedException {
		File file = new File(fullFilePath);
		while (true) {
			try {
				if (file.exists())
					break;
				else
					throw new Exception("Need Some time to download");
			} catch (Exception e) {
				Thread.sleep(1000);
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		nlpRequestModel.getAttributes().put("PDF File Path", "C:\\Users\\User\\Desktop\\Comparison\\Project Information.pdf");
		nlpRequestModel.getAttributes().put("Page Number", 1);
		nlpRequestModel.getAttributes().put("Column Header", "ProjectName");
		
		try {
			NlpResponseModel nlpResponseModel = new FetchSpecifiedColumnDataFromPDFFileTable().execute(nlpRequestModel);
			System.out.println(nlpResponseModel.getAttributes().get("listOfData"));
		} catch (NlpException e) {
			e.printStackTrace();
		}
	}
} 