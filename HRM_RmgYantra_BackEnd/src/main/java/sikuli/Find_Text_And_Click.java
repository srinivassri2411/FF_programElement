package sikuli;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.sikuli.script.Button;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j


public class Find_Text_And_Click implements Nlp {
	@InputParams({ @InputParam(name = "Text", type = "java.lang.String") })

	@Override
	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();
		return params;
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		StringBuilder sb = new StringBuilder();
		return sb;
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		//InputStream imageStream = (InputStream) attributes.get("Image Stream");
		//String region = (String) attributes.get("Region");
		String Text = (String) attributes.get("Text");

		//region = region.toLowerCase();

		System.setProperty("java.awt.headless", "false");
		try {
			Thread.sleep(1000);
			//BufferedImage bufferedImage = ImageIO.read(imageStream);
			Screen screen = new Screen();
			//Pattern imagePattern = new Pattern(bufferedImage);
			int a=0;
			while (a<5) {
				try {
					screen.existsText(Text).click();
					break;
				} catch (Exception e) {
					// TODO: handle exception
					screen.findText(Text).click();
				}
				a++;
			}
//			screen.findText(Text);
			if (a==5) {
//				StringWriter sw = new StringWriter();
//				e.printStackTrace(new PrintWriter(sw));
//				String exceptionAsString = sw.toString();
				//log.info(exceptionAsString);
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failed to find text and perform click operation ");
			}
			else {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully found text and performed click operation");
			}
			
//			screen.findText(Text);
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			//log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to find text and perform click operation " + exceptionAsString);
		}
		return nlpResponseModel;
	}
	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlp = new NlpRequestModel();
		Map<String, Object> attributes = nlp.getAttributes();
		attributes.put("Text", "calculate");
		Find_Text_And_Click c=new Find_Text_And_Click();
		c.execute(nlp);
	}
}
