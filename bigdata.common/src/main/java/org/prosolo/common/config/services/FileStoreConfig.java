package org.prosolo.common.config.services;

import org.simpleframework.xml.Element;

public class FileStoreConfig {

	
	@Element(name = "file-store-service-url")
	public String fileStoreServiceUrl;
	
	@Element(name = "file-store-bucket-name")
	public String fileStoreBucketName;

}
