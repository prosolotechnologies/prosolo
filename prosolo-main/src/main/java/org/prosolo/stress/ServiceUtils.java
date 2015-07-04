package org.prosolo.stress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
 

/**
@author Zoran Jeremic Nov 19, 2013
 */

public class ServiceUtils {
	private static final Logger LOGGER = Logger.getLogger(ServiceUtils.class);
	public static String convertInputStreamToString(InputStream is){
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    try {
			while ((line = reader.readLine()) != null) {
			  sb.append(line + "\n");
			}
			is.close();
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error:"+e.getLocalizedMessage());
		}
		return sb.toString();		
	}
}
