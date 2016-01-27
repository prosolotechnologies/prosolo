package org.prosolo.recommendation.impl;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;

/**
zoran
 */

public class RecommendedDocument implements Serializable {
	
	private static Logger logger = Logger.getLogger(RecommendedDocument.class);

	private static final long serialVersionUID = -8106528154836461966L;
	
	private String title;
	private String filename;
	private String path;
	private String link;
	private DocumentType documentType;
	
	public RecommendedDocument(SearchHit searchHit) {
		Map<String, SearchHitField> hitSource = searchHit.getFields();//.getSource();
		String type = (String) hitSource.get("contentType").getValue().toString().toUpperCase();
		
		if (hitSource.containsKey("title")) {
			this.title = (String) hitSource.get("title").getValue();
		} else {
			this.title = (String) hitSource.get("url").getValue();
		}

		if (type.equals(DocumentType.DOCUMENT.name())) {
			this.documentType = DocumentType.DOCUMENT;
			setPath(createLinkToFileFromKey((String) hitSource.get("url").getValue()));
		} else if (type.equals(DocumentType.WEBPAGE.name())) {
			this.documentType = DocumentType.WEBPAGE;
			setPath((String) hitSource.get("url").getValue());
		} else {
			logger.info("Document type is nor valid:" + type);
		}
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private String createLinkFromPath(String path){
		String link = path.replaceFirst(Settings.getInstance().config.fileManagement.uploadPath, "");
		link = Settings.getInstance().config.fileManagement.urlPrefixFolder + link;
 		return link;
	}
	
	private String createLinkToFileFromKey(String key){
		return CommonSettings.getInstance().config.fileStore.fileStoreServiceUrl + "/" + 
				CommonSettings.getInstance().config.fileStore.fileStoreBucketName + "/" + 
				key;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public DocumentType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(DocumentType documentType) {
		this.documentType = documentType;
	}

	public boolean isWebPage() {
		return this.documentType.equals(DocumentType.WEBPAGE);
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

}
