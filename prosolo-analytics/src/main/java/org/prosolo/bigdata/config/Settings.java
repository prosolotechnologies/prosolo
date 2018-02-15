package org.prosolo.bigdata.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import java.io.File;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.prosolo.common.config.CommonSettings;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 * @author Zoran Jeremic Apr 2, 2015
 *
 */

public class Settings {
	public static class SettingsHolder {
		public static final Settings INSTANCE = new Settings();
	}

	public Config config;
	public static final String absoluteConfigPath = System
			.getProperty("user.home")
			+ File.separator
			+ ".prosolo"
			+ File.separator;
	public static final String configFileName = "prosolo_analytics_config.xml";
	private static final String defaultConfigFilePath = "config/";
	static final String LOG_PROPERTIES_FILE = "log4j.xml";
	private static final Logger LOGGER = Logger.getLogger(Settings.class
			.getName());

	public static Settings getInstance() {
		return SettingsHolder.INSTANCE;
	}

	public Settings() {
		try {
			// this.initializeLogger();
			this.loadConfig(configFileName);
			this.setSystemProperties();

		} catch (Exception e) {
			LOGGER.error("Could not load settings: ");
		}
	}

	private void loadConfig(String filename) throws Exception {
		try {
			String configToLoad = absoluteConfigPath + filename;
			File homeConfig = new File(configToLoad);
			Serializer serializer = new Persister();
			if (homeConfig.exists()) {
				InputStream is = new FileInputStream(new File(configToLoad));
				this.config = serializer.read(Config.class, is);
			}
			// // otherwise, load the default config file
			else {
				this.loadDefaultConfig();
				if (this.config != null) {
					// // and save it to the <USER_HOME>/.prosolo folder
					this.saveConfig(this.config, filename);
					// new File(absoluteConfigPath +
					// File.pathSeparator+tempFileDir).mkdirs();

				} else {
					throw new Exception();
				}
			}
			LOGGER.info("Settings loaded!");
		} catch (FileNotFoundException fnfe) {
			LOGGER.error("Could not open the configuration file: "
					+ configFileName + " - " + fnfe.getMessage());
		} catch (Exception e) {
			LOGGER.error("Could not serialize the configuration file: "
					+ configFileName, e);
		}
	}

	public void loadDefaultConfig() throws Exception {
		InputStream is = null;
		Serializer serializer = new Persister();
		try {
			// get path to config file
			URL url = Thread.currentThread().getContextClassLoader()
					.getResource(defaultConfigFilePath + configFileName);
			if (url != null) {
				String path = url.getFile();
				// remove white spaces encoded with %20
				path = path.replaceAll("%20", " ");
				is = new FileInputStream(new File(path));
				// read config file into Java
				this.config = serializer.read(Config.class, is);
			}

		} catch (FileNotFoundException fnfe) {
			// TODO
			LOGGER.error("Could not find the configuration file: "
					+ configFileName, fnfe);
		} catch (Exception e) {
			throw new Exception("Could not serialize the configuration file: "
					+ configFileName, e);
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					LOGGER.error("Could not close InputStream!", e);
				}
			}
		}

		// return is;
	}

	private void saveConfig(Object object, String fileName) {
		LOGGER.info("Saving settings...");
		Serializer serializer = new Persister();
		String homeConfigFile = absoluteConfigPath + fileName;
		File source = new File(homeConfigFile);
		new File(absoluteConfigPath).mkdirs();
		try {
			source.createNewFile();
			serializer.write(object, source);
			LOGGER.info("Settings saved!");
		} catch (Exception e) {
			LOGGER.error("Could not save the configuration file: " + fileName+" full path:"+homeConfigFile,
					e);
		}
	}
	private void setSystemProperties(){
		System.setProperty("spark.mode",this.config.sparkConfig.mode);
		System.setProperty("spark.namespace",CommonSettings.getInstance().config.namespace);
		System.setProperty("spark.maxCores",String.valueOf(this.config.sparkConfig.maxCores));
		System.setProperty("spark.master",this.config.sparkConfig.master);
		System.setProperty("spark.executorMemory",this.config.sparkConfig.executorMemory);
		System.setProperty("spark.applicationName",this.config.sparkConfig.appName);
		System.setProperty("spark.oneJar",this.config.sparkConfig.oneJar);
		System.setProperty("cassandra.dbHost",this.config.dbConfig.dbServerConfig.dbHost);
		System.setProperty("cassandra.dbPort",String.valueOf(this.config.dbConfig.dbServerConfig.dbPort));
		System.setProperty("elasticsearch.host",CommonSettings.getInstance().config.elasticSearch.esHostsConfig.esHosts.get(0).host);
		System.setProperty("elasticsearch.port",String.valueOf(this.config.sparkConfig.elasticsearchConnectorPort));
		System.setProperty("elasticsearch.jobsIndex",String.valueOf(CommonSettings.getInstance().config.elasticSearch.jobsLogsIndex));
		System.setProperty("mysql.dbHost",CommonSettings.getInstance().config.mysqlConfig.host);
		System.setProperty("mysql.dbName",CommonSettings.getInstance().config.mysqlConfig.database);
		System.setProperty("mysql.dbPort",String.valueOf(CommonSettings.getInstance().config.mysqlConfig.port));
		System.setProperty("mysql.dbUser",CommonSettings.getInstance().config.mysqlConfig.user);
		System.setProperty("mysql.dbPass",CommonSettings.getInstance().config.mysqlConfig.password);
	}

	public void initializeLogger() {
		// org.apache.log4j.Logger.getLogger("com.datastax.driver").setLevel(org.apache.log4j.Level.OFF);
		// org.apache.log4j.Logger.getLogger("org.apache.cassandra").setLevel(org.apache.log4j.Level.ERROR);

		// Properties logProperties = new Properties();

		// try
		// {
		// load our log4j properties / configuration file
		URL url = Thread.currentThread().getContextClassLoader()
				.getResource(LOG_PROPERTIES_FILE);
		if (url != null) {
			String path = url.getFile();
			// remove white spaces encoded with %20
			path = path.replaceAll("%20", " ");
			DOMConfigurator.configure(url);

			/*
			 * LoggerContext lc = (LoggerContext)
			 * LoggerFactory.getILoggerFactory(); JoranConfigurator configurator
			 * = new JoranConfigurator(); configurator.setContext(lc);
			 * lc.reset(); try { configurator.doConfigure(path); } catch
			 * (JoranException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */

			// logProperties.load(new FileInputStream(new File(path)));
			// PropertyConfigurator.configure(logProperties);
			LOGGER.debug("Logging initialized.");
		}
		// }
		// catch(IOException e)
		// {
		// throw new RuntimeException("Unable to load logging property " +
		// LOG_PROPERTIES_FILE);
		// }
	}
}
