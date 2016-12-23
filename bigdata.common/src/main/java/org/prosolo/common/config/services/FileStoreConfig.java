package org.prosolo.common.config.services;

import org.prosolo.common.config.CommonSettings;
import org.simpleframework.xml.Element;

import java.io.File;

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
			path= virtualHostingUrl+ File.separator;
		}
		return path;
	}

}
