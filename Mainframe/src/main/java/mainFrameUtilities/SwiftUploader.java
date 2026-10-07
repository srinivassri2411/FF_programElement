
package mainFrameUtilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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



public class SwiftUploader implements Nlp {
    @InputParams({@InputParam(name = "TXT File Path", type = "java.lang.String"),
    	@InputParam(name = "Host", type = "java.lang.String"),
    	@InputParam(name = "User", type = "java.lang.String"),
    	@InputParam(name = "Password", type = "java.lang.String"),
    	@InputParam(name = "Library", type = "java.lang.String"),
    @InputParam(name = "Table", type = "java.lang.String")
    })
    

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String filePath = (String) programElementsInput.get("TXT File Path");
          String host = (String) programElementsInput.get("Host");
          String user = (String) programElementsInput.get("User");
          String password = (String) programElementsInput.get("Password");
          String library = (String) programElementsInput.get("Library");
          String table = (String) programElementsInput.get("Table");
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {

              String swiftMessage = readFile(filePath);

              swiftMessage = swiftMessage.replace("\r", "")
                      .replaceAll("\\n\\s+", "\n")
                      .replaceAll("\\n\\s*\\n", "\n")
                      .trim();

              String[] transactions = swiftMessage.split("\\{1:");

              List<Transaction> parsedTransactions = new ArrayList<>();

              for (String txn : transactions) {

                  if (txn.trim().isEmpty())
                      continue;

                  txn = "{1:" + txn;

                  int blockStart = txn.indexOf("{4:");
                  int blockEnd = txn.indexOf("-}");

                  if (blockStart == -1 || blockEnd == -1)
                      continue;

                  String messageBody = txn.substring(blockStart + 3, blockEnd).trim();

                  Transaction transaction = parseTransaction(messageBody);
                  parsedTransactions.add(transaction);
              }

              insertIntoDB(parsedTransactions, host, user, password, library, table);

              nlpResponseModel.setMessage("Transactions inserted successfully!");
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
       	     e.printStackTrace();
             StringWriter sw = new StringWriter();
             e.printStackTrace(new PrintWriter(sw));

             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Failed to upload swift format : " + sw);

          }

          return nlpResponseModel;
      }
    private static String readFile(String filePath) throws Exception {

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        StringBuilder content = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null)
            content.append(line).append("\n");

        reader.close();

        return content.toString();
    }

    private static Transaction parseTransaction(String swiftMessage) {

        Transaction mt103 = new Transaction();

        mt103.senderReference = extractField(swiftMessage, ":20:");
        mt103.bankOperationCode = extractField(swiftMessage, ":23B:");
        mt103.valueDateCurrencyAmount = extractField(swiftMessage, ":32A:");
        mt103.currencyInstructedAmount = extractField(swiftMessage, ":33B:");
        mt103.detailsOfCharges = extractField(swiftMessage, ":71A:");

        String orderingCustomer = extractField(swiftMessage, ":50K:");

        if (orderingCustomer != null) {

            String[] lines = orderingCustomer.split("\n");

            if (lines.length > 0)
                mt103.orderingCustomerAccount = lines[0].replace("/", "").trim();

            if (lines.length > 1)
                mt103.orderingCustomerName = lines[1].trim();

            if (lines.length > 2) {

                StringBuilder address = new StringBuilder();

                for (int i = 2; i < lines.length; i++)
                    address.append(lines[i]).append(" ");

                mt103.orderingCustomerAddress = address.toString().trim();
            }
        }

        String beneficiaryCustomer = extractField(swiftMessage, ":59:");

        if (beneficiaryCustomer != null) {

            String[] lines = beneficiaryCustomer.split("\n");

            if (lines.length > 1)
                mt103.beneficiaryCustomerName = lines[1].trim();

            if (lines.length > 2) {

                StringBuilder address = new StringBuilder();

                for (int i = 2; i < lines.length; i++)
                    address.append(lines[i]).append(" ");

                mt103.beneficiaryCustomerAddress = address.toString().trim();
            }
        }

        if (mt103.valueDateCurrencyAmount != null) {

            String value = mt103.valueDateCurrencyAmount;

            if (value.length() >= 9) {

                String tempDate = value.substring(0, 6);

                mt103.date = tempDate.substring(0, 2) + "-"
                        + tempDate.substring(2, 4) + "-"
                        + tempDate.substring(4, 6);

                mt103.currency = value.substring(6, 9);

                if (value.length() > 9)
                    mt103.amount = value.substring(9);
            }
        }

        return mt103;
    }

    private static String extractField(String message, String fieldIdentifier) {

        int startIndex = message.indexOf(fieldIdentifier);

        if (startIndex == -1)
            return null;

        startIndex += fieldIdentifier.length();

        int nextFieldIndex = message.indexOf("\n:", startIndex);

        if (nextFieldIndex == -1)
            nextFieldIndex = message.length();

        return message.substring(startIndex, nextFieldIndex).trim();
    }

    private static void insertIntoDB(List<Transaction> transactions,
                                     String ip,
                                     String user,
                                     String password,
                                     String libraryName,
                                     String tableName) throws Exception {

        String url = "jdbc:as400://" + ip;

        Class.forName("com.ibm.as400.access.AS400JDBCDriver");

        Connection connection = DriverManager.getConnection(url, user, password);

        System.out.println("Connected to AS400 DB2");

        String insertSQL = "INSERT INTO " + libraryName + "." + tableName +
                " (senderReference,bankOperationCode,date,currency,amount," +
                "currencyInstructedAmount,orderingCustomerAccount,orderingCustomerName," +
                "orderingCustomerAddress,beneficiaryCustomerName,beneficiaryCustomerAddress,detailsOfCharges)" +
                " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement ps = connection.prepareStatement(insertSQL);

        for (Transaction t : transactions) {

            ps.setString(1, t.senderReference);
            ps.setString(2, t.bankOperationCode);
            ps.setString(3, t.date);
            ps.setString(4, t.currency);
            ps.setString(5, t.amount);
            ps.setString(6, t.currencyInstructedAmount);
            ps.setString(7, t.orderingCustomerAccount);
            ps.setString(8, t.orderingCustomerName);
            ps.setString(9, t.orderingCustomerAddress);
            ps.setString(10, t.beneficiaryCustomerName);
            ps.setString(11, t.beneficiaryCustomerAddress);
            ps.setString(12, t.detailsOfCharges);

            ps.executeUpdate();

            System.out.println("Inserted transaction: " + t.senderReference);
        }

        ps.close();
        connection.close();
    }

    static class Transaction {

        String senderReference;
        String bankOperationCode;
        String valueDateCurrencyAmount;
        String currencyInstructedAmount;
        String detailsOfCharges;

        String orderingCustomerAccount;
        String orderingCustomerName;
        String orderingCustomerAddress;

        String beneficiaryCustomerName;
        String beneficiaryCustomerAddress;

        String date;
        String currency;
        String amount;
    }
  } 