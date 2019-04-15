package org.prosolo.config.fileManagement;

import org.prosolo.util.StringUtils;
import org.simpleframework.xml.Element;

public class FileManagementConfig {

	@Element(name = "images")
	public ImagesConfig imagesConfig;
	
	@Element(name = "uplaod-path")
	public String uploadPath;
	
	@Element(name = "url-prefix-folder")
	public String urlPrefixFolder;

	@Element(name = "max-file-upload-size")
	public int maxFileUploadSize;

	@Override
	public String toString() {
		return StringUtils.toStringByReflection(this);
	}

	public int getMaxFileUploadSize() {
		return maxFileUploadSize;
	}
}