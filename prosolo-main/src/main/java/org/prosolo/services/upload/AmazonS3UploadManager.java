package org.prosolo.services.upload;

import java.io.InputStream;


/**
 *
 * @author Zoran Jeremic, May 22, 2014
 *
 */
public interface AmazonS3UploadManager {

	String storeInputStreamByKey( InputStream sourceInputStream, String fileName, String fileType);

	String storeInputStreamByFileName(InputStream sourceInputStream,
			String fileName, String fileType);

}