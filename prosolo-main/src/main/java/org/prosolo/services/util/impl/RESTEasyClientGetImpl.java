package org.prosolo.services.util.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.prosolo.services.util.RESTEasyClientGet;
import org.springframework.stereotype.Service;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@Service("org.prosolo.services.util.impl.RESTEasyClient")
public class RESTEasyClientGetImpl implements RESTEasyClientGet{
	private static Logger logger = Logger.getLogger(RESTEasyClientGetImpl.class
			.getName());
	@Override
	public String sendRestGetRequest(String url) {
		String output = "";
		try {
			Client client = Client.create();
			 
			WebResource webResource = client.resource(url);
	 
			ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
	 
			if (response.getStatus() != 200) {
			   throw new RuntimeException("Failed : HTTP error code : "	+ response.getStatus());
			}
	 
			// output = response.getEntity(String.class);

			BufferedReader br = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(response.getEntity(String.class).getBytes())));
			String line;
			
			while ((line = br.readLine()) != null) {
				output = output + line;
			}
		} catch (ClientProtocolException e) {
			logger.error(e.getLocalizedMessage());
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
		return output;
	}

}
