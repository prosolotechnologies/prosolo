package org.prosolo.services.nodes.data.activity.attachmentPreview;

public class MediaData {

	private MediaType1 mediaType;
	private String embedLink;
	
	public MediaData(MediaType1 mediaType, String embedLink) {
		this.mediaType = mediaType;
		this.embedLink = embedLink;
	}
	
	public MediaType1 getMediaType() {
		return mediaType;
	}
	public void setMediaType(MediaType1 mediaType) {
		this.mediaType = mediaType;
	}
	public String getEmbedLink() {
		return embedLink;
	}
	public void setEmbedLink(String embedLink) {
		this.embedLink = embedLink;
	}
	
	
}
