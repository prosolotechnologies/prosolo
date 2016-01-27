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

	@Element(name = "buffer-size")
	public int bufferSize;	

	@Override
	public String toString() {
		return StringUtils.toStringByReflection(this);
	}

}