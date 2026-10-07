package business_logics_Android;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class MP3ToWav implements Nlp {
    @InputParams({@InputParam(name = "MP3 File Path", type = "java.lang.String")})
    @ReturnType(name = "string3", type = "java.lang.String")
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
    	  //System.loadLibrary("avutil");
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Map<String, Object> attributes = nlpRequestModel.getAttributes();
          String mp3FilePath = (String) attributes.get("MP3 File Path");
String wavFilePath=StringUtils.EMPTY;
        try  {
                                if (mp3FilePath.endsWith(".mp3")) {
                                     wavFilePath = mp3FilePath.substring(0, mp3FilePath.lastIndexOf(".mp3")) + ".wav";
                    	            convertMP3ToWAV(mp3FilePath, wavFilePath.toString());
                              	  nlpResponseModel.setStatus(CommonConstants.pass);
                        		  nlpResponseModel.setMessage("The mp3 file is converted to "  + wavFilePath);
                }
                                else {
                              	  nlpResponseModel.setStatus(CommonConstants.fail);
                        		  nlpResponseModel.setMessage("This is not a mp3 file path "  + mp3FilePath);
                                }
        }
catch(Exception e) {
 	  StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionAsString = sw.toString();
	  nlpResponseModel.setStatus(CommonConstants.fail);
	  nlpResponseModel.setMessage("Failed  "+exceptionAsString);
	 
}
         nlpResponseModel.getAttributes().put("string3", wavFilePath);
          return nlpResponseModel;
      }
	    public static void convertMP3ToWAV(String inputFilePath, String outputFilePath) throws IOException, FFmpegFrameGrabber.Exception, FFmpegFrameRecorder.Exception {
	        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFilePath);
	        grabber.start();
	        int audioChannels = grabber.getAudioChannels();
	        int sampleRate = grabber.getSampleRate();
	        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFilePath, grabber.getAudioChannels());
	        recorder.setSampleRate(sampleRate);
	        recorder.setAudioChannels(audioChannels);
	        recorder.setFormat("wav"); // Explicitly set the output format to WAV
	        recorder.start();
	        org.bytedeco.javacv.Frame frame;
	        while ((frame = grabber.grabFrame()) != null) {
	            recorder.record(frame);
	        }
	        grabber.stop();
	        recorder.stop();
	        grabber.release();
	        recorder.release();
	    }}