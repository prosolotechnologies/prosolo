/**
 * 
 */
package org.prosolo.services.upload.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.primefaces.model.UploadedFile;
import org.prosolo.app.Settings;
import org.prosolo.domainmodel.content.ContentType;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.upload.AmazonS3UploadManager;
import org.prosolo.services.upload.ImageUtil;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.util.FileUtil;
import org.prosolo.util.urigenerator.AmazonS3Utility;
import org.prosolo.util.urigenerator.MD5HashUtility;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.upload.UploadManager")
public class UploadManagerImpl implements UploadManager {
	
	@Autowired private AmazonS3UploadManager s3Manager;
	
	@Override
	public AttachmentPreview uploadFile(User user, UploadedFile uploadedFile, String fileName) throws IOException {
		AttachmentPreview attachmentPreview = new AttachmentPreview();
		attachmentPreview.setInitialized(true);
		String fullPath = storeFile(user, uploadedFile, fileName);

		// link
		attachmentPreview.setLink(fullPath);
		
		// title
		String title = fileName;
		int lastDotIndex = title.lastIndexOf(".");
		
		if (lastDotIndex != -1) {
			title = title.substring(0, lastDotIndex);
		}
		attachmentPreview.setUploadTitle(title);
		
		// description
		//htmlPage.setDescription("Document description");
		
		if (ImageUtil.checkIfImage(fullPath)) {
			attachmentPreview.setImage(fullPath);
		}
		
		attachmentPreview.setContentType(ContentType.UPLOAD);
		
		return attachmentPreview;
	}
	
	@Override
	public String storeFile(User user, UploadedFile uploadedFile, String fileName) throws IOException {
		String key = MD5HashUtility.generateKeyForFilename(fileName);
		//Don't delete this. It temporary stores files to
		//local directory for later indexing
		File tempFile = new File(Settings.getInstance().config.fileManagement.uploadPath + File.separator + key);
		String fileType = FileUtil.getFileType(tempFile);
	
		FileUtils.copyInputStreamToFile(
				uploadedFile.getInputstream(), 
				tempFile);
	
		s3Manager.storeInputStreamByKey(uploadedFile.getInputstream(), key,fileType);
		
		String fullPath = AmazonS3Utility.createFullPathFromRelativePath(key);
		return fullPath;
	} 
	
}
