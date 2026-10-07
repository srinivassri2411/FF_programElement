package business_logics_Android;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;


public class Swipe_till_Element_is_Visible implements Nlp {
    @InputParams({@InputParam(name = "Xpath", type = "java.lan.String"),@InputParam(name = "Swipe count", type = "java.lang.Integer")})

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
        String xpath = (String) attributes.get("Xpath");
        int maxSwipeCount = (int) attributes.get("Swipe count");
        AndroidDriver androidDriver=null;
        IOSDriver iosDriver=null;
        try {
            androidDriver=nlpRequestModel.getAndroidDriver();
            iosDriver=nlpRequestModel.getIosDriver();
        } catch (Exception e) {
            iosDriver=nlpRequestModel.getIosDriver();
        }
        Duration impWait = androidDriver.manage().timeouts().getImplicitWaitTimeout();
        androidDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        
        try
        {
            if (androidDriver!=null) {
                org.openqa.selenium.Dimension screenSize = androidDriver.manage().window().getSize();//Identify screen dimension
                int screenCenter = (int) (screenSize.getWidth()*0.5);//Identify center point of screen for Y axis
                int startPoint = (int) (screenSize.getHeight()*0.9);//Identify beginning point of scroll for X axis
                int endPoint = (int) (screenSize.getHeight()*0.5);//Identify ending point of scroll
                int count = 0;    
                //            Integer maxSwipeCount =30;

                PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
                
               
                //Search Element until it is available on screen. If no, then perform below operations
                while (count<=maxSwipeCount) {
                	 try {
                         if ( androidDriver.findElement(By.xpath(xpath)).isDisplayed()==true) {
                             break;
                         }

                     } catch (Exception e) {
                         // TODO: handle exception
                     }
                    Sequence swipe =  new Sequence(finger, 1);

                    //Move finger into starting position
                    swipe.addAction(finger.createPointerMove(Duration.ofSeconds(0), PointerInput.Origin.viewport(), 474, 1727));


                    //Finger goes up into contact with screen
                    swipe.addAction(finger.createPointerDown(0));

                    //Finger moves to End Position
                    swipe.addAction(finger.createPointerMove(Duration.ofMillis(700), PointerInput.Origin.viewport(), 474, 1200));

                    //Take out finger from screen
                    swipe.addAction(finger.createPointerUp(0));

                    androidDriver.perform(Arrays.asList(swipe));
                    Thread.sleep(1000);
                   
                    count++;

                    System.out.println(count);
                }
            }
            
            else {
                org.openqa.selenium.Dimension screenSize = iosDriver.manage().window().getSize();//Identify screen dimension
                int screenCenter = (int) (screenSize.getWidth()*0.5);//Identify center point of screen for Y axis
                int startPoint = (int) (screenSize.getHeight()*0.7);//Identify beginning point of scroll for X axis
                int endPoint = (int) (screenSize.getHeight()*0.5);//Identify ending point of scroll
                int count = 0;    

                PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

            //Search Element until it is available on screen. If no, then perform below operations
            while (count<=maxSwipeCount) {
                new TouchAction(iosDriver)
                .press(PointOption.point(screenCenter, startPoint))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(500)))
                .moveTo(PointOption.point(screenCenter, endPoint))
                .release()
                .perform();
                Thread.sleep(2000);
                try {
                    if ( iosDriver.findElement(By.xpath(xpath)).isDisplayed()==true) {
                        break;
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                }
                count++;
                
                System.out.println(count);
            }
        }
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Successfully Swiped");  
        
        
        }
        catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            //Log.info(exceptionAsString);
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to perform action "+exceptionAsString);    
        }
        finally {
        	androidDriver.manage().timeouts().implicitlyWait(impWait);
        }
        return nlpResponseModel;
    }
}