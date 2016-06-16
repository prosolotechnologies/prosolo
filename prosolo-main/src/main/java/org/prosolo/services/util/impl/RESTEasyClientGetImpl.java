package org.prosolo.services.util.impl;

import java.io.BufferedReader; 
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.prosolo.services.util.RESTEasyClientGet;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.util.impl.RESTEasyClient")
public class RESTEasyClientGetImpl implements RESTEasyClientGet{
	private static Logger logger = Logger.getLogger(RESTEasyClientGetImpl.class
			.getName());
	@Override
	public String sendRestGetRequest(String url) {
		String output = "";
		try {
			Client client = ClientBuilder.newClient();
					//Client.create();
			 
			WebTarget webTarget = client.target(url);
			Invocation.Builder invocationBuilder =
			        webTarget.request(MediaType.APPLICATION_JSON);
			//ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
			Response response = invocationBuilder.get();
			if (response.getStatus() != 200) {
			   throw new RuntimeException("Failed : HTTP error code : "	+ response.getStatus());
			}
	 
			// output = response.getEntity(String.class);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(response.readEntity(String.class).getBytes())));
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
