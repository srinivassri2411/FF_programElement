
package mainFrameUtilities;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;

public class ClearAS400Table implements Nlp {
    @InputParams({@InputParam(name = "User", type = "java.lang.String"), @InputParam(name = "Password", type = "java.lang.String"), @InputParam(name = "Library Name", type = "java.lang.String"), @InputParam(name = "Table Name", type = "java.lang.String")})
    

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
//          String host = (String) programElementsInput.get("Host");
          String user = (String) programElementsInput.get("User");
          String password = (String) programElementsInput.get("Password");
          String libraryName = (String) programElementsInput.get("Library Name");
          String tableName = (String) programElementsInput.get("Table Name");
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              String url = "jdbc:as400://185.113.5.134";

              Class.forName("com.ibm.as400.access.AS400JDBCDriver");

              Connection connection = DriverManager.getConnection(url, user, password);

              System.out.println("Connected to AS400 DB2");

              String sql = "DELETE FROM " + libraryName + "." + tableName;

              Statement stmt = connection.createStatement();

              int rowsDeleted = stmt.executeUpdate(sql);

              System.out.println("Rows deleted: " + rowsDeleted);

              stmt.close();
              connection.close();

              nlpResponseModel.setMessage(tableName+" cleared successfully with "+rowsDeleted+" rows.");
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
       	     e.printStackTrace();
             StringWriter sw = new StringWriter();
             e.printStackTrace(new PrintWriter(sw));

             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Failed to create Table : " + sw);
          }

          return nlpResponseModel;
      }

  } 