package qrcode;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class UpdateHtmlImage implements Nlp {

    @InputParams({
        @InputParam(name = "Html File Path", type = "java.lang.String"),
        @InputParam(name = "Image Name", type = "java.lang.String")
    })
    @ReturnType(name = "result", type = "java.lang.Boolean")   

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> input = nlpRequestModel.getAttributes();
        String htmlPath = (String) input.get("Html File Path");
        String imageName = (String) input.get("Image Name");

        NlpResponseModel response = new NlpResponseModel();
        boolean isUpdated = false;   

        try {
            // Read HTML content
            String html = new String(Files.readAllBytes(Paths.get(htmlPath)), StandardCharsets.UTF_8);

            // Update <img> tag fully
            html = html.replaceAll(
                "<img[^>]*id=\"barcodeImage\"[^>]*>",
                "<img id=\"barcodeImage\" src=\"./" + imageName + "\" alt=\"Barcode Image\" />"
            );

            // Update image name text
            html = html.replaceAll(
                "Image Name:\\s*[^<]*",
                "Image Name: " + imageName
            );

            // Write the updated HTML back to the file
            Files.write(Paths.get(htmlPath), html.getBytes(StandardCharsets.UTF_8));

            // If no exception → update success
            isUpdated = true;

            response.setStatus(CommonConstants.pass);
            response.setMessage("HTML updated and saved successfully!");

        } catch (Exception e) {
            response.setStatus(CommonConstants.fail);
            response.setMessage("Failed to update HTML: " + e);
        }

        // 🔥 Returning boolean instead of string
        response.getAttributes().put("result", isUpdated);
        return response;
    }

    public static void main(String[] args) {
        NlpRequestModel request = new NlpRequestModel();
        request.getAttributes().put("Html File Path", "C:\\Users\\User\\Downloads\\Walmart\\ImagePreviewTemplate.html");
        request.getAttributes().put("Image Name", "Pantene QR Code.png");

        UpdateHtmlImage generator = new UpdateHtmlImage();

        try {
            NlpResponseModel output = generator.execute(request);
            System.out.println("STATUS: " + output.getStatus());
            System.out.println("UPDATED: " + output.getAttributes().get("result")); // true/false
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
