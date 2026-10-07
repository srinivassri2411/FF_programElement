package business_logics_Android;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.text.SimpleDateFormat;
import java.util.*;
public class greetingsBasedOnTime implements Nlp {
    @InputParams({@InputParam(name = "Current Time", type = "java.lang.String")})
    @ReturnType(name = "Greetings", type = "java.lang.String")
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String currentTime = (String) programElementsInput.get("Current Time");
          String returnValue=null;
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              // Your program element business logic starts here ...
        	  SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
     		
     		    Date time1 = sdf.parse(currentTime);
     	        Date time2 = sdf.parse("12:00:00");
     	        Date time3 = sdf.parse("15:59:00");
     	        Date time4 = sdf.parse("20:59:00");
     	        Date time5 = sdf.parse("03:59:00");
     	       
     	        // Compare times using Date
     	        if (time1.before(time2) && time1.after(time5)){
     	        	returnValue="Good Morning";
     	        } else if (time1.after(time2) && time1.before(time3)) {
     	        	returnValue="Good Afternoon";
     	        } else if (time1.after(time3) && time1.before(time4)) {
     	        	returnValue="Good Evening";
     	        }else if  (time1.after(time4) || time1.before(time5)) {
     	        	returnValue="Good Night";
     	        }
              nlpResponseModel.setStatus(CommonConstants.pass);
              //Uncomment the below line and modify the message, only if you want to override the pass message defined for the program element.
              //nlpResponseModel.setMessage("Added two numbers");
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);
             //Uncomment the below line and modify the message, only if you want to override the fail message defined for the program element.
             nlpResponseModel.setMessage("Fail due to "+e );
          }
          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("Greetings", returnValue);
          return nlpResponseModel;
      }
  }