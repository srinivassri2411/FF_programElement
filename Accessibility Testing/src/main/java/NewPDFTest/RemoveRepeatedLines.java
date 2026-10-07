package NewPDFTest;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class RemoveRepeatedLines implements Nlp {
	@InputParams({ @InputParam(name = "FilePath", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = (String) programElementsInput.get("FilePath");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
			String modifiedContent = content.replaceAll("(?m)^(\\s*\\n)+", ""); // remove extra lines
			Files.write(Paths.get(filePath), modifiedContent.getBytes(StandardCharsets.UTF_8));

			List<String> lines = new ArrayList<>();

			// Read all lines from the file
			try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
				String line;
				while ((line = reader.readLine()) != null) {
					lines.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(lines);
			List<String> line2 = new ArrayList<>();
			line2.addAll(lines);

			System.out.println(line2);

			String last = lines.get(lines.size() - 1);
			for (int i = lines.size() - 1; i >= 1; i--) {
//			String	dupString=lines.get(i);
				String org = lines.get(i - 1);
				if (!org.contains(last)) {
					break;
				} else {
					line2.remove(i);

				}
			}
			System.out.println(line2);
			// Write the modified lines back to the file
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
				for (String line : line2) {
					writer.write(line);
					writer.newLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			nlpResponseModel.setMessage("sucessfully removed repeated lines");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to remove repeated lines");

		}

		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel nl = new NlpRequestModel();
		nl.getAttributes().put("FilePath", "C:\\CIBC\\extractedPDFText.txt");
		RemoveRepeatedLines def = new RemoveRepeatedLines();
		def.execute(nl);
	}

}