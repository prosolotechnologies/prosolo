/**
 * 
 */
package org.prosolo.services.upload.impl;

import java.io.IOException;

import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.services.nodes.data.statusWall.AttachmentPreview;
import org.prosolo.services.upload.AmazonS3UploadManager;
import org.prosolo.services.upload.ImageUtil;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.util.FileUtil;
import org.prosolo.util.urigenerator.AmazonS3Utility;
import org.prosolo.util.urigenerator.MD5HashUtility;
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
	public AttachmentPreview uploadFile(String fileName, UploadedFile uploadedFile)
			throws IOException {
		AttachmentPreview attachmentPreview = new AttachmentPreview();
		attachmentPreview.setInitialized(true);
		String fullPath = storeFile(uploadedFile, fileName);

		// link
		attachmentPreview.setLink(fullPath);
		
		attachmentPreview.setFileName(fileName);
		
		// title
		String title = fileName;
		int lastDotIndex = title.lastIndexOf(".");
		
		if (lastDotIndex != -1) {
			title = title.substring(0, lastDotIndex);
		}
		attachmentPreview.setTitle(title);
		
		// description
		//htmlPage.setDescription("Document description");
		
		if (ImageUtil.checkIfImage(fullPath)) {
			attachmentPreview.setImageUrl(fullPath);
		}
		
		attachmentPreview.setContentType(ContentType1.FILE);
		
		return attachmentPreview;
	}
	
	@Override
	public String storeFile(UploadedFile uploadedFile, String fileName) throws IOException {
		String key = MD5HashUtility.generateKeyForFilename(fileName);
		String fileType = FileUtil.getFileType(uploadedFile.getFileName());

		String fileUrlWithEncodedFilenam = s3Manager.storeInputStreamByKey(uploadedFile.getInputstream(), key, fileType);
		
		return AmazonS3Utility.createFullPathFromRelativePath(fileUrlWithEncodedFilenam);
	}
	
}
