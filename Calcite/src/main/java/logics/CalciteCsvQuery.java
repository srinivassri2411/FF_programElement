package logics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CalciteCsvQuery {
	public static void main(String[] args) throws Exception {
//        Properties props = new Properties();
//        props.setProperty("model", new File("C:\\Users\\User\\Downloads\\model.json").getAbsolutePath());
//
//        try (Connection conn = DriverManager.getConnection("jdbc:calcite:", props)) {
//            Statement stmt = conn.createStatement();
//
//            // Use CSV file name (without .csv) as table name
//            String query = "SELECT COUNT(*) AS total FROM \"output\"";
//
//            ResultSet rs = stmt.executeQuery(query);
//
//            while (rs.next()) {
//                System.out.println("Row count: " + rs.getInt("total"));
//            }
//
//            rs.close();
//            stmt.close();
//        }
		Class.forName("org.h2.Driver");

		// Connect to H2 in-memory DB
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:");

		Statement stmt = conn.createStatement();

		// Change the file path here
		String filePath = "C:\\Users\\User\\Downloads\\dbcsv.csv";

		// Execute query to create a CSV-readable table
		String createTableSQL = "CREATE TABLE my_table AS SELECT * FROM CSVREAD('" + filePath + "');";
		stmt.execute(createTableSQL);

		// Example query: get count
		String query = args[0];
		ResultSet rs = stmt.executeQuery(query);

		while (rs.next()) {
			System.out.println("Row count: " + rs.getInt(1));
//			returnValue=rs.getInt(1)+"";
		}

		conn.close();
	}
}