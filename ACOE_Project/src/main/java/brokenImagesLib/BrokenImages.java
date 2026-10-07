package brokenImagesLib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;


public class BrokenImages implements Nlp {

    WebDriver driver;

    @InputParams({ @InputParam(name = "ScreenShots Folder Path", type = "java.lang.String") })
    @Override
    public NlpResponseModel execute(NlpRequestModel request) throws NlpException {

        String folderPath = (String) request.getAttributes().get("ScreenShots Folder Path");

        // ✅ Auto-create directory if it does not exist
        File folderDir = new File(folderPath);
        if (!folderDir.exists()) {
            folderDir.mkdirs();
        }

        driver = request.getWebDriver();
        NlpResponseModel response = new NlpResponseModel();

        try {
            autoScrollToLoadLazyImages(500);

            List<BrokenImageInfo> brokenImages = detectBrokenImagesAndPlaceholder();

            if (brokenImages.isEmpty()) {
                response.setMessage("No broken images found.");
                response.setStatus(CommonConstants.pass);
                return response;
            }

            captureGroupedViewportScreenshots(brokenImages, "broken_images", folderPath);
            createExcelReport(brokenImages, folderPath);

            response.setMessage(
                    "Broken Images Found: " + brokenImages.size()
                            + " | Screenshots & Excel generated at: " + folderPath);
            response.setStatus(CommonConstants.pass);

        } catch (Exception e) {
            response.setMessage("Failed due to: " + e.getMessage());
            response.setStatus(CommonConstants.fail);
        }

        return response;
    }

    // ======================= DATA HOLDER =======================
    private static class BrokenImageInfo {
        WebElement element;
        String imageName;
        String imageUrl;
        String reason;
        int index;
    }

    // ======================= AUTO SCROLL =======================
    private void autoScrollToLoadLazyImages(long pauseMillis) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long height = ((Number) js.executeScript(
                "return Math.max(document.body.scrollHeight, document.documentElement.scrollHeight);")).longValue();
        long viewport = ((Number) js.executeScript("return window.innerHeight;")).longValue();

