package legistify;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.*;
import java.nio.file.Files;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

public class UnZipGivenFile implements Nlp {

	@InputParams({ @InputParam(name = "Zip File", type = "java.lang.String"),
			@InputParam(name = "Extract Type", type = "java.lang.String") })
	@ReturnType(name = "Created File", type = "java.util.List")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String zipFile = (String) programElementsInput.get("Zip File");
		String extractType = (String) programElementsInput.get("Extract Type");

		List<String> returnValue = null;
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		try {
			returnValue = unzipToTempFolder(zipFile, extractType);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully unzipped given file");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to unzip given file " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("Created File", returnValue);
		return nlpResponseModel;
	}

	private static final String ILLEGAL_CHARS = "[\\\\:*?\"<>|]"; // Note: removed '/' from here

	public static List<String> unzipToTempFolder(String zipFilePath, String extractType) throws IOException {
		File tempDir = Files.createTempDirectory("FF_unzippedFiles_").toFile();
		List<String> extractedFilePaths = new ArrayList<>();
		List<String> extractedFolderPaths = new ArrayList<>();

		try (ZipFile zipFile = new ZipFile(new File(zipFilePath))) {
			Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();

				// Split entry name by '/' to preserve folder structure
				String[] parts = entry.getName().split("/");
				for (int i = 0; i < parts.length; i++) {
					parts[i] = parts[i].replaceAll(ILLEGAL_CHARS, "_");
				}

				// Rebuild path preserving folder hierarchy
				File outFile = new File(tempDir, String.join(File.separator, parts));

				if (entry.isDirectory()) {
					outFile.mkdirs();
					extractedFolderPaths.add(outFile.getAbsolutePath());
				} else {
					File parent = outFile.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}

					try (InputStream in = zipFile.getInputStream(entry);
							OutputStream out = new FileOutputStream(outFile)) {
						in.transferTo(out);
					}

					extractedFilePaths.add(outFile.getAbsolutePath());
				}
			}
		}

		if ("folders".equalsIgnoreCase(extractType)) {
			return extractedFolderPaths;
		}

		return extractedFilePaths;
	}

	public static void main(String[] args) throws NlpException, IOException {
//		unzipToTempFolder("C:\\Users\\User\\Downloads\\Case-Documents-95b5d129-33a1-4565-b1f1-7b335b7eb05c.zip",
//				"files");
		NlpRequestModel mode = new NlpRequestModel();

		mode.getAttributes().put("Zip File",
				"C:\\Users\\User\\Downloads\\Case-Documents-ed18e3e9-3a4f-42ed-b381-0bd8d8d523bf.zip");
		mode.getAttributes().put("Extract Type", "files");
		System.out.println(new UnZipGivenFile().execute(mode));

		mode.getAttributes().put("Extract Type", "folders");
		System.out.println(new UnZipGivenFile().execute(mode));
	}
}