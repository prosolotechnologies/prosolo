package org.prosolo.config;

import org.prosolo.config.admin.AdminConfig;
import org.prosolo.config.app.AppConfig;
import org.prosolo.config.fileManagement.FileManagementConfig;
import org.prosolo.config.init.InitConfig;
import org.prosolo.config.services.ServicesConfig;
import org.prosolo.util.StringUtils;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Config {
	
	@Element(name = "init") 
	public InitConfig init;
	
	@Element(name = "logging-config")
	public String log4j;

	@Element(name = "mongo-db-config")
	public MongoDBConfig mongoDatabase;
	
	@Element(name="app-config")
	public AppConfig application;
	
	@Element(name = "file-management")
	public FileManagementConfig fileManagement;
	
	@Element(name = "services")
	public ServicesConfig services;
	
	@Element(name="twitter-stream-config")
	public TwitterStreamConfig twitterStreamConfig; 
	
	@Element(name="admin")
	public AdminConfig admin; 
	
	@Element(name="analytical-server")
	public AnalyticalServerConfig analyticalServerConfig;
	
	@Override
	public String toString() {
		return StringUtils.toStringByReflection(this);
	}
	
	public InitConfig getInit() {
		return init;
	}

	public AppConfig getApplication() {
		return application;
	}

}
