
package Custom_SQL_Query;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

public class InsertInvoiceToDB implements Nlp {
    @InputParams({@InputParam(name = "Invoice ID", type = "java.lang.String"), @InputParam(name = "Subject", type = "java.lang.String"), @InputParam(name = "Current Date", type = "java.lang.String"), @InputParam(name = "Valid Till Date", type = "java.lang.String"),@InputParam(name = "Address ID", type = "java.lang.String"),@InputParam(name = "Contact ID", type = "java.lang.String"),@InputParam(name = "Order ID", type = "java.lang.String")})
    @ReturnType(name = "Query", type = "java.lang.String")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String invoiceID = (String) programElementsInput.get("Invoice ID");
          String subject = (String) programElementsInput.get("Subject");
          String currentDate = (String) programElementsInput.get("Current Date");
          String validTillDate = (String) programElementsInput.get("Valid Till Date");
          String addressID = (String) programElementsInput.get("Address ID");
          String contactID = (String) programElementsInput.get("Contact ID");
          String orderID = (String) programElementsInput.get("Order ID");
          String returnValue=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              // Your program element business logic starts here ...
        	  returnValue ="INSERT INTO invoice (invoice_id,creation_date,discount,due_date,grand_total,net_total,shipping_and_handling_charges,status,subject,valid_till,billing_address_address_id,contact_contact_id,sales_order_order_id,shipping_address_address_id) VALUES ('"+invoiceID+"','"+currentDate+"',0.0,NULL, 99.0,99.0,0.0,NULL,'"+subject+"','"+validTillDate+"','"+addressID+"','"+contactID+"','"+orderID+"','"+addressID+"' );";
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);

          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("Query", returnValue);
          return nlpResponseModel;
      }

  } 