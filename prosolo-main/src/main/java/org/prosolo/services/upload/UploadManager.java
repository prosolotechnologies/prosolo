/**
 * 
 */
package org.prosolo.services.upload;

import java.io.IOException;

import org.primefaces.model.UploadedFile;
import org.prosolo.domainmodel.user.User;
import org.prosolo.web.activitywall.data.AttachmentPreview;

/**
 * @author "Nikola Milikic"
 * 
 */
public interface UploadManager {

	String storeFile(User user, UploadedFile uploadedFile, String fileName)
			throws IOException;

	AttachmentPreview uploadFile(User user, UploadedFile uploadedFile,
			String fileName) throws IOException;

}