package Mac;

import java.awt.Robot;

import java.awt.event.InputEvent;

import java.io.PrintWriter;

import java.io.StringWriter;

import java.time.Duration;

import java.util.Map;

import org.openqa.selenium.By;

import org.openqa.selenium.Dimension;

import org.openqa.selenium.Point;

import org.openqa.selenium.WebElement;

import org.sikuli.script.FindFailed;

import com.tyss.optimize.common.util.CommonConstants;

import com.tyss.optimize.nlp.util.Nlp;

import com.tyss.optimize.nlp.util.NlpException;

import com.tyss.optimize.nlp.util.NlpRequestModel;

import com.tyss.optimize.nlp.util.NlpResponseModel;

import com.tyss.optimize.nlp.util.annotation.InputParam;

import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.mac.Mac2Driver;

@SuppressWarnings("deprecation")

public class SelectDropdownElement implements Nlp {

    @InputParams({

            @InputParam(name = "xpath", type = "java.lang.String"),

            @InputParam(name = "driver", type = "io.appium.java_client.mac.Mac2Driver")

    })

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

        String xpath = (String) programElementsInput.get("xpath");

        Mac2Driver driver = (Mac2Driver) programElementsInput.get("driver");

        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {

            System.setProperty("java.awt.headless", "false");

//              Mac2Options options=new Mac2Options();

//              options.setCapability("appium:automationName", "Mac2");

//              options.setCapability("platformName", "mac");

//              options.setCapability("appium:bundleId", "com.banyan.app");

//              options.setCapability("appium:newCommandTimeout", "0");

//               driver = new Mac2Driver(new URL("http://localhost:4723"),options);

//               Thread.sleep(8000);

//               List<WebElement> windows = driver.findElements(By.className("XCUIElementTypeWindow"));

//              System.out.println("Windows count: " + windows.size());

//              driver.findElement(By.xpath("//XCUIElementTypeStaticText[@value=\"1389-service-tunnel\"]")).click();

            WebElement el = driver.findElement(By.xpath(xpath));

            System.out.println(el.getLocation());

            Point loc = el.getLocation();

            Dimension size = el.getSize();

            // click in the center of the element

            int x = loc.getX() + size.getWidth() / 2;

            int y = loc.getY() + size.getHeight() / 2;

            Robot robot = new Robot();

            // Move the mouse

            robot.mouseMove(x, y);

            // small pause to make sure pointer is in place

            robot.delay(200);

            // Press & release left mouse button

            Runtime.getRuntime().exec(new String[] { "osascript", "-e", "tell application \"Banyan\" to activate" });

            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);

            robot.delay(100); // hold for 100 ms

            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            robot.delay(100); // hold for 100 ms
            try {
                 el = driver.findElement(By.xpath("//XCUIElementTypeOther"));
                 robot.mousePress(InputEvent.BUTTON1_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);

            } catch (Exception e) {
                // TODO: handle exception
            }

//            robot.mousePress(InputEvent.BUTTON1_MASK);
//                    
//            robot.mouseRelease(InputEvent.BUTTON1_MASK);

            robot.delay(200);

            nlpResponseModel.setWebDriver(driver);

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

            Thread.sleep(15000);

            nlpResponseModel.setStatus(CommonConstants.pass);

            nlpResponseModel.setMessage("Element clicked successfully!");

            System.out.println("done");

        }

        catch (Exception e) {

            StringWriter sw = new StringWriter();

            e.printStackTrace(new PrintWriter(sw));

            String exceptionAsString = sw.toString();

            nlpResponseModel.setMessage("Failed to click : " + exceptionAsString);

            nlpResponseModel.setStatus(CommonConstants.fail);

            System.out.println(exceptionAsString);

        }

        // Your program element business logic ends here ...

        nlpResponseModel.getAttributes().put("Sum of Two numbers", driver);

        return nlpResponseModel;

    }

    public static void main(String[] args) throws FindFailed, InterruptedException, NlpException {

        NlpRequestModel nlpResponseModel = new NlpRequestModel();

        Map<String, Object> programElementsInput = nlpResponseModel.getAttributes();

//        programElementsInput.put("Command", "sudo installer -pkg /Users/rahulkulkarni/Downloads/Banyan-3.22.2.pkg -target /");

        new SelectDropdownElement().execute(nlpResponseModel);

    }

}