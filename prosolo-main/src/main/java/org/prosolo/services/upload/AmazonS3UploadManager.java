package org.prosolo.services.upload;

import java.io.InputStream;
import org.prosolo.bigdata.common.exceptions.FileUploadException;


/**
 *
 * @author Zoran Jeremic, May 22, 2014
 *
 */
public interface AmazonS3UploadManager {

	/**
	 *
	 * @param sourceInputStream
	 * @param fileName
	 * @param fileType
	 * @return
	 * @throws FileUploadException
	 */
	String storeInputStreamByKey( InputStream sourceInputStream, String fileName, String fileType);

	String storeInputStreamByFileName(InputStream sourceInputStream,
			String fileName, String fileType);

}