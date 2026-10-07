package ssh_utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class FTP_from_local_to_server implements Nlp {
	@InputParams({ @InputParam(name = "Session", type = "com.jcraft.jsch.Session"),
		@InputParam(name = "Local File Path", type = "java.lang.String"),
		@InputParam(name = "Remote Directory", type = "java.lang.String") })
	@ReturnType(name = "UnixPath", type = "java.lang.String")

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

	private static String uploadFile(ChannelSftp channel, String localFilePath, String remoteDirectory) throws FileNotFoundException, SftpException {

		// Extract filename from localFilePath
		String localFileName = new File(localFilePath).getName();
		String remoteFilePath = remoteDirectory + localFileName;

		// Check if remote file exists
		try {
			channel.stat(remoteFilePath);
			// If the file exists, remove it before uploading the new file
			channel.rm(remoteFilePath);
		} catch (SftpException e) {
			// File doesn't exist, continue with upload
		}

		// Transfer file
		channel.put(new FileInputStream(localFilePath), remoteFilePath);

		System.out.println("File transfer successful!");
		return remoteFilePath;
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();

		Session session = (Session) attributes.get("Session");
		String localFilePath = (String) attributes.get("Local File Path");
		String remoteDirectory = (String) attributes.get("Remote Directory");
		ChannelSftp sftpChannel = null;
		String remotePath = "";

		// Check if local file exists
		File localFile = new File(localFilePath);
		if (!localFile.exists()) {
			nlpResponseModel.setMessage("Local file not found: " + localFilePath);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		try {
			// Create SFTP channel
			sftpChannel = (ChannelSftp) session.openChannel("sftp");
			sftpChannel.connect();

			// Upload a file to the remote server
			remotePath = uploadFile(sftpChannel, localFilePath, remoteDirectory);
			nlpResponseModel.setMessage("File Transfer is Successful");
			nlpResponseModel.setStatus(CommonConstants.pass);

		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to transfer the files.." + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
			e.printStackTrace();
		} finally {
			sftpChannel.disconnect();
		}
		nlpResponseModel.getAttributes().put("UnixPath", remotePath);
		return nlpResponseModel;
	}
}