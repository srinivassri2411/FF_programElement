
package others;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

public class ConnectAndVerifyUDPListener implements Nlp {
	@InputParams({ @InputParam(name = "User Name", type = "java.lang.String"),
			@InputParam(name = "Host", type = "java.lang.String"),
			@InputParam(name = "Port", type = "java.lang.String"),
			@InputParam(name = "PEM FIle", type = "java.lang.String"),
			@InputParam(name = "Listen CMD", type = "java.lang.String"),
			@InputParam(name = "Local Target IP", type = "java.lang.String"),
			@InputParam(name = "UDP Port", type = "java.lang.String"),
			@InputParam(name = "Message List", type = "java.lang.String") })
	@ReturnType(name = "result", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String hostN = (String) programElementsInput.get("User Name");
		String host = (String) programElementsInput.get("Host");
		String port = (String) programElementsInput.get("Port");
		String pEMFIle = (String) programElementsInput.get("PEM FIle");
		String listenCMD = (String) programElementsInput.get("Listen CMD");
		String targetIp = (String) programElementsInput.get("Local Target IP");
		String udpPort = (String) programElementsInput.get("UDP Port");
		String msgList = (String) programElementsInput.get("Message List");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String remoteLogPath = null;
		try {
			String[] msgs = msgList.split(">>");
			connect(hostN, host, port, pEMFIle, listenCMD, targetIp, udpPort, msgs);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully verified the given texts using UDP");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel
					.setMessage("Failed to verify the given texts using UDP check logs in " + remoteLogPath + e);
		}
		nlpResponseModel.getAttributes().put("result", remoteLogPath);
		return nlpResponseModel;
	}

