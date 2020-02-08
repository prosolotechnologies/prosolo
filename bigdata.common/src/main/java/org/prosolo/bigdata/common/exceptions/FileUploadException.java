package org.prosolo.bigdata.common.exceptions;

/**
 * Exception that occurs when there is an error during file upload.
 *
 * @author stefanvuckovic
 * @date 2019-08-05
 * @since 1.3.3
 *
 */
public class FileUploadException extends RuntimeException {

	public FileUploadException(){
		this("Error uploading the file");
	}

	public FileUploadException(String message){
		super(message);
	}
}
