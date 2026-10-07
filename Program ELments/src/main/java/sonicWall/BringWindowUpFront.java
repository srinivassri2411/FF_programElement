
package sonicWall;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

import org.openqa.selenium.NotFoundException;

public class BringWindowUpFront implements Nlp {
	@InputParams({ @InputParam(name = "Window Name", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String windowName = (String) programElementsInput.get("Window Name");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			boolean res1 = bringUpFront(windowName);
			Thread.sleep(1200);
			boolean res2 = maximize(windowName);
			if (res1 && res2) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Window maximized and brought to front: " + windowName);
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Window not maximized and brought to front: " + windowName);
			}
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed =>" + e.getMessage());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static boolean bringUpFront(String windowTitle) throws InterruptedException {
		WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
		if (hwnd == null) {
			throw new NotFoundException("Given " + windowTitle + " was not found");
		} else {
			User32.INSTANCE.ShowWindow(hwnd, User32.SW_RESTORE);
			Thread.sleep(1000);
			User32.INSTANCE.SetForegroundWindow(hwnd);
			return true;
		}
	}

	public static boolean maximize(String windowTitle) throws InterruptedException {
		WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);

		if (hwnd == null) {
			throw new NotFoundException("Given " + windowTitle + " was not found");
		} else {
			User32.INSTANCE.ShowWindow(hwnd, User32.SW_MAXIMIZE);
			Thread.sleep(1000);
			User32.INSTANCE.SetForegroundWindow(hwnd);
			System.out.println("Window maximized and brought to front: " + windowTitle);
			return true;
		}
	}
}