package specializedAutomation_Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
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


public class FetchDataFromPDFTableAsListofList implements Nlp {
	@InputParams({@InputParam(name = "FileInputStream", type = "java.io.InputStream"), @InputParam(name = "TableNumber", type = "java.lang.Integer")})
	@ReturnType(name = "list", type = "java.util.List")

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
		FileInputStream fileInputStream = (FileInputStream) attributes.get("FileInputStream");
		Integer tableNumber = (Integer) attributes.get("TableNumber");

		// Your program element business logic goes here ...
		List<List<String>> list = null;
		try {
			// Get the path field using reflection
			Field pathField = FileInputStream.class.getDeclaredField("path");
			pathField.setAccessible(true);

			// Get the path value from the FileInputStream object
			String pdfFilePath = (String) pathField.get(fileInputStream);
			String absolutePath = convertpdfToWord(pdfFilePath);
			list = getDataFromWord(absolutePath, tableNumber);
			deleteFile(absolutePath);
			nlpResponseModel.setMessage("Fetching data from PDF file is Successful...");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to Fetch data from PDF file..."+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("list", list);
		return nlpResponseModel;
	}

	private static String convertpdfToWord(String pdfFilePath) throws Exception, InterruptedException {

		// Set download directory
		String downloadPath = System.getProperty("java.io.tmpdir");
		HashMap<String, Object> chromePrefs = new HashMap<>();
		chromePrefs.put("download.default_directory", downloadPath);

		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", chromePrefs);
		options.addArguments("--headless");
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

	private static List<List<String>> getDataFromWord(String absolutePath, int tableNumber) throws IOException {
		List<List<String>> wordData = new LinkedList<>();
		FileInputStream fis = new FileInputStream(absolutePath);
		XWPFDocument document = new XWPFDocument(fis);
		List<XWPFTableRow> tableRows = document.getTables().get(tableNumber-1).getRows();
		for (int i = 0; i < tableRows.size(); i++) {
			List<XWPFTableCell> tableCells = tableRows.get(i).getTableCells();
			List<String> word = new LinkedList<>();
			for (int j = 0; j < tableCells.size(); j++) {
				word.add(tableCells.get(j).getText());
			}
			wordData.add(word);
		}
		document.close();
		return wordData;
	}
	
	private static void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
			System.out.println("File Deleted Successfully");
		} else {
			System.out.println("File Not Found to Delete");
		}
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
} 