package org.prosolo.web.useractions.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.organization.VisibilityType;

public class NewPostData implements Serializable {
	
	private static final long serialVersionUID = 765125035780761493L;

	// not used in post dialog itself, but used for storing link's title
	private String title;
	private String text;
	private String link;
	private VisibilityType visibility;
	private boolean connectWithStatus;
	private String mentionedUsers;
	
	public NewPostData() {
		this.visibility = VisibilityType.PUBLIC;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link.trim();
	}

	public VisibilityType getVisibility() {
		return visibility;
	}

	public void setVisibility(VisibilityType visibility) {
		this.visibility = visibility;
	}

	public boolean isConnectWithStatus() {
		return connectWithStatus;
	}

	public void setConnectWithStatus(boolean connectWithStatus) {
		this.connectWithStatus = connectWithStatus;
	}

	public String getMentionedUsers() {
		return mentionedUsers;
	}

	public void setMentionedUsers(String mentionedUsers) {
		this.mentionedUsers = mentionedUsers;
	}

}
