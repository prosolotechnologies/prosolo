/**
 * 
 */
package org.prosolo.common.domainmodel.activities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.activities.RecommendationType;

/**
 * @author Nikola Milikic
 * 
 */
@Entity
public class Recommendation extends BaseEntity {

	private static final long serialVersionUID = 8822023220832508913L;

	private User maker;
	private boolean accepted;
	private boolean dismissed;

	private User recommendedTo;
	private Node recommendedResource;
	private RecommendationType recommendationType;

	public Recommendation() {
	}

	public Recommendation(RecommendationType type) {
		setRecommendationType(type);
	}
	
	@OneToOne (fetch=FetchType.LAZY)
	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		if (null != maker) {
			this.maker = maker;
		}
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public User getRecommendedTo() {
		return recommendedTo;
	}

	public void setRecommendedTo(User recommendedTo) {
		if (null != recommendedTo) {
			this.recommendedTo = recommendedTo;
		}
	}

	@OneToOne(fetch = FetchType.LAZY)
	public Node getRecommendedResource() {
		return recommendedResource;
	}

	public void setRecommendedResource(Node recommendedResource) {
		if (null != recommendedResource) {
			this.recommendedResource = recommendedResource;
		}
	}

	@Enumerated(EnumType.STRING)
	public RecommendationType getRecommendationType() {
		return recommendationType;
	}

	public void setRecommendationType(RecommendationType recommendationType) {
		this.recommendationType = recommendationType;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isDismissed() {
		return dismissed;
	}

	public void setDismissed(boolean dismissed) {
		this.dismissed = dismissed;
	}

}
