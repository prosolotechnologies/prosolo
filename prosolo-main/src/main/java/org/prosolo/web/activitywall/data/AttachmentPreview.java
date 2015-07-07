/**
 * 
 */
package org.prosolo.web.activitywall.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.content.ContentType;

/**
 * @author "Nikola Milikic"
 * 
 */
public class AttachmentPreview implements Serializable {

	private static final long serialVersionUID = 7923932956660196648L;

	private String title;
	private String description;
	private String link;
	private String image;
	private ContentType contentType;
	private String embedingLink;
	private MediaType mediaType;
	private EventType action;
	private String id;
	
	// for upload
	private String uploadTitle;
	private String uploadDescription;
	private String uploadLink;
	
	
	private NodeData resource;
	
	// used for AssignementUploadActivity
	private int duration;
	private boolean visibleToEveryone;
	private int maxFilesNumber;
	private String uploadedAssignmentLink;
	private String uploadedAssignmentTitle;
	
	// used for new post dialog
	private boolean invalidLink;
	private List<String> images;
	private int selectedImageIndex;
	private boolean initialized = false;
	
	public AttachmentPreview() {
		this.images = new ArrayList<String>();
		this.selectedImageIndex = -1;
	}
	
	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType mediaType) {
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

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
	
	public NodeData getResource() {
		return resource;
	}

	public void setResource(NodeData resource) {
		this.resource = resource;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public String getEmbedingLink() {
		return embedingLink;
	}

	public void setEmbedingLink(String embedingLink) {
		this.embedingLink = embedingLink;
	}
	
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public boolean isVisibleToEveryone() {
		return visibleToEveryone;
	}

	public void setVisibleToEveryone(boolean visibleToEveryone) {
		this.visibleToEveryone = visibleToEveryone;
	}

	public int getMaxFilesNumber() {
		return maxFilesNumber;
	}

	public void setMaxFilesNumber(int maxFilesNumber) {
		this.maxFilesNumber = maxFilesNumber;
	}
	
	public String getUploadedAssignmentLink() {
		return uploadedAssignmentLink;
	}

	public void setUploadedAssignmentLink(String uploadedAssignmentLink) {
		this.uploadedAssignmentLink = uploadedAssignmentLink;
	}
	
	public String getUploadedAssignmentTitle() {
		return uploadedAssignmentTitle;
	}

	public void setUploadedAssignmentTitle(String uploadedAssignmentTitle) {
		this.uploadedAssignmentTitle = uploadedAssignmentTitle;
	}
	
	public EventType getAction() {
		return action;
	}

	public void setAction(EventType action) {
		this.action = action;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
			this.image = images.get(0);
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
	
	public String getUploadTitle() {
		return uploadTitle;
	}

	public void setUploadTitle(String uploadTitle) {
		this.uploadTitle = uploadTitle;
	}

	public String getUploadDescription() {
		return uploadDescription;
	}

	public void setUploadDescription(String uploadDescription) {
		this.uploadDescription = uploadDescription;
	}

	public String getUploadLink() {
		return uploadLink;
	}

	public void setUploadLink(String uploadLink) {
		this.uploadLink = uploadLink;
	}

	@Override
	public String toString() {
		return "AttachmentPreview [title=" + title + ", description="
				+ description + ", link=" + link + ", image=" + image
				+ ", contentType=" + contentType + ", embedingLink="
				+ embedingLink + ", mediaType=" + mediaType + ", resource="
				+ resource + "]";
	}
	
	public void reset() {
		this.initialized = false;
		this.link = null;
		this.invalidLink = false;;
		this.title = null;
		this.description = null;
		this.uploadTitle = null;
		this.uploadDescription = null;
		this.uploadLink = null;
		this.images = new ArrayList<String>();;
		this.image = null;
		this.selectedImageIndex = -1;
		this.contentType = null;
	}

}
