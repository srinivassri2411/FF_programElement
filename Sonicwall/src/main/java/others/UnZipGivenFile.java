
package others;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

public class UnZipGivenFile implements Nlp {
	@InputParams({ @InputParam(name = "Zip File", type = "java.lang.String") })
	@ReturnType(name = "Created File", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String zipFile = (String) programElementsInput.get("Zip File");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = unzipToTempFolder(zipFile);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Succesfully unzipped given file");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to unzip given file " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("Created File", returnValue);
		return nlpResponseModel;
	}

	public static String unzipToTempFolder(String zipFilePath) throws IOException {
		File tempDir = Files.createTempDirectory("FF_unzippedFiles_").toFile();
		try (ZipFile zipFile = new ZipFile(new File(zipFilePath))) {
			Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				File outFile = new File(tempDir, entry.getName());

				if (entry.isDirectory()) {
					outFile.mkdirs();
				} else {
					File parent = outFile.getParentFile();
					if (!parent.exists())
						parent.mkdirs();

					try (InputStream in = zipFile.getInputStream(entry);
							OutputStream out = new FileOutputStream(outFile)) {
						in.transferTo(out);
					}
				}
			}
		}
		return tempDir.getAbsolutePath() + File.separator;
	}
}