/**
 * 
 */
package org.prosolo.services.nodes.data.activity.attachmentPreview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.common.domainmodel.content.ImageSize;
import org.prosolo.services.nodes.data.ActivityType;

public class AttachmentPreview1 implements Serializable {

	private static final long serialVersionUID = 7923932956660196648L;

	private long id;
	private String title;
	private String description;
	private String link;
	private String domain;
	private String imageUrl;
	private ImageSize imageSize;
	private String fileName;
	private ContentType1 contentType;
	private String embedingLink;
	private String embedId;
	private MediaType1 mediaType;

	// used for new post dialog
	private boolean invalidLink;
	private List<String> images;
	private int selectedImageIndex;
	private boolean initialized = false;
	
	//for learning resource preview
	private boolean universityCreated;
	private String creatorName;
	private String duration;
	private ActivityType activityType;
	
	//for twitter post
	private String nickname;
	private String profileUrl;
	
	
	public AttachmentPreview1() {
		this.images = new ArrayList<String>();
		this.selectedImageIndex = -1;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public MediaType1 getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType1 mediaType) {
		this.mediaType = mediaType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public ContentType1 getContentType() {
		return contentType;
	}

	public void setContentType(ContentType1 contentType) {
		this.contentType = contentType;
	}

	public String getEmbedingLink() {
		return embedingLink;
	}

	public void setEmbedingLink(String embedingLink) {
		this.embedingLink = embedingLink;
	}

	public boolean isInvalidLink() {
		return invalidLink;
	}

	public void setInvalidLink(boolean invalidLink) {
		this.invalidLink = invalidLink;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
		
		if (images != null && !images.isEmpty()) {
			this.selectedImageIndex = 0;
			this.imageUrl = images.get(0);
		}
	}

	public int getSelectedImageIndex() {
		return selectedImageIndex;
	}

	public void setSelectedImageIndex(int selectedImageIndex) {
		this.selectedImageIndex = selectedImageIndex;
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public ImageSize getImageSize() {
		return imageSize;
	}

	public void setImageSize(ImageSize imageSize) {
		this.imageSize = imageSize;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return "AttachmentPreview [title=" + title + ", description="
				+ description + ", link=" + link + ", image=" + imageUrl
				+ ", contentType=" + contentType + ", embedingLink="
				+ embedingLink + ", mediaType=" + mediaType + "]";
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public boolean isUniversityCreated() {
		return universityCreated;
	}

	public void setUniversityCreated(boolean universityCreated) {
		this.universityCreated = universityCreated;
	}

	public ActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
	}

	public String getEmbedId() {
		return embedId;
	}

	public void setEmbedId(String embedId) {
		this.embedId = embedId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

}
