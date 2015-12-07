package org.prosolo.web.home.data;

import java.io.Serializable;
import java.util.Date;

import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.activities.RecommendationType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

/*
 * @author Zoran Jeremic 2013-05-25
 */
public class RecommendationData implements Serializable {
	
	private static final long serialVersionUID = 6038078929220025547L;

	private long id;
	private long makerId;
	private RecommendedResourceType resourceType;
	private RecommendationType recommendationType;
	private ResourceAvailability resourceAvailability;
	private Recommendation recommendation;
	private BaseEntity resource;
	private String resourceTitle;
	private User maker;
	private Date dateCreated;

	public RecommendedResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(RecommendedResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public BaseEntity getResource() {
		return resource;
	}

	public void setResource(BaseEntity resource) {
		this.resource = resource;
	}

	public String getResourceTitle() {
		return resourceTitle;
	}

	public void setResourceTitle(String resourceTitle) {
		this.resourceTitle = resourceTitle;
	}

	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		this.maker = maker;
	}

	public RecommendationType getRecommendationType() {
		return recommendationType;
	}

	public void setRecommendationType(RecommendationType recommendationType) {
		this.recommendationType = recommendationType;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public ResourceAvailability getResourceAvailability() {
		return resourceAvailability;
	}

	public void setResourceAvailability(
			ResourceAvailability resourceAvailability) {
		this.resourceAvailability = resourceAvailability;
	}

	public Recommendation getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(Recommendation recommendation) {
		this.recommendation = recommendation;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getMakerId() {
		return makerId;
	}

	public void setMakerId(long makerId) {
		this.makerId = makerId;
	}
}
