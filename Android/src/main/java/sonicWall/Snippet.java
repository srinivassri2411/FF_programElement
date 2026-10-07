package sonicWall;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

public class Snippet {
	public static void bringUpFront() {
		String windowTitle = "Control Panel";

		WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);

		if (hwnd == null) {
			System.out.println("Window not found: " + windowTitle);
		} else {
			User32.INSTANCE.ShowWindow(hwnd, User32.SW_RESTORE);
			User32.INSTANCE.SetForegroundWindow(hwnd);
			System.out.println("Window brought to front: " + windowTitle);
		}
	}

	public static void maximize() {
		String windowTitle = "Control Panel";
		WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);

		if (hwnd == null) {
			System.out.println("Window not found: " + windowTitle);
		} else {
			User32.INSTANCE.ShowWindow(hwnd, User32.SW_MAXIMIZE);
			User32.INSTANCE.SetForegroundWindow(hwnd);
			System.out.println("Window maximized and brought to front: " + windowTitle);
		}
	}

	public static void main(String[] args) {
		bringUpFront();
		maximize();
	}

	
}
