package org.prosolo.config.observation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ObservationConfigLoader {

	private static final String FILE_NAME = "observation_config.json";
	private static final String FILE_PATH = "config/";

	public static ObservationConfig loadObservationConfig() throws Exception {
		Gson gson = new GsonBuilder().create();
		BufferedReader br = null;
		try {
			URL url = Thread.currentThread().getContextClassLoader().getResource(FILE_PATH + FILE_NAME);
			if (url != null) {
				String path = url.getFile();
				path = path.replaceAll("%20", " ");
				br = new BufferedReader(new FileReader(path));
				return gson.fromJson(br, ObservationConfig.class);
			}else{
				throw new Exception();
			}
		} catch (Exception e) {
			throw new Exception("Error while loading roles and capabilities");
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					return null;
				}
			}
		}
	}
}



