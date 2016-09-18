package org.prosolo.services.nodes.data;

public class AnnouncementData {

	private String creatorFullName;
	private String formattedCreationDate;
	private String text;
	private String title;
	private String creatorAvatarUrl;
	private String encodedId;
	private long id;

	public String getCreatorFullName() {
		return creatorFullName;
	}

	public void setCreatorFullName(String creatorFullName) {
		this.creatorFullName = creatorFullName;
	}

	public String getFormattedCreationDate() {
		return formattedCreationDate;
	}

	public void setFormattedCreationDate(String formattedCreationDate) {
		this.formattedCreationDate = formattedCreationDate;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCreatorAvatarUrl() {
		return creatorAvatarUrl;
	}

	public void setCreatorAvatarUrl(String creatorAvatarUrl) {
		this.creatorAvatarUrl = creatorAvatarUrl;
	}

	public String getEncodedId() {
		return encodedId;
	}

	public void setEncodedId(String encodedId) {
		this.encodedId = encodedId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}