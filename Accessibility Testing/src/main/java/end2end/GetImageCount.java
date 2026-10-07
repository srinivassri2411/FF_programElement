
package end2end;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetImageCount implements Nlp {
	@InputParams({ @InputParam(name = "FilePath", type = "java.lang.String") })
	@ReturnType(name = "Summary", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = (String) programElementsInput.get("FilePath");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		StringBuilder summary = new StringBuilder();
		try {
			String input = new String(Files.readAllBytes(Paths.get(filePath)));
			int imageWithAltTextCount = 0;
			int imageWithAltTextAndLinkCount = 0;
			int imageWithLinkOnlyCount = 0;
			String[] lines = input.split("\\n");

			for (String line : lines) {
				boolean hasGraphic = line.contains("graphic");
				boolean hasLink = line.contains("link");

				if (hasGraphic && hasLink) {
					if (line.contains("webaim.org")) {
						imageWithLinkOnlyCount++;
					} else {

						imageWithAltTextAndLinkCount++;
					}
				} else if (hasGraphic) {
					imageWithAltTextCount++;
				} else if (hasLink) {
					imageWithLinkOnlyCount++;
				}
				
			}
			summary.append("Summary:,\n");
			summary.append("Images with alt text: ").append(imageWithAltTextCount).append(",\n");
			summary.append("Images with alt text and link: ").append(imageWithAltTextAndLinkCount).append(",\n");
			summary.append("Images with link only: ").append(imageWithLinkOnlyCount).append(",\n");
			int totalImages = imageWithAltTextCount + imageWithAltTextAndLinkCount + imageWithLinkOnlyCount;
			summary.append("Total images: ").append(totalImages).append("\n");

			nlpResponseModel.setMessage("Actul image count is " + totalImages);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to get count of image");
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("Summary", summary.toString());
		return nlpResponseModel;
	}

}