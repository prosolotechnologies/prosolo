/**
 * 
 */
package org.prosolo.web.portfolio.data;

import java.io.Serializable;

/**
 * @author "Nikola Milikic"
 * 
 */
public class SocialNetworksData implements Serializable {

	private static final long serialVersionUID = 2744838596870425737L;

	private long id;
	private String twitterLink = "";
	private String twitterLinkEdit = "";
	private String facebookLink = "";
	private String facebookLinkEdit = "";
	private String gplusLink = "";
	private String gplusLinkEdit = "";
	private String blogLink = "";
	private String blogLinkEdit = "";

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTwitterLink() {
		return twitterLink;
	}

	public void setTwitterLink(String twitterLink) {
		this.twitterLink = twitterLink;
	}
	
	public String getTwitterLinkEdit() {
		return twitterLinkEdit;
	}

	public void setTwitterLinkEdit(String twitterLinkEdit) {
		this.twitterLinkEdit = twitterLinkEdit;
	}

	public String getFacebookLink() {
		return facebookLink;
	}

	public void setFacebookLink(String facebookLink) {
		this.facebookLink = facebookLink;
	}

	public String getFacebookLinkEdit() {
		return facebookLinkEdit;
	}

	public void setFacebookLinkEdit(String facebookLinkEdit) {
		this.facebookLinkEdit = facebookLinkEdit;
	}

	public String getGplusLink() {
		return gplusLink;
	}

	public void setGplusLink(String gplusLink) {
		this.gplusLink = gplusLink;
	}

	public String getGplusLinkEdit() {
		return gplusLinkEdit;
	}

	public void setGplusLinkEdit(String gplusLinkEdit) {
		this.gplusLinkEdit = gplusLinkEdit;
	}

	public String getBlogLink() {
		return blogLink;
	}

	public void setBlogLink(String blogLink) {
		this.blogLink = blogLink;
	}

	public String getBlogLinkEdit() {
		return blogLinkEdit;
	}

	public void setBlogLinkEdit(String blogLinkEdit) {
		this.blogLinkEdit = blogLinkEdit;
	}

}
