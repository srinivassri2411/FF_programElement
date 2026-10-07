package cmdAutomation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class WaitForFileToBeDownload implements Nlp {
    @InputParams({ @InputParam(name = "Downlaod Directory", type = "java.lang.String"),
            @InputParam(name = "Max Wait Time (in seconds)", type = "java.lang.Integer") })
    @ReturnType(name = "absoluteFilePath", type = "java.lang.String")

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

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        Map<String, Object> attributes = nlpRequestModel.getAttributes();

        String downlaodDirectory = (String) attributes.get("Downlaod Directory");
        Integer maxWaitTime = (Integer) attributes.get("Max Wait Time (in seconds)");

        // Your program element business logic goes here ...
        String absoluteFilePath = "";
        int count = 0;
        long currentTime = System.currentTimeMillis();
        long lastModifiedThreshold = currentTime - 1000; // 1 second ago
        while (true) {
            File latestFile = getLatestFile(downlaodDirectory, lastModifiedThreshold);
            if (latestFile != null) {
                String fileName = latestFile.getName();
                if (!fileName.endsWith(".crdownload") && !fileName.endsWith(".tmp")
                        && !fileName.startsWith(".com.google.Chrome")) {
                    nlpResponseModel.setMessage("File " + fileName + " downloaded successfully.");
                    nlpResponseModel.setStatus(CommonConstants.pass);
                    absoluteFilePath = latestFile.getAbsolutePath();
                    break;
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count++;
                if (count >= maxWaitTime) {
                    nlpResponseModel.setMessage("No files downloaded within the last " + maxWaitTime + " seconds.");
                    nlpResponseModel.setStatus(CommonConstants.fail);
                    break;
                }
            }
        }

        nlpResponseModel.getAttributes().put("absoluteFilePath", absoluteFilePath);
        return nlpResponseModel;
    }

    public static File getLatestFile(String directoryPath, Long lastModifiedThreshold) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files == null || files.length == 0) {
            return null; // No files in the directory
        }

        // Find the latest file updated within the last 1 second
        File latestFile = null;

        for (File file : files) {
            if (file.isFile() && file.lastModified() > lastModifiedThreshold) {
                latestFile = file;
                break; // Found the latest file updated within the last 1 second
            }
        }
        return latestFile;
    }

    public static void main(String[] args) throws NlpException {
        NlpRequestModel req = new NlpRequestModel();
        req.getAttributes().put("Downlaod Directory", "/Users/sonicwall/Downloads");
        req.getAttributes().put("Max Wait Time (in seconds)", 300);
        NlpResponseModel res = new WaitForFileToBeDownload().execute(req);
        System.out.println(res.getAttributes().get("absoluteFilePath"));
    }
}