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
	
	@Element(name = "file-store-service-url")
	public String fileStoreServiceUrl;
	
	@Element(name = "file-store-bucket-name")
	public String fileStoreBucketName;
	
	

	@Override
	public String toString() {
		return StringUtils.toStringByReflection(this);
	}

}