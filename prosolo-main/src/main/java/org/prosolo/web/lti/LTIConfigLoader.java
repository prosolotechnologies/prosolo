package org.prosolo.web.lti;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.prosolo.web.lti.json.data.ToolProxy;

import com.google.gson.Gson;

public class LTIConfigLoader {

	private static Logger logger = Logger.getLogger(LTIConfigLoader.class);

	private static final String FILE_NAME = "lti_conf.json";
	private static final String FILE_PATH = "config/";

	private static LTIConfigLoader instance;

	private LTIConfigLoader() {

	}

	public static LTIConfigLoader getInstance() {
		if (instance == null) {
			instance = new LTIConfigLoader();
		}
		return instance;
	}

	public ToolProxy loadToolProxy() throws Exception {
		Gson gson = new Gson();
		BufferedReader br = null;
		try {
			URL url = Thread.currentThread().getContextClassLoader().getResource(FILE_PATH + FILE_NAME);
			if (url != null) {
				String path = url.getFile();
				path = path.replaceAll("%20", " ");
				br = new BufferedReader(new FileReader(path));
				return gson.fromJson(br, ToolProxy.class);
			}else{
				throw new Exception();
			}
		} catch (Exception e) {
			logger.error(e);
			throw new Exception("Error while creating ToolProxy");
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("Error while closing buffered reader", e);
					return null;
				}
			}
		}
	}
}
