package business_logics_Android;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.android.AndroidDriver;

public class ToClickByImageWithSubImage implements Nlp {
    @InputParams({ 
        @InputParam(name = "mainImageStream", type = "java.io.InputStream"),
        @InputParam(name = "subImageStream", type = "java.io.InputStream") 
    })
    @ReturnType(name = "ElementCoordinates", type = "java.lang.String")
    @Override
    public List<String> getTestParameters() throws NlpException {
        return new ArrayList<>();
    }

    @Override
    public StringBuilder getTestCode() throws NlpException {
        return new StringBuilder();
    }

    public static List<Integer> cooredinates(BufferedImage mainImage, BufferedImage subImage) {
        Pattern searchImage = new Pattern(mainImage).similar(0.9f);
        Finder objFinder = new Finder(subImage);
        objFinder.find(searchImage);
        int x = 0, y = 0;
        List<Integer> coordinates = new ArrayList<>();
        int counter = 0;
        while (objFinder.hasNext()) {
            Match objMatch = objFinder.next();
            x = objMatch.getX();
            y = objMatch.getY();
            int w = objMatch.getW();
            int h = objMatch.getH();
            System.out.println("Match found at: " + x + ", " + y + ", " + (x + w) + ", " + (y + h));
            counter++;
        }
        if (counter != 0) {
            coordinates.add(x);
            coordinates.add(y);
            System.out.println("Match Found!");
        }
        return coordinates;
    }

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        Map<String, Object> attributes = nlpRequestModel.getAttributes();
        
        InputStream mainImageStream = (InputStream) attributes.get("mainImageStream");
        InputStream subImageStream = (InputStream) attributes.get("subImageStream");
        
        String coordinatesString = null;
        try {
            AndroidDriver driver = nlpRequestModel.getAndroidDriver();
            BufferedImage mainImage = ImageIO.read(mainImageStream);
            BufferedImage subImage = ImageIO.read(subImageStream);
            
            List<Integer> coordinates = cooredinates(mainImage, subImage);
            if (coordinates.size() > 0) {
                int xCoordinate = coordinates.get(0) + (subImage.getWidth() / 2);
                int yCoordinate = coordinates.get(1) + (subImage.getHeight() / 2);

                int xVal = (xCoordinate * driver.manage().window().getSize().getWidth()) / mainImage.getWidth();
                int yVal = (yCoordinate * driver.manage().window().getSize().getHeight()) / mainImage.getHeight();

                PointerInput finger = new PointerInput(PointerInput.Kind.MOUSE, "finger");
                Sequence tapSequence = new Sequence(finger, 0);
                tapSequence.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), xVal, yVal));
                tapSequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
                tapSequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
                driver.perform(Arrays.asList(tapSequence));

                coordinatesString = xVal + "," + yVal;
                nlpResponseModel.setMessage("Successfully clicked on the given image");
                nlpResponseModel.setStatus(CommonConstants.pass);
            } else {
                nlpResponseModel.setMessage("Sub-image not found in the main image.");
                nlpResponseModel.setStatus(CommonConstants.fail);
            }
        } catch (Exception e) {
            nlpResponseModel.setMessage("Exception occurred while processing images: " + e.getMessage());
            nlpResponseModel.setStatus(CommonConstants.fail);
        }

        nlpResponseModel.getAttributes().put("ElementCoordinates", coordinatesString);
        return nlpResponseModel;
    }
}
