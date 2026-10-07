package business_logics_Android;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.android.AndroidDriver;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class GetColour_FromImage implements Nlp {
    @InputParams({ @InputParam(name = "Xpath", type = "java.lang.String") })
    @ReturnType(name = "Color", type = "java.lang.String")
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String locatorValue = (String) programElementsInput.get("Xpath");
        String result = "";
        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
            // Your program element business logic starts here ...
            AndroidDriver driver = nlpRequestModel.getAndroidDriver();
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            WebElement elem = driver.findElement(By.xpath(locatorValue));

            int centerX = (elem.getLocation().getX() + elem.getSize().getWidth() / 2);
            int centerY = elem.getLocation().getY() + elem.getSize().getHeight() / 2;
            BufferedImage image = ImageIO.read(scrFile);
            // Getting pixel color by position x and y
            int clr = image.getRGB(centerX, centerY);
            int r = (clr & 0x00ff0000) >> 16;
            int g = (clr & 0x0000ff00) >> 8;
            int b = clr & 0x000000ff;
            log.info("Red=>"+ r +"");
            log.info("Green=>"+ g +"");
            log.info("Green=>"+ b +"");
            result = getBasicColorName(r, g, b);

            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Successfull in getting color ");
        } catch (Exception e) {
            // Your program element Exception handling goes here ...
            nlpResponseModel.setStatus(CommonConstants.fail);
            // Uncomment the below line and modify the message, only if you want to override
            // the fail message defined for the program element.
            // nlpResponseModel.setMessage("Failed to add numbers");
        }
        // Your program element business logic ends here ...
        nlpResponseModel.getAttributes().put("Color", result);
        return nlpResponseModel;
    }

    private static String getBasicColorName(int r, int g, int b) {
        Map<String, int[]> basicColors = new HashMap<>();
        basicColors.put("Red", new int[] { 255, 0, 0 });
        basicColors.put("Green", new int[] { 0, 128, 0 });
        basicColors.put("Blue", new int[] { 0, 0, 255 });
        basicColors.put("Yellow", new int[] { 255, 255, 0 });
        basicColors.put("Orange", new int[] { 255, 165, 0 });
        basicColors.put("Purple", new int[] { 128, 0, 128 });
        basicColors.put("Pink", new int[] { 255, 192, 203 });
        basicColors.put("Brown", new int[] { 139, 69, 19 });
        basicColors.put("Gray", new int[] { 128, 128, 128 });
        basicColors.put("Black", new int[] { 0, 0, 0 });
        basicColors.put("White", new int[] { 255, 255, 255 });
        basicColors.put("Cyan", new int[] { 0, 255, 255 });
        basicColors.put("Teal", new int[] { 0, 128, 128 });
        basicColors.put("Magenta", new int[] { 255, 0, 255 });
        basicColors.put("Navy", new int[] { 0, 0, 128 });
        basicColors.put("Maroon", new int[] { 128, 0, 0 });
        basicColors.put("Olive", new int[] { 128, 128, 0 });
        basicColors.put("Lime", new int[] { 0, 255, 0 });

        String closestColor = "Unknown";
        double minDistance = Double.MAX_VALUE;

        for (Map.Entry<String, int[]> entry : basicColors.entrySet()) {
            int[] rgb = entry.getValue();
            double distance = Math.sqrt(Math.pow(r - rgb[0], 2) + Math.pow(g - rgb[1], 2) + Math.pow(b - rgb[2], 2));
            if (distance < minDistance) {
                minDistance = distance;
                closestColor = entry.getKey();
            }
        }

        return closestColor;
    }

    public static void main(String[] args) {
        System.out.println(getBasicColorName(101, 101, 101));
    }
}