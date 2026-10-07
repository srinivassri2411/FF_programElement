
package mainFrameUtilities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import org.tn5250j.Session5250;
import org.tn5250j.framework.common.SessionManager;
import org.tn5250j.framework.tn5250.Screen5250;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetScreenText implements Nlp {
	@InputParams({ @InputParam(name = "Screen", type = "org.tn5250j.framework.tn5250.Screen5250") })
	@ReturnType(name = "text", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Screen5250 screen = (Screen5250) programElementsInput.get("Screen");
//          String string = (String) programElementsInput.get("String to Enter");
//          Integer row = (Integer) programElementsInput.get("Row Index");
//          Integer column = (Integer) programElementsInput.get("Column Index");

		String text = "";
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			text = getScreenText(screen);
			   File tempFile = File.createTempFile("output_", ".png");
		        
		        String tempPNGPath = tempFile.getAbsolutePath();
			nlpResponseModel.setOutputLocalFilePaths(List.of(captureWindow("Fireflink AS400", tempPNGPath)));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage(text + " is the screen text");

		} catch (Exception e) {

			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));

			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to get data : " + sw);
		}

		nlpResponseModel.getAttributes().put("text", text);
		return nlpResponseModel;
	}
	public static String captureWindow(String partialTitle, String outputPath) throws Exception {

	    User32 USER32 = User32.INSTANCE;
	    GDI32 gDI32 = GDI32.INSTANCE;

	    // -----------------------------
	    // Find window by partial title
	    // -----------------------------
	    AtomicReference<HWND> found = new AtomicReference<>();

	    USER32.EnumWindows((hwnd, pointer) -> {

	        char[] buffer = new char[512];
	        USER32.GetWindowText(hwnd, buffer, 512);
	        String title = Native.toString(buffer);

	        if (title != null && title.contains(partialTitle) && USER32.IsWindowVisible(hwnd)) {
	            found.set(hwnd);
	            return false;
	        }

	        return true;

	    }, Pointer.NULL);

	    HWND hwnd = found.get();

	    if (hwnd == null) {
	        throw new RuntimeException("Window not found: " + partialTitle);
	    }

	    // -----------------------------
	    // Get window size
	    // -----------------------------
	    RECT rect = new RECT();
	    USER32.GetWindowRect(hwnd, rect);

	    int width = rect.right - rect.left;
	    int height = rect.bottom - rect.top;

	    // -----------------------------
	    // Create device contexts
	    // -----------------------------
	    HDC windowDC = USER32.GetDC(hwnd);
	    HDC memDC = gDI32.CreateCompatibleDC(windowDC);

	    HBITMAP hBitmap = gDI32.CreateCompatibleBitmap(windowDC, width, height);

	    WinNT.HANDLE old = gDI32.SelectObject(memDC, hBitmap);

	    // -----------------------------
	    // Capture window without focus
	    // -----------------------------
	    USER32.PrintWindow(hwnd, memDC, 0);

	    // -----------------------------
	    // Prepare bitmap structure
	    // -----------------------------
	    WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
	    bmi.bmiHeader.biWidth = width;
	    bmi.bmiHeader.biHeight = -height;
	    bmi.bmiHeader.biPlanes = 1;
	    bmi.bmiHeader.biBitCount = 32;
	    bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

	    Memory buffer = new Memory((long) width * height * 4);

	    gDI32.GetDIBits(
	            memDC,
	            hBitmap,
	            0,
	            height,
	            buffer,
	            bmi,
	            WinGDI.DIB_RGB_COLORS
	    );

	    int[] pixels = buffer.getIntArray(0, width * height);

	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    image.setRGB(0, 0, width, height, pixels, 0, width);

	    // -----------------------------
	    // Save screenshot
	    // -----------------------------
	    File file = new File(outputPath);
	    ImageIO.write(image, "png", file);

	    // -----------------------------
	    // Cleanup resources
	    // -----------------------------
	    gDI32.SelectObject(memDC, old);
	    gDI32.DeleteObject(hBitmap);
	    gDI32.DeleteDC(memDC);
	    USER32.ReleaseDC(hwnd, windowDC);
	    return file.getAbsolutePath();
	}
	public static String getScreenText(Screen5250 screen) {

		char[] buffer = screen.getCharacters();
		int rows = screen.getRows();
		int cols = screen.getColumns();

		StringBuilder sb = new StringBuilder();

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				sb.append(buffer[r * cols + c]);
			}
			sb.append("\n"); // new line after each row
		}
		System.out.println(sb.toString());
		return sb.toString();
	}
}