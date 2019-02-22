package org.prosolo.config.init;

import org.simpleframework.xml.Element;

public class InitConfig {

	@Element(name = "bc", required = false)
	public int bc = 1;
	
	@Element(name = "localization", required = false)
	public String localization;
	
	@Element(name = "formatDB", required = false)
	public boolean formatDB = true;
	
	@Element(name = "importData", required = false)
	public boolean importData = true;
	
	@Element(name = "importCapabilities", required = false)
	public boolean importCapabilities = true;
	
	@Element(name = "indexTrainingSet", required = false)
	public boolean indexTrainingSet = true;
	
	@Element(name = "default-user")
	public DefaultUserConfig defaultUser;

	@Element(name = "database-migration")
	public DatabaseMigration databaseMigration;
	
	public String getLocalization() {
		return localization;
	}

}