import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import javax.swing.JOptionPane;

//import org.cctns.cas.state.offline.common.logger.CctnsLogger;
//import org.cctns.cas.state.offline.common.logger.CoreLoggerConstants;
//import org.cctns.dao.DBRunner;


public class ConnectcionMigration {
	private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";
	private static final String URL = "jdbc:mysql://localhost:3306/cctns_kochi";
	private static final String USER = "root";
	private static final String PASSWORD = "abcd";
	private static String INSTRUCTIONS = new String();
	//private static CctnsLogger cctnsLogger = CctnsLogger.getInstance();
//jdbc:mysql://localhost:3306/binding?useUnicode=true&characterEncoding=utf8&user=root&password=abcd
	public static Connection getConnection() throws SQLException {
//		return DriverManager.getConnection("jdbc:mysql://localhost:5432/test?useUnicode=true&characterEncoding=utf8&user=root&password=root");
               return DriverManager.getConnection("jdbc:postgresql://10.23.72.167:5432/cipatest?user=postgres&password=administrator");

	}
	public static Connection getConnectionTest() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://10.23.72.194:3307/test?useUnicode=true&characterEncoding=utf8&user=root&password=cctns");
	}
	static {
		try {
			Class.forName(DRIVER_NAME).newInstance();
			System.out.println("*** Driver loaded");
		} catch (Exception e) {
			System.out.println("*** Error : " + e.toString());
			
		}

	}

	public static void main(String[] args) throws SQLException {
		Connection con = getConnection();
		 Connection conTest =getConnectionTest();
		// String sqlQuery = "select concat('INSERT INTO m_accommodation (ACCOMMODATION_CD,LANG_CD,ACCOMMODATION,ORIGINAL_RECORD) VALUES (', concat(ACCOMMODATION_CD, ',6', ',\'', ACCOMMODATION, '\',', '1' ');')) from m_accommodation where lang_cd=6;";
		//File file = new File("D:\\cctns_state_db_master_mandatory_hindi_data_added_by_kochi.sql");
		// FileReader fr = new FileReader(new File("D:\\mysqlHindi.sql"));
		// be sure to not have line starting with "--" or "/*" or any other non
		// aplhabetical character
		 String sqlQuery = "select ACCOMMODATION_CD,LANG_CD,ACCOMMODATION,ORIGINAL_RECORD from m_accommodation where lang_cd=6;";
		new ConnectcionMigration().executeSqlScript(con,conTest, sqlQuery);
	}
	
	public void executeSqlScript(Connection conn,Connection conTest, String sqlQuery) throws SQLException {

		// Delimiter
		String delimiter = ";";
		String finalSql ="";
		String values="";
		StringBuffer sb= new StringBuffer();
		Statement fetchStatement = null;
		fetchStatement = conn.createStatement();
		Reader stream =null;
		 ResultSet rs = fetchStatement.executeQuery(sqlQuery);
		 String insertSQL ="INSERT INTO m_accommodation (ACCOMMODATION_CD,LANG_CD,ACCOMMODATION,ORIGINAL_RECORD) VALUES ( ";
		 while(rs.next()){
			 //values=rs.getString("ACCOMMODATION_CD")+" " +rs.getString("LANG_CD")+" "+rs.getCharacterStream ("ACCOMMODATION")+" "+rs.getString("ORIGINAL_RECORD")+" ";
			 values= " "+ rs.getString("ACCOMMODATION_CD")+", "+rs.getString("LANG_CD")+", ? "+" ,"+rs.getString("ORIGINAL_RECORD") +");";
			 stream = rs.getCharacterStream ("ACCOMMODATION");
		 }
		 
		 insertSQL = insertSQL+values;
		 System.out.println(insertSQL);
		 
		// Create scanner
		Scanner scanner;
		try {
			scanner = new Scanner(insertSQL).useDelimiter(delimiter);
		} catch (Exception e1) { 
			return;
		}

		// Loop through the SQL file statements
		PreparedStatement currentStatement = null; 
		while (scanner.hasNext()) {

			// Get statement
			String rawStatement = scanner.next() + delimiter;
			try {
				// Execute statement
				
				currentStatement = conTest.prepareStatement(rawStatement);
				currentStatement.setCharacterStream(1, stream);
				currentStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
				//cctnsLogger.log(CoreLoggerConstants.ERROR, currentStatement.toString());
				
			} finally {
				// Release resources
				if (currentStatement != null) {
					try {
						currentStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
						//cctnsLogger.log(CoreLoggerConstants.ERROR, currentStatement);
					}
				}
				currentStatement = null;
			}
		}
	}
}
