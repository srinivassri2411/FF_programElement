
package Business_Logics;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.sikuli.script.SikuliException;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.LoadLibs;

public class ClickUsingText2 implements Nlp {
	@InputParams({ @InputParam(name = "Text", type = "java.lang.String"),
			@InputParam(name = "Image Stream", type = "java.io.InputStream") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String Text = (String) programElementsInput.get("Text");
		InputStream imageStream = (InputStream) programElementsInput.get("Image Stream");
		System.setProperty("java.awt.headless", "false");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			BufferedImage bufferedImage = ImageIO.read(imageStream);

			ITesseract tesseract = new Tesseract();
			tesseract.setDatapath(LoadLibs.extractTessResources("tessdata").getAbsolutePath());
//			String extractedText = tesseract.doOCR(new File("C:\\Users\\user\\Downloads\\screenshot1.png"));
//			System.out.println("----++="+extractedText);
//Testing
			Screen sc = new Screen();
//			System.out.println( "-----"+sc.find(new Pattern("C:\\Users\\user\\Downloads\\screenshot1.png")).getText());
			Thread.sleep(3000);
			Iterator<Match> all = sc.findAll(new Pattern(bufferedImage));
			System.out.println(all.hasNext());
			int a = 1;
			while (all.hasNext()) {
				Match match = (Match) all.next();
				int x = match.x;
				int y = match.y;
				int w = match.w + 100;
				int h = match.h;
				Robot robot = new Robot();
				Rectangle screenRect = new Rectangle(x, y, w, h);
				BufferedImage screenshot = robot.createScreenCapture(screenRect);
//			            ImageIO.write(screenshot, "PNG", new File("C:\\\\Users\\\\user\\\\Downloads\\\\screenshot"+""+a+".png"));
//			            System.out.println(sc.find(new Pattern(screenshot)).getText());
//			             String outputFile = new File("C:\\\\Users\\\\user\\\\Downloads\\\\screenshot"+""+a+".png").getAbsolutePath();

//						System.out.println("---"+match.getText());
				String extractedText = tesseract.doOCR((screenshot)).trim();
				System.out.println(extractedText);
				if (extractedText.equals(Text)) {
					System.out.println("if==");
					sc.find(new Pattern(screenshot)).click();
				}

				a++;
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully clicked on text " + Text);

		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to click on text " + e);

		}

		// Your program element business logic ends here ...
		// nlpResponseModel.getAttributes().put("Result", returnValue);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException, SikuliException, InterruptedException {

		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		programElementsInput.put("Text", "skybeyo4545");
		new ClickUsingText2().execute(nlpRequestModel);
	}
}