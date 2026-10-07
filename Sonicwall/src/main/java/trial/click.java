package trial;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;


import org.sikuli.script.FindFailed;

public class click {

	public static void main(String[] args) throws FindFailed, InterruptedException, AWTException {
		// TODO Auto-generated method stub
//Screen s=new Screen();
//Pattern p=new Pattern("/Users/sonicwall/Desktop/Screenshot 2025-07-29 at 4.31.02 PM.png"
//		+ "");
//s.find(p).click();
		Robot robot = new Robot();

        // Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;

        // Calculate 50% X and 25% Y
        int x = (int) (width * 0.5);
        int y = (int) (height * 0.25);

        // Move the mouse to the calculated position
        robot.mouseMove(x, y);
        Thread.sleep(500);  // Small pause for visibility

        // Perform mouse click (left click)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(500);

        // Press and release Enter key
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);

	}

}
