package org.prosolo.common.config.services;

import java.io.File;

import org.simpleframework.xml.Element;

public class FileStoreConfig {

	@Element(name = "virtual-hosting-url", required=false)
	public String virtualHostingUrl;

	@Element(name = "file-store-service-url")
	public String fileStoreServiceUrl;
	
	@Element(name = "file-store-bucket-name")
	public String fileStoreBucketName;

	public String getFilePath(){
		String path= fileStoreServiceUrl + "/" +fileStoreBucketName + "/";
		if(virtualHostingUrl!=null && virtualHostingUrl.length()>0){
			path= virtualHostingUrl+ "/";
		}
		return path;
	}

}
