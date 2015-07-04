package org.prosolo.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.Test;

/**
 *
 * @author Zoran Jeremic, May 27, 2014
 *
 */
public class DBConnectionTest {

	@Test
	public void test() {
		System.out.println("Testing db connection now...");
		Connection connection = null;
		try {
			// This is the JDBC driver class for Oracle database
			Class.forName("com.mysql.jdbc.Driver");

			// We use an Oracle express database for this example
			String url = "jdbc:mysql://54.235.77.216:3306/prosolo";

			// Define the username and password for connection to our database.
			String username = "prosolo";
			String password = "prosolo2014";
			System.out.println("DB URL:"+url+".username."+username+".password."+password);
			// Connect to database
			connection = DriverManager.getConnection(url, username, password);
			if(connection==null){System.out.println("Connection is null");}
			else {System.out.println("Connection is not null:");}
			 Statement stmt = null;
			 stmt = connection.createStatement();
			String sql = "CREATE TABLE TEST " +
	                   "(id INTEGER not NULL, " +
	                   " first VARCHAR(255), " + 
	                   " last VARCHAR(255), " + 
	                   " age INTEGER, " + 
	                   " PRIMARY KEY ( id ))"; 
			stmt.executeUpdate(sql);
			 System.out.println("Created table in given database...");
			 connection.close();
		}catch(Exception ex){
			ex.getStackTrace();
		}
	}

}
