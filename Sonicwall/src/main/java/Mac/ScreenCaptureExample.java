package Mac;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ScreenCaptureExample {
    public static void main(String[] args) {
        try {
            // Create Robot instance
            Robot robot = new Robot();

            // Get screen size
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

            // Capture screenshot
            BufferedImage screenFullImage = robot.createScreenCapture(screenRect);

            // Save as PNG
            ImageIO.write(screenFullImage, "png", new File("./screenshot.png"));

            System.out.println("Screenshot captured and saved!");
        } catch (AWTException | java.io.IOException ex) {
            System.err.println("Error during screenshot capture: " + ex.getMessage());
        }
    }
}

