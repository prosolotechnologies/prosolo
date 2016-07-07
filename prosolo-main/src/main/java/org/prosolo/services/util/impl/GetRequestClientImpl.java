package org.prosolo.services.util.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.prosolo.services.util.GetRequestClient;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.util.impl.GetRequestClient")
public class GetRequestClientImpl implements GetRequestClient {
	private static Logger logger = Logger.getLogger(GetRequestClientImpl.class
			.getName());
	@Override
	public String sendRestGetRequest(String url) {
		String output = "";
		try {
			StringBuilder result = new StringBuilder();
		    URL urlObj = new URL(url);
		    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
		    conn.setRequestMethod("GET");
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String line;
		    while ((line = rd.readLine()) != null) {
		       result.append(line);
		    }
		    rd.close();
		    output = result.toString();
		} catch(Exception e) {
			logger.error(e);
		}
		return output;
	}

}
