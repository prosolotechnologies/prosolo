package org.prosolo.app;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.prosolo.services.indexing.ESAdministration;
import org.prosolo.services.indexing.impl.ESAdministrationImpl;
import org.prosolo.services.logging.LoggingServiceAdmin;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.MySQLConfig;
import org.prosolo.util.FileUtil;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class BeforeContextLoader implements ServletContextListener	{
	
	private static Logger logger = Logger.getLogger(BeforeContextLoader.class.getName());
 
	/* Application Startup Event */
	public void	contextInitialized(ServletContextEvent ce) {
		// read settings from config.xml
		Settings settings = Settings.getInstance();
		if (settings.config == null) {
			return;
		}

		// configure logging
		PropertyConfigurator.configure(this.getClass().getClassLoader().getResource(settings.config.log4j));
		SLF4JBridgeHandler.install();
		 
		if (settings.config.init.formatDB) {
		  	deleteLoggingDatabaseCollections();

		  	try {
				initElasticSearchIndexes();
			} catch (IndexingServiceNotAvailable e1) {
				logger.error(e1);
			}
			
		  	try {
				dropTables();
			} catch (SQLException e) {
				logger.error(e.getMessage());
			} catch (ClassNotFoundException e) {
				logger.error(e.getMessage());
			}
			
			createOrEmptyUploadFolder();
		}
//		if(settings.config.init.indexTrainingSet){
//			ESAdministration esAdmin=new ESAdministrationImpl();
//			esAdmin.indexTrainingSet();
//		}
	 
	}
	private void deleteLoggingDatabaseCollections(){
		LoggingServiceAdmin loggingServiceAdmin=new LoggingServiceAdmin();
		boolean deleted=loggingServiceAdmin.dropAllCollections();
		if(deleted){
			logger.info("Logging db deleted successfully.");
		}
		
	}
 
	private void initElasticSearchIndexes() throws IndexingServiceNotAvailable {
		ESAdministration esAdmin=new ESAdministrationImpl();
		esAdmin.deleteIndexes();
		esAdmin.createIndexes();
	}
	
	private void createOrEmptyUploadFolder() {
		String uploadFolderPath = Settings.getInstance().config.fileManagement.uploadPath;
		File uploadFolder = new File(uploadFolderPath);
		
		if (uploadFolder.exists()) {
			logger.info("Initiated emptying of upload folder.");
			
			FileUtil.deleteFolderContents(uploadFolder);
			
			logger.info("Completed emptying of upload folder.");
		} else {
			logger.info("Initiated creation of upload folder.");
			
			boolean fodlerCreated = uploadFolder.mkdirs();
			
			if (fodlerCreated) {
				logger.info("Completed deletion of upload folder.");
			} else {
				logger.error("Error creating folder at location '"+uploadFolderPath+"'. " +
						"Check if the you have proper permissions..");
			}
		}
	}


	/* Application Shutdown	Event */
	public void	contextDestroyed(ServletContextEvent ce) {}
	
	private void dropTables() throws SQLException,
			ClassNotFoundException {
		logger.info("Initiated database format.");
		
		Connection connection = null;
		try {
			// This is the JDBC driver class for Oracle database
			Class.forName("com.mysql.jdbc.Driver");

			// We use an Oracle express database for this example
			MySQLConfig mySQLConfig=CommonSettings.getInstance().config.mysqlConfig;
			String username = mySQLConfig.user;
			String password = mySQLConfig.password;
			String host = mySQLConfig.host;
			int port = mySQLConfig.port;
			String database = mySQLConfig.database;
			String url="jdbc:mysql://"+ host + ":" + port + "/" + database;
			// Define the username and password for connection to our database.
 
			// Connect to database
			connection = DriverManager.getConnection(url, username, password);
	 
			connection.setAutoCommit(false);
			
			DatabaseMetaData md = connection.getMetaData();
		    ResultSet rs = md.getTables(null, null, "%", null);
		    
		    Statement statement = connection.createStatement();
		    
		    statement.execute("SET FOREIGN_KEY_CHECKS = 0;");
		    
		    int count = 0;
		    while (rs.next()) {
				//logger.info("Deleting table: " + rs.getString(3));
				// To delete a table from database we use the DROP TABLE IF EXISTS
				// command and specify the table name to be dropped
				String query = "drop table if exists " + rs.getString(3) + " cascade;  \n";
				// Create a statement
				// Execute the statement to delete the table
				statement.executeUpdate(query);
				count++;
		    }
		    
		    statement.execute("SET FOREIGN_KEY_CHECKS = 1;");
		    statement.execute("CREATE TABLE innodb_lock_monitor(a int) ENGINE=INNODB;");
		    
		    logger.info("---------------------------------------");
		    logger.info("Deleted tables: "+count);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		}
		
		logger.info("Database format completed!");
	}
}