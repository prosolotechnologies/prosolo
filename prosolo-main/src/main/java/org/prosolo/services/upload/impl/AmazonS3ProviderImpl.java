package org.prosolo.services.upload.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.prosolo.services.upload.AmazonS3Provider;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 *
 * @author Zoran Jeremic, May 22, 2014
 *
 */
@Service("org.prosolo.services.upload.AmazonS3Provider")
public class AmazonS3ProviderImpl implements AmazonS3Provider {
	
	private Logger logger = Logger.getLogger(AmazonS3ProviderImpl.class);
	
	private AmazonS3 s3;
	
	private static final String absoluteAwsPath = System.getProperty("user.home") 
			+ File.separator + ".aws" + File.separator;
	
	@Override
	public AmazonS3 getS3Client(){
		if (s3 == null) {
			try {
				init();
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return s3;
	}
	
	private void init() throws Exception {

        /*
		* The ProfileCredentialsProvider will return your [default]
		* credential profile by reading from the credentials file located at
		* (~/.aws/credentials).
		*/
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
        	credentials =uploadCredentials();
        }
        s3 = new AmazonS3Client(credentials);
    }
	
	private AWSCredentials uploadCredentials(){
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("config/credentials");
		OutputStream resStreamOut = null;
	    int readBytes;
	    byte[] buffer = new byte[4096];
	    
	    try {
	    	new File(absoluteAwsPath).mkdirs();
	        resStreamOut = new FileOutputStream(new File(absoluteAwsPath + "credentials"));
	        
	        while ((readBytes = is.read(buffer)) > 0) {
	            resStreamOut.write(buffer, 0, readBytes);
	        }
	       return new ProfileCredentialsProvider().getCredentials();
	    } catch (IOException e1) {
	    	logger.error(e1);
	    } finally {
	        try {
				is.close();
			} catch (IOException e) {
				logger.error(e);
			}
	        
	        try {
				resStreamOut.close();
			} catch (IOException e) {
				logger.error(e);
			}
	    }
	    return null;
	}
}
