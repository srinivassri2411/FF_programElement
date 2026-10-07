package temp;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class SeleniumChromeTest {

    public static void main(String[] args) throws Exception {

        WebDriver driver = new ChromeDriver();

        Thread.sleep(2000);

        bringChromeToFront();

        driver.get("https://www.google.com");

        Thread.sleep(5000);

        driver.quit();
    }

    private static void bringChromeToFront() {

        final HWND[] chromeWindow = {null};

        User32.INSTANCE.EnumWindows((hwnd, data) -> {

            if (!User32.INSTANCE.IsWindowVisible(hwnd)) {
                return true;
            }

            char[] windowText = new char[512];

            User32.INSTANCE.GetWindowText(
                    hwnd,
                    windowText,
                    512
            );

            String title = new String(windowText).trim();

            if (title.contains("Chrome")) {

                chromeWindow[0] = hwnd;

                return false;
            }

            return true;

        }, null);

        HWND hwnd = chromeWindow[0];

        if (hwnd == null) {
            System.out.println("Chrome window not found");
            return;
        }

        System.out.println("Chrome HWND: " + hwnd);

        User32.INSTANCE.ShowWindow(
                hwnd,
                User32.SW_RESTORE
        );

        User32.INSTANCE.SetForegroundWindow(hwnd);
    }
}
