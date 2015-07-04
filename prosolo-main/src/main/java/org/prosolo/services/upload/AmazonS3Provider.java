package org.prosolo.services.upload;

import com.amazonaws.services.s3.AmazonS3;

/**
 *
 * @author Zoran Jeremic, May 22, 2014
 *
 */
public interface AmazonS3Provider {

	AmazonS3 getS3Client();

}
