package TCSPOC;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class CaptureScreenshotOfPDFSection implements Nlp {
	@InputParams({ @InputParam(name = "Pdf File Path", type = "java.lang.String"),
			@InputParam(name = "Pdf File Password", type = "java.lang.String"),
			@InputParam(name = "Width Of Section(cm)", type = "java.lang.Double"),
			@InputParam(name = "Height Of Section(cm)", type = "java.lang.Double"),
			@InputParam(name = "Section X location(cm)", type = "java.lang.Double"),
			@InputParam(name = "Section Y location(cm)", type = "java.lang.Double"),
			@InputParam(name = "Page Number", type = "java.lang.Integer"),
			@InputParam(name = "Folder Path", type = "java.lang.String") })
	@ReturnType(name = "filePath", type = "java.lang.String")

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

	public static int cmToPixels(double cm, int dpi) {
		return (int) Math.round(cm * dpi / 2.54);
	}

	public static void captureScreenshotFromPdf(String filePath, int pageNumber, int startX, int startY, int width,
			int height, String outputFileStoragePath, String PdfFilePassword) throws IOException {

		PDDocument document = null;
		if (PdfFilePassword.isEmpty()) {
			document = PDDocument.load(new File(filePath));

		} else {
			document = PDDocument.load(new File(filePath), PdfFilePassword);

		}

		PDPage page = document.getPage(pageNumber - 1);

		Rectangle rect = new Rectangle(startX, startY, width, height);

		PDFRenderer renderer = new PDFRenderer(document);
		BufferedImage image = renderer.renderImageWithDPI(pageNumber - 1, 300); // 300 is the DPI (dots per inch)

		BufferedImage croppedImage = image.getSubimage(rect.x, rect.y, rect.width, rect.height);

		File output = new File(outputFileStoragePath);
		ImageIO.write(croppedImage, "png", output);

		document.close();
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		Double width_cm = (Double) attributes.get("Width Of Section(cm)");
		Double height_cm = (Double) attributes.get("Height Of Section(cm)");
		Double startX_cm = (Double) attributes.get("Section X location(cm)");
		Double startY_cm = (Double) attributes.get("Section Y location(cm)");
		Integer PageNumber = (Integer) attributes.get("Page Number");
		String FolderPath = (String) attributes.get("Folder Path");
		String PdfFilePassword = (String) attributes.get("Pdf File Password");
		String PdfFilePath = (String) attributes.get("Pdf File Path");
		String filePath = null;
		try {
			int dpi = 300;
			int startX = cmToPixels(startX_cm, dpi);
			int startY = cmToPixels(startY_cm, dpi);
			int width = cmToPixels(width_cm, dpi);
			int height = cmToPixels(height_cm, dpi);
			String fileName = "PdfSection" + new Random().nextInt(100) + ".PNG";
			filePath = Paths.get(FolderPath, new String[] { fileName }).toString();
			captureScreenshotFromPdf(PdfFilePath, PageNumber, startX, startY, width, height, filePath, PdfFilePassword);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Fetched Signature");

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Fetch Signature");

		}
		nlpResponseModel.getAttributes().put("filePath", filePath);
		return nlpResponseModel;
	}
}