package org.prosolo.services.upload.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.FileUploadException;
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

			AmazonS3 s3 = s3Provider.getS3Client();

			PutObjectRequest putObjectRequest=new PutObjectRequest(bucketName, key, sourceInputStream, objectMetadata);
			putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
		    s3.putObject(putObjectRequest);

		    // encode the file name in the returned relative file URL
			String encodeFilenameInUrl = encodeFilenameInUrl(key);
			return encodeFilenameInUrl;
		} catch (IOException | AmazonClientException e) {
			logger.error("AmazonService exception for bucket:" + bucketName, e);
			throw new FileUploadException();
		} catch (Exception ex) {
			logger.error("error", ex);
			throw new FileUploadException();
		}
	}
	
	public InputStream retrieveFile(String key) {
		AmazonS3 s3 = s3Provider.getS3Client();
		S3Object s3Object = s3.getObject(bucketName, key);
		return s3Object.getObjectContent();
	}

	public String encodeFilenameInUrl(String relativeFileUrl) throws UnsupportedEncodingException {
		int secondSlashIndex = StringUtils.ordinalIndexOf(relativeFileUrl, "/", 2);

		return relativeFileUrl.substring(0, secondSlashIndex+1) + URLEncoder.encode(relativeFileUrl.substring(secondSlashIndex+1), java.nio.charset.StandardCharsets.UTF_8.toString());
	}

}