        for (long i = 0; i < height; i += viewport) {
            js.executeScript("window.scrollTo(0, arguments[0]);", i);
            try { Thread.sleep(pauseMillis); } catch (InterruptedException ignored) {}
        }
        js.executeScript("window.scrollTo(0,0);");
    }

    // ======================= DETECTION LOGIC =======================
    private List<BrokenImageInfo> detectBrokenImagesAndPlaceholder() {

        List<BrokenImageInfo> list = new ArrayList<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String pageUrl = driver.getCurrentUrl();
        int counter = 1;

        for (WebElement img : driver.findElements(By.tagName("img"))) {

            if (!img.isDisplayed()) {
                continue;
            }

            BrokenImageInfo info = new BrokenImageInfo();
            info.element = img;

            try {
                String src = img.getAttribute("src");
                String alt = img.getAttribute("alt");

                info.imageUrl = src;
                info.imageName =
                        (alt == null || alt.trim().isEmpty()) ? "Alt text not provided" : alt;

                if (src == null || src.trim().isEmpty()) {
                    info.reason = "Image src attribute missing";
                    info.index = counter++;
                    list.add(info);
                    continue;
                }

                try {
                    URL url = new URL(new URL(pageUrl), src);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    con.setRequestMethod("GET");
                    con.connect();

                    int status = con.getResponseCode();
                    String type = con.getContentType();
                    con.disconnect();

                    if (status >= 400) {
                        info.reason = "HTTP error " + status;
                        info.index = counter++;
                        list.add(info);
                        continue;
                    }

                    if (type == null || !type.toLowerCase().startsWith("image")) {
                        info.reason = "URL did not return image";
                        info.index = counter++;
                        list.add(info);
                        continue;
                    }

                } catch (IOException e) {
                    info.reason = "Network/timeout error";
                    info.index = counter++;
                    list.add(info);
                    continue;
                }

                Boolean broken = (Boolean) js.executeScript(
                        "return arguments[0].naturalWidth===0 || arguments[0].complete===false", img);

                if (Boolean.TRUE.equals(broken)) {
                    info.reason = "Image failed to render";
                    info.index = counter++;
                    list.add(info);
                }

            } catch (Exception e) {
                info.reason = "Browser validation error";
                info.index = counter++;
                list.add(info);
            }
        }
        return list;
    }

    // ======================= SCREENSHOTS =======================
    private List<File> captureGroupedViewportScreenshots(
            List<BrokenImageInfo> brokenList,
            String outputPrefix,
            String folder) throws IOException {

        List<File> files = new ArrayList<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        long viewportHeight =
                ((Number) js.executeScript("return window.innerHeight;")).longValue();

        Map<Integer, List<BrokenImageInfo>> groups = new TreeMap<>();

        for (BrokenImageInfo info : brokenList) {
            int top = info.element.getLocation().getY();
            int index = (int) (top / viewportHeight);
            groups.computeIfAbsent(index, k -> new ArrayList<>()).add(info);
        }

        int shot = 1;

        for (Map.Entry<Integer, List<BrokenImageInfo>> entry : groups.entrySet()) {

            js.executeScript("window.scrollTo(0, arguments[0]);",
                    entry.getKey() * viewportHeight);
            try { Thread.sleep(600); } catch (InterruptedException ignored) {}

            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            BufferedImage image = ImageIO.read(src);

            double dpr =
                    ((Number) js.executeScript("return window.devicePixelRatio || 1")).doubleValue();
            int scrollY =
                    ((Number) js.executeScript("return window.pageYOffset || 0")).intValue();

            Graphics2D g = image.createGraphics();
            g.setStroke(new BasicStroke((float) (3 * dpr)));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, (int) (14 * dpr)));

            for (BrokenImageInfo info : entry.getValue()) {

                Point p = info.element.getLocation();
                Dimension d = info.element.getSize();

                int x = (int) (p.getX() * dpr);
                int y = (int) ((p.getY() - scrollY) * dpr);
                int w = (int) (d.getWidth() * dpr);
                int h = (int) (d.getHeight() * dpr);

                g.setColor(new Color(255, 0, 0, 80));
                g.fillRect(x, y, w, h);
                g.setColor(Color.RED);
                g.drawRect(x, y, w, h);

                String label = "Broken #" + info.index;
                FontMetrics fm = g.getFontMetrics();
                g.setColor(Color.BLACK);
                g.fillRect(x, y - fm.getHeight(), fm.stringWidth(label) + 6, fm.getHeight());
                g.setColor(Color.WHITE);
                g.drawString(label, x + 3, y - 4);
            }

            g.dispose();

            File out = new File(folder + File.separator + outputPrefix + "_" + shot++ + ".png");
            ImageIO.write(image, "PNG", out);
            files.add(out);
        }

        return files;
    }

    // ======================= EXCEL =======================
    private void createExcelReport(List<BrokenImageInfo> list, String folder) throws IOException {

        Workbook wb = new XSSFWorkbook();
        Sheet sh = wb.createSheet("Broken Images");

        String[] headers = { "Sl No", "Image Name", "Image URL", "Broken Reason" };

        Font bold = wb.createFont();
        bold.setBold(true);
        CellStyle style = wb.createCellStyle();
        style.setFont(bold);

        Row h = sh.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell c = h.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(style);
        }

        int r = 1;
        for (BrokenImageInfo info : list) {
            Row row = sh.createRow(r++);
            row.createCell(0).setCellValue(info.index);
            row.createCell(1).setCellValue(info.imageName);
            row.createCell(2).setCellValue(info.imageUrl);
            row.createCell(3).setCellValue(info.reason);
        }

        for (int i = 0; i < headers.length; i++) sh.autoSizeColumn(i);

        try (FileOutputStream fos =
                     new FileOutputStream(new File(folder, "Broken_Images_Summary.xlsx"))) {
            wb.write(fos);
        }
        wb.close();
    }
	
	
//	public static void main(String[] args) {
//
//	    WebDriver driver = null;
//
//	    try {
//
//	        // ---------------- Launch Browser ----------------
////	        WebDriverManager.chromedriver().setup();
//
//	        ChromeOptions options = new ChromeOptions();
//	        options.addArguments("--start-maximized");
//
//	        driver = new ChromeDriver(options);
//
//	        // ---------------- Open Website ----------------
//	        driver.get("file:///C:/Users/User/Desktop/JavaScript/page.html");
//	        // Replace with your application URL
//
//	        // ---------------- Create NLP Request ----------------
//	        NlpRequestModel request = new NlpRequestModel();
//
//	        request.setWebDriver(driver);
//
//	        Map<String, Object> attributes = new HashMap<>();
//
//	        attributes.put(
//	                "ScreenShots Folder Path",
//	                "C:\\BrokenImagesReport");
//
//	        request.setAttributes(attributes);
//
//	        // ---------------- Execute NLP ----------------
//	        BrokenImages brokenImages = new BrokenImages();
//
//	        NlpResponseModel response = brokenImages.execute(request);
//
//	        // ---------------- Print Result ----------------
//	        System.out.println("--------------------------------");
//	        System.out.println("Status  : " + response.getStatus());
//	        System.out.println("Message : " + response.getMessage());
//	        System.out.println("--------------------------------");
//
//	    }
//	    catch (Exception e) {
//	        e.printStackTrace();
//	    }
//	    finally {
//
//	        if (driver != null) {
//	            driver.quit();
//	        }
//
//	    }
//
//	}
}
