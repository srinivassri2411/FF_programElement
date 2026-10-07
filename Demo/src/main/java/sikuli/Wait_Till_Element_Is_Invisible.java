package sikuli;

import java.awt.image.BufferedImage;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;


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



public class Wait_Till_Element_Is_Invisible implements Nlp {
	@InputParams({@InputParam(name = "Image Stream", type = "java.io.InputStream"),@InputParam(name = "Wait Time", type = "java.lang.Integer")})

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
		InputStream imageStream = (InputStream) attributes.get("Image Stream");
		int WaitTime = (int) attributes.get("Wait Time");
		System.setProperty("java.awt.headless", "false");
		try
		{
			Thread.sleep(1000);
			BufferedImage bufferedImage = ImageIO.read(imageStream);
			Screen screen = new Screen();
			Pattern imagePattern = new Pattern(bufferedImage);
			screen.waitVanish(imagePattern, WaitTime);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully waited till element is invisible");	
		}
		catch(Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			//Log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Element is stil visible"+exceptionAsString);	
		}
		return nlpResponseModel;
	}
} 