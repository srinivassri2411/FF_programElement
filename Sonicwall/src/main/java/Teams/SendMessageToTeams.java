package Teams;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.requests.GraphServiceClient;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import okhttp3.Request;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SendMessageToTeams implements Nlp {

	@InputParams({ @InputParam(name = "Client_ID", type = "java.lang.String"),
			@InputParam(name = "Client_Secret", type = "java.lang.String"),
			@InputParam(name = "Tenant_ID", type = "java.lang.String"),
			@InputParam(name = "Team_ID", type = "java.lang.String"),
			@InputParam(name = "Channel_Folder_Name", type = "java.lang.String"), // dynamic channel folder
			@InputParam(name = "Webhook_URL", type = "java.lang.String"),
			@InputParam(name = "Message", type = "java.lang.String"),
			@InputParam(name = "SharePoint_File_Link", type = "java.lang.String"), // optional — already uploaded SP
																					// link
			@InputParam(name = "Report_FilePath", type = "java.lang.String") // optional — local report to upload
	})
	@ReturnType(name = "Status", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String client_ID = (String) programElementsInput.get("Client_ID");
		String client_Secret = (String) programElementsInput.get("Client_Secret");
		String tenant_ID = (String) programElementsInput.get("Tenant_ID");
		String team_ID = (String) programElementsInput.get("Team_ID");
		String channel_FolderName = (String) programElementsInput.get("Channel_Folder_Name");
		String webhook_URL = (String) programElementsInput.get("Webhook_URL");
		String message = (String) programElementsInput.get("Message");
		String sharePoint_FileLink = (String) programElementsInput.get("SharePoint_File_Link");
		String report_FilePath = (String) programElementsInput.get("Report_FilePath");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String returnValue = null;

		// Validate mandatory fields
		if (webhook_URL == null || webhook_URL.trim().isEmpty()) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel
					.setMessage("Webhook_URL is missing or null. Please provide a valid Teams Incoming Webhook URL.");
			return nlpResponseModel;
		}
		if (channel_FolderName == null || channel_FolderName.trim().isEmpty()) {
			System.out.println("⚠️ Channel_Folder_Name not provided — file will be uploaded to drive root.");
		}

		try {
			returnValue = sendToTeams(client_ID, client_Secret, tenant_ID, team_ID, channel_FolderName, webhook_URL,
					message, sharePoint_FileLink, report_FilePath);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Message sent to Teams successfully");
		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to send message to Teams: " + e);
		}

		nlpResponseModel.getAttributes().put("Status", returnValue);
		System.out.println("Status: " + returnValue);
		return nlpResponseModel;
	}

	public static String sendToTeams(String CLIENT_ID, String CLIENT_SECRET, String TENANT_ID, String TEAM_ID,
			String CHANNEL_FOLDER_NAME, String WEBHOOK_URL, String MESSAGE_TEXT, String SHAREPOINT_FILE_LINK,
			String REPORT_FILE_PATH) throws Exception {

		String uploadedReportUrl = null;
		String uploadedReportName = null;

		if (REPORT_FILE_PATH != null && !REPORT_FILE_PATH.trim().isEmpty()) {
			File file = new File(REPORT_FILE_PATH);
			if (!file.exists()) {
				throw new Exception("Report file not found at path: " + REPORT_FILE_PATH);
			}

			uploadedReportName = file.getName();

			byte[] bytes;
			try (FileInputStream fis = new FileInputStream(file)) {
				bytes = fis.readAllBytes();
			}

			GraphServiceClient<Request> graphClient = getGraphClient(CLIENT_ID, TENANT_ID, CLIENT_SECRET);

			// ── Step 1: Upload file ───────────────────────────────────────────────
			// Use channel folder if provided, else upload to drive root (default)
			boolean hasChannelFolder = CHANNEL_FOLDER_NAME != null && !CHANNEL_FOLDER_NAME.trim().isEmpty();

			String uploadPath = hasChannelFolder ? CHANNEL_FOLDER_NAME + "/" + uploadedReportName // e.g.
																									// QA_Automations_Notifications/file.xlsx
					: uploadedReportName; // e.g. file.xlsx (drive root)

			System.out.println("📁 Upload path: " + uploadPath);

			DriveItem uploadedFile = graphClient.groups(TEAM_ID).drive().root().itemWithPath(uploadPath).content()
					.buildRequest().put(bytes);

			if (uploadedFile == null || uploadedFile.webUrl == null) {
				throw new Exception("File upload succeeded but webUrl is null.");
			}

			String fileItemId = uploadedFile.id;
			String driveId = uploadedFile.parentReference.driveId;

			System.out.println("✅ Report uploaded: " + uploadedFile.webUrl);

			// ── Step 2: Create org-wide sharing link (no login required) ─────
			com.microsoft.graph.models.DriveItemCreateLinkParameterSet linkParams = com.microsoft.graph.models.DriveItemCreateLinkParameterSet
					.newBuilder().withType("view") // view only
					.withScope("organization") // anyone in org can open
					.build();

			com.microsoft.graph.models.Permission permission = graphClient.drives(driveId).items(fileItemId)
					.createLink(linkParams).buildRequest().post();

			// Use the sharing link instead of webUrl — this one works without login
			uploadedReportUrl = permission.link.webUrl;
			System.out.println("🔗 Shareable Report Link: " + uploadedReportUrl);
		}

		String payload = buildWebhookPayload(MESSAGE_TEXT, SHAREPOINT_FILE_LINK, uploadedReportName, uploadedReportUrl);

		postToWebhook(WEBHOOK_URL, payload);

		System.out.println("✅ Message sent to Teams via Incoming Webhook");

		StringBuilder result = new StringBuilder("Message Sent Successfully");
		if (SHAREPOINT_FILE_LINK != null && !SHAREPOINT_FILE_LINK.trim().isEmpty())
			result.append(" | SharePoint Link Shared");
		if (uploadedReportUrl != null)
			result.append(" | Report Uploaded: ").append(uploadedReportUrl);

		return result.toString();
	}

	private static String buildWebhookPayload(String messageText, String sharePointFileLink, String reportFileName,
			String reportFileUrl) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("  \"@type\": \"MessageCard\",");
		sb.append("  \"@context\": \"http://schema.org/extensions\",");
		sb.append("  \"summary\": \"Automation Notification\",");
		sb.append("  \"themeColor\": \"0076D7\",");
		sb.append("  \"title\": \"Automation Notification\",");
		sb.append("  \"text\": \"" + escapeJson(messageText) + "\",");
		sb.append("  \"sections\": [");

		boolean hasSections = false;

		// Section 1 — SharePoint File Link (optional)
		boolean hasSharePointLink = sharePointFileLink != null && !sharePointFileLink.trim().isEmpty();
		if (hasSharePointLink) {
			if (hasSections)
				sb.append(",");
			sb.append("    {");
			sb.append("      \"activityTitle\": \"📂 SharePoint File Link \",");
			sb.append("      \"activityText\": \"[Click here to open the SharePoint file]("
					+ escapeJson(sharePointFileLink) + ")\"");
			sb.append("    }");
			hasSections = true;
		}

		// Section 2 — Uploaded Report Link (optional)
		boolean hasReport = reportFileUrl != null && reportFileName != null;
		if (hasReport) {
			if (hasSections)
				sb.append(",");
			sb.append("    {");
			sb.append("      \"activityTitle\": \"📊 Execution Report\",");
			sb.append("      \"activityText\": \"[" + escapeJson(reportFileName) + "](" + escapeJson(reportFileUrl)
					+ ")\"");
			sb.append("    }");
			hasSections = true;
		}

		// If no optional sections, add a placeholder to keep JSON valid
		if (!hasSections) {
			sb.append("    {");
			sb.append("      \"activityTitle\": \"ℹ️ No attachments\"");
			sb.append("    }");
		}

		sb.append("  ]");
		sb.append("}");
		return sb.toString();
	}

	private static void postToWebhook(String webhookUrl, String jsonPayload) throws Exception {
		URL url = new URL(webhookUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setDoOutput(true);

		try (OutputStream os = conn.getOutputStream()) {
			os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
		}

		int responseCode = conn.getResponseCode();
		if (responseCode != 200 && responseCode != 202) {
			InputStream errorStream = conn.getErrorStream();
			String errorBody = errorStream != null ? new String(errorStream.readAllBytes(), StandardCharsets.UTF_8)
					: "No error body";
			throw new Exception("Webhook POST failed. HTTP " + responseCode + " | " + errorBody);
		}

		System.out.println("📨 Webhook response code: " + responseCode);
	}

	private static String escapeJson(String text) {
		if (text == null)
			return "";
		return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t",
				"\\t");
	}

	private static GraphServiceClient<Request> getGraphClient(String CLIENT_ID, String TENANT_ID,
			String CLIENT_SECRET) {
		ClientSecretCredential credential = new ClientSecretCredentialBuilder().clientId(CLIENT_ID).tenantId(TENANT_ID)
				.clientSecret(CLIENT_SECRET).build();

		TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
				Collections.singletonList("https://graph.microsoft.com/.default"), credential);

		return GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();
	}

	public static void main(String[] args) throws Exception {

		NlpRequestModel model = new NlpRequestModel();
		Map<String, Object> attributes = model.getAttributes();

		attributes.put("Client_ID", "c857735f-125f-468d-a743-74112807a785");
		attributes.put("Tenant_ID", "7f943f02-1859-4b47-a7fc-f910aaa46cf7");
		attributes.put("Team_ID", "064e09a0-82a9-453f-b8a4-758872da8573");
		attributes.put("Webhook_URL",
				"https://csod365.webhook.office.com/webhookb2/064e09a0-82a9-453f-b8a4-758872da8573@7f943f02-1859-4b47-a7fc-f910aaa46cf7/BitbucketServer/321d6a44e8474d07b574aaf2de001b1c/8abd87e7-620b-4837-857c-6fa02928efd8/V2VUuyJtQUEfVg65dQenzMM9s7tRfdBFVa0Tidr726YI01%22");
		attributes.put("Message", "Testing for sending reports through backend");
		attributes.put("Report_FilePath", "C:\\CSOD_DATA\\MuleSoft_Test_Data\\EXEC16761.pdf");
		attributes.put("Channel_Folder_Name", "  ");
		attributes.put("SharePoint_File_Link",
				"https://csod365.sharepoint.com/:x:/r/sites/ITSharedServices/Shared%20Documents/3.%20DevOps%20and%20QA/CSOD-Skybeyo/IT%20Integrations/IT_Integration_Results/UAT/16%20Mar%202026/MuleSoft_Integration_Results_UAT_16_03_2026%20%2012_46_1.xlsx?d=wedc13f9eb5a44b9b8af4ba8ffdfeb0a3&csf=1&web=1&e=ZISg30");

		new SendMessageToTeams().execute(model);
		System.out.println("NLP execution completed successfully.");
	}
}