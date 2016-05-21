package org.prosolo.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.prosolo.config.Config;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class Settings {

	private static Logger logger = Logger.getLogger(Settings.class);

	private static final String configFileName = "prosolo_main_config.xml";
	private static final String defaultConfigFilePath = "config/";
 
	private static final String absoluteConfigPath = System.getProperty("user.home") + File.separator + ".prosolo" + File.separator;
	//private static final String absoluteConfigPath = System.getProperty("appserver.home") + File.separator + ".prosolo" + File.separator;
	public Config config;

	private static class SettingsHolder { 
		private static final Settings INSTANCE = new Settings();
	}

	public static Settings getInstance() {
		return SettingsHolder.INSTANCE;
	}

	private Settings() {
		try {
			loadConfig();
		} catch (Exception e) {
			logger.error("Could not load settings: ", e);
		}
	}

	private void loadConfig() throws Exception {
		String homeConfigFile = absoluteConfigPath + configFileName;

		try {
			File homeConfig = new File(homeConfigFile);
			Serializer serializer = new Persister();
			// if there is a config file in the <USER_HOME>/.prosolo folder load that one
			if (homeConfig.exists()) {
				InputStream is = new FileInputStream(new File(homeConfigFile));
				config = serializer.read(Config.class, is);
			} 
			// otherwise, load the default config file
			else {
				loadDefaultConfig();
				if (config != null) {
					// and save it to the <USER_HOME>/.prosolo folder
					saveConfig();
				} else {
					throw new Exception();
				}
			}
			logger.info("Settings loaded!");

		} catch (FileNotFoundException fnfe) {
			throw new FileNotFoundException(
					"Could not open the configuration file: " + configFileName
							+ " - " + fnfe.getMessage());

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Could not serialize the configuration file: "
					+ configFileName, e);
		}
	}

	private void loadDefaultConfig() throws Exception {
		InputStream is = null;
		Serializer serializer = new Persister();

		try {
			// get path to config file
			URL url = Thread.currentThread().getContextClassLoader().getResource(defaultConfigFilePath + configFileName);
			if (url != null) {
				String path = url.getFile();
				// remove white spaces encoded with %20
				path = path.replaceAll("%20", " ");
				is = new FileInputStream(new File(path));
				config = serializer.read(Config.class, is);
			} else {
				loadDefaultConfig1(is, serializer);
			}
		} catch (FileNotFoundException fnfe) {
			loadDefaultConfig1(is, serializer);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Could not read the config file: " + configFileName, e);
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("Could not close InputStream!", e);
				}
			}
		}
	}

	private void loadDefaultConfig1(InputStream is, Serializer serializer) throws FileNotFoundException {
		is = Thread.currentThread().getContextClassLoader().getResourceAsStream(defaultConfigFilePath + configFileName);
		
		if (is != null) {
			try {
				config = serializer.read(Config.class, is);
			} catch (Exception e) {
				logger.error("Could not read the config file: " + configFileName, e);
			}
		} else {
			throw new FileNotFoundException("Could not open the config file: " + configFileName);
		}
	}

	private void saveConfig() {
		logger.info("Saving settings...");
		Serializer serializer = new Persister();
		String homeConfigFile = absoluteConfigPath + configFileName;
		File source = new File(homeConfigFile);
		
		// create dir
		new File(absoluteConfigPath).mkdirs();
		
		try {
			source.createNewFile();
			serializer.write(config, source);
			logger.info("Settings saved!");
		} catch (Exception e) {
			logger.error("Could not save the configuration file: " + configFileName+" full path:"+homeConfigFile, e);
		}
	}
	public String getAbsoluteConfigPath(){
		return absoluteConfigPath;
	}

}
