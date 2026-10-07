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

public class WaitForFileToBeDownloadAndVerifyByLoopingBack implements Nlp {

    @InputParams({
        @InputParam(name = "Downlaod Directory", type = "java.lang.String"),
        @InputParam(name = "Max Wait Time (in seconds)", type = "java.lang.Integer"),
        @InputParam(name = "Lookback Time (in seconds)", type = "java.lang.Integer")
    })
    @ReturnType(name = "absoluteFilePath", type = "java.lang.String")
    @Override
    public List<String> getTestParameters() throws NlpException {
        return new ArrayList<>();
    }

    @Override
    public StringBuilder getTestCode() throws NlpException {
        return new StringBuilder();
    }

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        Map<String, Object> attributes = nlpRequestModel.getAttributes();

        String downloadDirectory = (String) attributes.get("Downlaod Directory");
        Integer maxWaitTime = (Integer) attributes.get("Max Wait Time (in seconds)");
        Integer lookbackTime = (Integer) attributes.get("Lookback Time (in seconds)");

        String absoluteFilePath = "";
        int count = 0;

        while (true) {

            long currentTime = System.currentTimeMillis();

            long lastModifiedThreshold = currentTime - (lookbackTime * 1000L);

            File latestFile = getLatestFile(downloadDirectory, lastModifiedThreshold);

            if (latestFile != null) {
                String fileName = latestFile.getName();

                if (!fileName.endsWith(".crdownload") && !fileName.endsWith(".tmp")) {
                    nlpResponseModel.setMessage("File " + fileName + " downloaded successfully.");
                    nlpResponseModel.setStatus(CommonConstants.pass);
                    absoluteFilePath = latestFile.getAbsolutePath();
                    break;
                }
            }

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

        nlpResponseModel.getAttributes().put("absoluteFilePath", absoluteFilePath);
        return nlpResponseModel;
    }

    // ✅ Improved method: gets actual latest file
    public static File getLatestFile(String directoryPath, Long lastModifiedThreshold) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files == null || files.length == 0) {
            return null;
        }

        File latestFile = null;
        long latestTime = 0;

        for (File file : files) {
            if (file.isFile() && file.lastModified() > lastModifiedThreshold) {

                if (file.lastModified() > latestTime) {
                    latestTime = file.lastModified();
                    latestFile = file;
                }
            }
        }

        return latestFile;
    }

    // ✅ Test main method
    public static void main(String[] args) throws NlpException {
        NlpRequestModel req = new NlpRequestModel();

        req.getAttributes().put("Downlaod Directory", "C:\\Users\\User\\Downloads\\");
        req.getAttributes().put("Max Wait Time (in seconds)", 300);
        req.getAttributes().put("Lookback Time (in seconds)", 120);

        NlpResponseModel res = new WaitForFileToBeDownloadAndVerifyByLoopingBack().execute(req);

        System.out.println(res.getAttributes().get("absoluteFilePath"));
    }
}