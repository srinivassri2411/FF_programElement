//
//package Business_logic;
//
//import java.awt.image.BufferedImage;
//import java.io.BufferedWriter;
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import javax.imageio.ImageIO;
//
//import org.sikuli.script.Pattern;
//import org.sikuli.script.Screen;
//
//import com.tyss.optimize.common.util.CommonConstants;
//import com.tyss.optimize.nlp.util.Nlp;
//import com.tyss.optimize.nlp.util.NlpException;
//import com.tyss.optimize.nlp.util.NlpRequestModel;
//import com.tyss.optimize.nlp.util.NlpResponseModel;
//import com.tyss.optimize.nlp.util.annotation.InputParam;
//import com.tyss.optimize.nlp.util.annotation.InputParams;
//
//
//public class ClickOnTheImage implements Nlp {
//	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement")})
//	//    @ReturnType(name = "colorString", type = "java.lang.String")
//
//	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
//
//		NlpResponseModel nlpResponseModel = new NlpResponseModel();
//		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
//		System.setProperty("java.awt.headless", "false");
//		List<Map.Entry<String, Object>> entryList = new ArrayList<>(programElementsInput.entrySet());
//		try {
//			String imageName =null;
//			for (int i = 0; i<entryList.size(); i++) {
//				if ( entryList.get(i).getKey().toString().contains("png")) {
//					imageName= entryList.get(i).getKey();
//					break;
//				}
//			}
//			InputStream inputStream = new ByteArrayInputStream((byte[]) programElementsInput.get(imageName));
//			BufferedImage bufferedImage = ImageIO.read(inputStream);
//			Screen screen = new Screen();
//			Pattern imagePattern = new Pattern(bufferedImage);
//			screen.click(imagePattern);
//			nlpResponseModel.setMessage("Successfully entered the input");
//			nlpResponseModel.setStatus(CommonConstants.pass);
//		} catch (Exception e) {
//			StringWriter sw = new StringWriter();
//			e.printStackTrace(new PrintWriter(sw));
//			String exceptionAsString = sw.toString();
//			nlpResponseModel.setMessage("Failed to find color : " + exceptionAsString);
//			nlpResponseModel.setStatus(CommonConstants.fail);
//		}
//		return nlpResponseModel;
//	}
//
//	@Override
//	public List<String> getTestParameters() throws NlpException {
//		List<String> params = new ArrayList<>();
//		return params;
//	}
//
//	@Override
//	public StringBuilder getTestCode() throws NlpException {
//		StringBuilder sb = new StringBuilder();
//		return sb;
//	}
//
//}
package Business_logic;

