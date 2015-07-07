package org.prosolo.web.home.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.featuredNews.FeaturedNews;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;

/*
 * @author Zoran Jeremic 2013-05-23
 */
public class FeaturedNewsData implements Serializable {
	
	private static final long serialVersionUID = -8254869762287693388L;

	private String personName;
	private String resourceName;
	private String creatorAvatar;
	private String actionLabel;
	private String prettyDateCreated;
	private FeaturedNews featuredNews;
	private User actor;
	private Node resource;

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getCreatorAvatar() {
		return creatorAvatar;
	}

	public void setCreatorAvatar(String creatorAvatar) {
		this.creatorAvatar = creatorAvatar;
	}

	public String getActionLabel() {
		return actionLabel;
	}

	public void setActionLabel(String actionLabel) {
		this.actionLabel = actionLabel;
	}

	public String getPrettyDateCreated() {
		return prettyDateCreated;
	}

	public void setPrettyDateCreated(String prettyDateCreated) {
		this.prettyDateCreated = prettyDateCreated;
	}

	public FeaturedNews getFeaturedNews() {
		return featuredNews;
	}

	public void setFeaturedNews(FeaturedNews featuredNews) {
		this.featuredNews = featuredNews;
	}

	public User getActor() {
		return actor;
	}

	public void setActor(User actor) {
		this.actor = actor;
	}

	public Node getResource() {
		return resource;
	}

	public void setResource(Node resource) {
		this.resource = resource;
	}

}