	private static String connect(String REMOTE_USER, String REMOTE_HOST, String SSH_PORT, String PEM_FILE,
			String REMOTE_LISTEN_CMD, String LOCAL_TARGET_IP, String UDP_PORT, String[] msgs) {
		Session session = null;
		ChannelExec remoteListener = null;
		Process localClient = null;
		File tempLog = null;
		String REMOTE_OUTPUT_DIR = "/home/" + REMOTE_USER + "/Outputmessages";
		String remoteLogPath = null;
		try {
			// Step 1: SSH connection
			session = connectSsh(REMOTE_USER, REMOTE_HOST, Integer.parseInt(SSH_PORT), PEM_FILE);
			System.out.println("SSH connected.");

			// Step 1a: Ensure remote output folder exists
			// Delete old folder (if exists) and recreate
			runRemoteOnce(session, "rm -rf " + REMOTE_OUTPUT_DIR + " && mkdir -p " + REMOTE_OUTPUT_DIR);
			System.out.println("Cleaned and recreated remote output directory: " + REMOTE_OUTPUT_DIR);

			// Create timestamped filename
			String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
			remoteLogPath = REMOTE_OUTPUT_DIR + "/messages_" + timestamp + ".txt";
			tempLog = File.createTempFile("remote_messages_", ".log");

			// Step 2: Start remote UDP listener
			PipedInputStream remoteOut = new PipedInputStream();
			PipedOutputStream remoteOutPipe = new PipedOutputStream(remoteOut);

			remoteListener = (ChannelExec) session.openChannel("exec");
			remoteListener.setCommand("bash -lc '" + REMOTE_LISTEN_CMD + "'");
			remoteListener.setInputStream(null);
			remoteListener.setErrStream(System.err);
			remoteListener.setOutputStream(remoteOutPipe);
			remoteListener.connect();
			System.out.println("Remote UDP listener started.");

			// Step 3: Start local ncat client
			localClient = new ProcessBuilder("ncat", "-u", LOCAL_TARGET_IP, String.valueOf(UDP_PORT))
					.redirectErrorStream(true).start();
			OutputStreamWriter clientWriter = new OutputStreamWriter(localClient.getOutputStream(),
					StandardCharsets.UTF_8);
			System.out.println("Local ncat client started.");

			Thread.sleep(2000); // wait to ensure listener is ready

			// Step 4: Send messages from local client

			for (String msg : msgs) {
				clientWriter.write(msg + "\n");
				clientWriter.flush();
				System.out.println("Sent from local: " + msg);
				Thread.sleep(500);
			}

			// Step 5: Read remote output, write to temp file with timestamp
			try (BufferedReader remoteReader = new BufferedReader(
					new InputStreamReader(remoteOut, StandardCharsets.UTF_8));
					BufferedWriter logWriter = new BufferedWriter(new FileWriter(tempLog))) {

				long deadline = System.currentTimeMillis() + 10_000;
				boolean[] seen = new boolean[msgs.length];

				while (System.currentTimeMillis() < deadline) {
					if (remoteReader.ready()) {
						String line = remoteReader.readLine();
						if (line != null) {
							String logLine = String.format("[%s] %s",
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), line);
							logWriter.write(logLine);
							logWriter.newLine();
							logWriter.flush();

							System.out.println("Remote received: " + logLine);

							for (int i = 0; i < msgs.length; i++) {
								if (!seen[i] && line.contains(msgs[i])) {
									seen[i] = true;
								}
							}
						}
					}
					if (allSeen(seen))
						break;
					Thread.sleep(100);
				}
				// Step 6: Upload temp file to remote directory
				uploadFile(session, tempLog, remoteLogPath);
				System.out.println("Logs saved to remote: " + remoteLogPath);
				if (allSeen(seen)) {
					System.out.println("Verified: all messages received.");
				} else {
					throw new RuntimeException("Timeout: not all messages were received by the remote host.");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (remoteListener != null && remoteListener.isConnected())
				remoteListener.disconnect();
			if (localClient != null)
				localClient.destroy();
			if (session != null && session.isConnected())
				session.disconnect();
			if (tempLog != null)
				tempLog.delete();
		}
		return remoteLogPath;
	}

	// ----------------- Helper Methods -----------------

	private static Session connectSsh(String user, String host, int port, String pemPath) throws JSchException {
		JSch jsch = new JSch();
		jsch.addIdentity(pemPath);
		Session session = jsch.getSession(user, host, port);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setServerAliveInterval(15_000);
		session.connect(20_000);
		return session;
	}

	private static void runRemoteOnce(Session session, String cmd) throws Exception {
		ChannelExec ch = (ChannelExec) session.openChannel("exec");
		ch.setCommand(cmd);
		ch.setInputStream(null);
		ch.setErrStream(System.err);
		ch.connect();
		while (!ch.isClosed())
			Thread.sleep(50);
		ch.disconnect();
	}

	private static void uploadFile(Session session, File localFile, String remotePath) throws Exception {
		String command = "scp -t " + remotePath;
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(command);

		OutputStream out = channel.getOutputStream();
		InputStream in = channel.getInputStream();
		channel.connect();

		if (checkAck(in) != 0)
			throw new IOException("SCP remote did not accept command.");

		// send "C0644 filesize filename"
		long filesize = localFile.length();
		String commandLine = "C0644 " + filesize + " " + new File(remotePath).getName() + "\n";
		out.write(commandLine.getBytes(StandardCharsets.UTF_8));
		out.flush();
		if (checkAck(in) != 0)
			throw new IOException("SCP remote did not accept file header.");

		// send file data
		try (FileInputStream fis = new FileInputStream(localFile)) {
			byte[] buf = new byte[1024];
			int len;
			while ((len = fis.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		}
		out.write(0); // send '\0'
		out.flush();
		if (checkAck(in) != 0)
			throw new IOException("SCP remote did not accept file data.");

		out.close();
		channel.disconnect();
	}

	private static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		if (b == 0 || b == -1)
			return b;
		if (b == 1 || b == 2) {
			StringBuilder sb = new StringBuilder();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			throw new IOException("SCP error: " + sb);
		}
		return b;
	}

	private static boolean allSeen(boolean[] arr) {
		for (boolean b : arr)
			if (!b)
				return false;
		return true;
	}
}