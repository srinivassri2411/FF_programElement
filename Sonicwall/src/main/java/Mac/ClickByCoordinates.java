package Mac;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

public class ClickByCoordinates {
	static Robot robot=null;

	public static void main(String[] args) throws InterruptedException, FindFailed, IOException {
		try {
			Thread.sleep(2000); // Optional small delay
				Screen s=new Screen();
				ScreenImage screenshot = s.capture();

	            // Get the BufferedImage
	            BufferedImage image = screenshot.getImage();

	            // Save it as a PNG file
	            String outputPath = "/Users/sonicwall/Downloads/screenshot.png";
	            ImageIO.write(image, "png", new File(outputPath));
				Pattern p=new Pattern(args[0]);
				Pattern p1=new Pattern(args[1]);

				Match match = s.find(p);
				popup(match);

			try {
				Thread.sleep(500); // Optional small delay
				match=s.find(p);
				popup(match);
				Thread.sleep(300);
				findAndClick(s.find(p1));
				findAndClick(s.find(p1));
				
			} catch (Exception e) {
				// TODO: handle exception
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void copyToClipboard(String text) {
		StringSelection stringSelection = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
	}
	
	public static void popup(Match match) throws InterruptedException, AWTException {
		int x=(int)match.x;
		int y=(int)match.y;
		x=x+70;
		y=y+40;

		
	

	 robot = new Robot();
//	int	x=Integer.parseInt(args[0]);
//	int y=Integer.parseInt(args[1]);
	robot.mouseMove(x, y);
	robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); // Left click
	robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

	System.out.println("Clicked at coordinates: (" + x + ", " + y + ")");
	copyToClipboard("S()nicw@ll123");

	// Step 2: Simulate Cmd + V
	//            Robot robot = new Robot();
	robot.setAutoDelay(1000); // Short delay between events

	// Press Command (⌘) + V
	robot.keyPress(KeyEvent.VK_META);   // Command key on macOS
	robot.keyPress(KeyEvent.VK_V);
	robot.keyRelease(KeyEvent.VK_V);
	robot.keyRelease(KeyEvent.VK_META);

	System.out.println("Pasted text using Cmd + V");
	Thread.sleep(200); // Optional small delay

	// Press ENTER
	robot.keyPress(KeyEvent.VK_ENTER);
	robot.keyRelease(KeyEvent.VK_ENTER);
	}
	public static void findAndClick(Match match) throws InterruptedException, AWTException {
		int x=(int)match.x;
		int y=(int)match.y;
		x=x+70;
		y=y+40;

		
	

	 robot = new Robot();
//	int	x=Integer.parseInt(args[0]);
//	int y=Integer.parseInt(args[1]);
	robot.mouseMove(x, y);
	robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); // Left click
	robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

	System.out.println("Clicked at coordinates: (" + x + ", " + y + ")");
//	copyToClipboard("S()nicw@ll123");

	// Step 2: Simulate Cmd + V
	//            Robot robot = new Robot();
	robot.setAutoDelay(1000); // Short delay between events

	Thread.sleep(200); // Optional small delay

	// Press ENTER
	robot.keyPress(KeyEvent.VK_ENTER);
	robot.keyRelease(KeyEvent.VK_ENTER);
	Thread.sleep(200); // Optional small delay

	}
}

