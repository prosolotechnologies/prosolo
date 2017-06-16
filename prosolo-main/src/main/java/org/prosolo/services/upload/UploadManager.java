/**
 * 
 */
package org.prosolo.services.upload;

import java.io.IOException;

import org.primefaces.model.UploadedFile;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview1;

/**
 * @author "Nikola Milikic"
 * 
 */
public interface UploadManager {

	String storeFile(UploadedFile uploadedFile, String fileName)
			throws IOException;

	AttachmentPreview1 uploadFile(String fileName, UploadedFile uploadedFile) 
			throws IOException;

}