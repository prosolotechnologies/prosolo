package org.prosolo.app;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.prosolo.services.logging.LoggingServiceAdmin;
import org.prosolo.util.FileUtil;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

public class BeforeContextLoader implements ServletContextListener	{
	
	private static Logger logger = Logger.getLogger(BeforeContextLoader.class.getName());
 
	/* Application Startup Event */
	public void	contextInitialized(ServletContextEvent ce) {
		// read settings from config.xml
		logger.debug("before context initialization");
		Settings settings = Settings.getInstance();
		if (settings.config == null) {
			return;
		}
		logger.debug("configuring logger");
		// configure logging
		PropertyConfigurator.configure(this.getClass().getClassLoader().getResource(settings.config.log4j));
		SLF4JBridgeHandler.install();
		 logger.debug("logging configured");
		if (settings.config.init.formatDB) {
			logger.debug("deleting logging database collections");
		  	deleteLoggingDatabaseCollections();
			logger.debug("create or empty upload folder");
			createOrEmptyUploadFolder();
		}
//		if(settings.config.init.indexTrainingSet){
//			ESAdministration esAdmin=new ESAdministrationImpl();
//			esAdmin.indexTrainingSet();
//		}
		logger.debug("before context initialization finished");
	}
	private void deleteLoggingDatabaseCollections(){
		LoggingServiceAdmin loggingServiceAdmin=new LoggingServiceAdmin();
		boolean deleted=loggingServiceAdmin.dropAllCollections();
		if(deleted){
			logger.info("Logging db deleted successfully.");
		}
		
	}
	
	private void createOrEmptyUploadFolder() {
		String uploadFolderPath = Settings.getInstance().config.fileManagement.uploadPath;
		File uploadFolder = new File(uploadFolderPath);
		
		if (uploadFolder.exists()) {
			logger.debug("Initiated emptying of upload folder.");
			
			FileUtil.deleteFolderContents(uploadFolder);
			
			logger.debug("Completed emptying of upload folder.");
		} else {
			logger.debug("Initiated creation of upload folder.");
			
			boolean fodlerCreated = uploadFolder.mkdirs();
			
			if (fodlerCreated) {
				logger.debug("Completed deletion of upload folder.");
			} else {
				logger.error("Error creating folder at location '"+uploadFolderPath+"'. " +
						"Check if the you have proper permissions..");
			}
		}
	}


	/* Application Shutdown	Event */
	public void	contextDestroyed(ServletContextEvent ce) {}
}