package org.prosolo.bigdata.common.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.rest.exceptions.ConnectException;
 

/**
@author Zoran Jeremic Apr 20, 2015
 *
 */

public class RestClient {
	private final static Logger logger = Logger
			.getLogger(RestClient.class);
	public String sendGetRequest(String stringUrl) {
		StringBuilder builder = new StringBuilder();
		try {
			URL url = new URL(stringUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			while ((output = br.readLine()) != null) {
				builder.append(output);
			}
			conn.disconnect();

		} catch (MalformedURLException e) {
			logger.error("MalformedURLException while trying to connect to external service:"+stringUrl,e);
		//	e.printStackTrace();

		} catch (IOException e) {
			logger.error("IOException while trying to connect to external service:"+stringUrl,e);
			//e.printStackTrace();

		}
		return builder.toString();
	}
	public String sendPostRequest(String stringUrl, String jsonInput) throws ConnectException {
		StringBuilder builder = new StringBuilder();
		try {
			URL url = new URL(stringUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			OutputStream os = conn.getOutputStream();
			os.write(jsonInput.getBytes());
			os.flush();
			
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			while ((output = br.readLine()) != null) {
				builder.append(output);
			}
			conn.disconnect();

		} catch (MalformedURLException e) {
			logger.error("MalformedURLException while trying to connect to external service:"+stringUrl,e);
		} catch (IOException e) {
			logger.error("IOException while trying to connect to external service:"+stringUrl,e);
			
			if (e instanceof java.net.ConnectException) {
				throw new ConnectException("There is an error with conecting the server at URL: " + stringUrl);
			}
		}catch(Exception e){
			logger.error("Exception while trying to connect to external service:"+stringUrl,e);
		}
		return builder.toString();
	}
}

