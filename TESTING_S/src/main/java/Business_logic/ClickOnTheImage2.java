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
//public class ClickOnTheImage2 implements Nlp {
//	BufferedWriter writer=null;
//	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement")})
////    @ReturnType(name = "colorString", type = "java.lang.String")
//
//	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
//
//		NlpResponseModel nlpResponseModel = new NlpResponseModel();
//		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
//		System.setProperty("java.awt.headless", "false");
//
////		WebElement element = (WebElement) programElementsInput.get("Element");
////		String imageContents = new String ((byte[]) programElementsInput.get(imageName));
////          String hex="";
//        List<Map.Entry<String, Object>> entryList = new ArrayList<>(programElementsInput.entrySet());
//
//        
//
//		try {
//			 File file = new File(System.getProperty("user.home")+File.separator+"varun.txt");
//		         writer = new BufferedWriter(new FileWriter(file));
//				writer.append(" Started On Elemnt\n");
//				writer.append(" List-----\n"+entryList);
//				String imageName =null;
//				for (int i = 0; i<entryList.size(); i++) {
//					writer.append(" list value----\n"+entryList.get(i).getKey()+"--key "+entryList.get(i).getValue()+"--value");
//					if ( entryList.get(i).getKey().toString().contains("png")) {
//						imageName= entryList.get(i).getKey();
//						break;
//					}
//				}
////				String imageName = (String) programElementsInput.get(entryList.get(1).getKey());
//
//				writer.append(" imageName----\n"+imageName);
//				
////				writer.append("This line will get element details \n" +element.getAttribute(attribute));
//				writer.append("This line will get map details \n" +nlpRequestModel.getAttributes().toString());
//				writer.append("This line will get map details---- \n" +programElementsInput);
//
//				writer.append("\n");
//				writer.append("This line will get just details \n" );
//				writer.append("\n");
//
//		        InputStream inputStream = new ByteArrayInputStream((byte[]) programElementsInput.get(imageName));
//		        BufferedImage bufferedImage = ImageIO.read(inputStream);
//				Screen screen = new Screen();
//				Pattern imagePattern = new Pattern(bufferedImage);
//				screen.click(imagePattern);
//			//			log.info("attribute keys are:---- ");
////			programElementsInput.forEach((key, value) -> log.info(key + " = " + value));
//			nlpResponseModel.setMessage("The Color is : ");
//			nlpResponseModel.setStatus(CommonConstants.pass);
////			writer.append(" fineshed ");
////	        writer.flush();
////	        writer.close();
//		} catch (Exception e) {
//			
//	       
//			StringWriter sw = new StringWriter();
//			e.printStackTrace(new PrintWriter(sw));
//			String exceptionAsString = sw.toString();
//			nlpResponseModel.setMessage("Failed to find color : " + exceptionAsString);
//			nlpResponseModel.setStatus(CommonConstants.fail);
//		}
//		finally {
//			try {
//				writer.append(" fineshed ");
//				 writer.flush();
//			        writer.close();
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//		}
//
//		// Your program element business logic ends here ...
////          nlpResponseModel.getAttributes().put("colorString", getColorName(hex));
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

