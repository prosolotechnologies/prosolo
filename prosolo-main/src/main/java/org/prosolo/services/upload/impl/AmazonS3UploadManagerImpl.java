package org.prosolo.services.upload.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.services.upload.AmazonS3Provider;
import org.prosolo.services.upload.AmazonS3UploadManager;
import org.prosolo.util.urigenerator.MD5HashUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;


/**
 *
 * @author Zoran Jeremic, May 22, 2014
 *
 */
@Service("org.prosolo.services.upload.AmazonS3UploadManager")
public class AmazonS3UploadManagerImpl implements AmazonS3UploadManager {
	
	private Logger logger = Logger.getLogger(AmazonS3UploadManagerImpl.class);

	@Autowired private AmazonS3Provider s3Provider;
	private String bucketName = CommonSettings.getInstance().config.fileStore.fileStoreBucketName;

	@Override
	public String storeInputStreamByFileName(InputStream sourceInputStream, String fileName, String fileType){
		String key = storeInputStreamByKey(sourceInputStream, MD5HashUtility.generateKeyForFilename(fileName), fileType);
		return key;
	}
	
	@Override
	public String storeInputStreamByKey(InputStream sourceInputStream, String key, String fileType) {
		ObjectMetadata objectMetadata = new ObjectMetadata();
		
		try {
			sourceInputStream.available();
			objectMetadata.setContentLength(sourceInputStream.available());
			
			if (fileType != null) {
				objectMetadata.setContentType(fileType);
			}
		} catch (IOException e) {
			logger.error("AmazonService IOException for bucket:" + bucketName, e);
		}
		
		AmazonS3 s3 = s3Provider.getS3Client();
		try{
			PutObjectRequest putObjectRequest=new PutObjectRequest(bucketName,key, sourceInputStream, objectMetadata);
			putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
		    @SuppressWarnings("unused")
			PutObjectResult result = s3.putObject(putObjectRequest);
		}catch (AmazonServiceException ase) {
	        logger.error("AmazonServiceException for bucket:"+bucketName,ase);
	    } catch (AmazonClientException ace) {
	    	logger.error("AmazonServiceException for bucket:"+bucketName,ace);
		} catch(Exception ex){
			logger.error(ex);
		}
		
		return key;
	}
	
	public InputStream retrieveFile(String key) {
		AmazonS3 s3 = s3Provider.getS3Client();
		S3Object s3Object = s3.getObject(bucketName, key);
		return s3Object.getObjectContent();
	}
	
}
