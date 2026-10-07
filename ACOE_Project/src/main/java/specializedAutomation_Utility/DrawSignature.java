package specializedAutomation_Utility;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.*;

public class DrawSignature implements Nlp {

    @InputParams({
        @InputParam(name = "Driver", type = "org.openqa.selenium.WebDriver"),
        @InputParam(name = "Signature Pad XPath/ID", type = "java.lang.String")
    })
    @ReturnType(name = "Signature Drawn", type = "java.lang.Boolean")

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> inputs = nlpRequestModel.getAttributes();
        WebDriver driver = (WebDriver) inputs.get("Driver");
        String locator = (String) inputs.get("Signature Pad XPath/ID");

        NlpResponseModel response = new NlpResponseModel();
        boolean success = false;

        try {
            // ===== Locate Signature Pad =====
            WebElement pad;
            if (locator.contains("/")) {
                pad = driver.findElement(By.xpath(locator));
            } else {
                pad = driver.findElement(By.id(locator));
            }

            Point padLocation = pad.getLocation();
            int startX = padLocation.getX();
            int startY = padLocation.getY();
            int width = pad.getSize().getWidth();
            int height = pad.getSize().getHeight();

            // Define margins to avoid edges
            int marginX = width / 8;
            int marginY = height / 5;

            int left = startX + marginX;
            int right = startX + width - marginX;
            int top = startY + marginY;
            int bottom = startY + height - marginY;

            Random rand = new Random();

            // ===== Generate Smoother Signature Points =====
            List<Point> points = new ArrayList<>();

            int currentX = left;
            int currentY = startY + height / 2;
            points.add(new Point(currentX, currentY));

            // Create 6–8 flowing strokes across pad
            for (int i = 0; i < 7; i++) {
                currentX += width / 8; // move right gradually
                currentY += (rand.nextInt(height / 3) - height / 6); // smooth vertical variation

                // keep inside pad
                currentX = Math.max(left, Math.min(currentX, right));
                currentY = Math.max(top, Math.min(currentY, bottom));

                points.add(new Point(currentX, currentY));
            }

            // ===== Draw Signature =====
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence sequence = new Sequence(finger, 0);

            // Move to first point & press
            sequence.addAction(finger.createPointerMove(Duration.ZERO,
                    PointerInput.Origin.viewport(),
                    points.get(0).getX(), points.get(0).getY()));
            sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));

            // Smooth moves along points
            for (int i = 1; i < points.size(); i++) {
                sequence.addAction(finger.createPointerMove(Duration.ofMillis(120),
                        PointerInput.Origin.viewport(),
                        points.get(i).getX(), points.get(i).getY()));
            }

            sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

            ((RemoteWebDriver) driver).perform(Collections.singletonList(sequence));

            success = true;
            response.setStatus(CommonConstants.pass);
            response.setMessage("Signature drawn successfully with smoother strokes.");

        } catch (Exception e) {
            response.setStatus(CommonConstants.fail);
            response.setMessage("Failed to draw signature: " + e.getMessage());
        }

        response.getAttributes().put("Signature Drawn", success);
        return response;
    }
}
