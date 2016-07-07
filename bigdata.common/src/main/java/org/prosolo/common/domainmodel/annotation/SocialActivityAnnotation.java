package org.prosolo.common.domainmodel.annotation;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.annotation.Annotation;
import org.prosolo.common.domainmodel.annotation.AnnotationType;

/**
@author Zoran Jeremic Jan 1, 2014
 */
@Entity
public class SocialActivityAnnotation extends Annotation {

	private static final long serialVersionUID = -1133497172984159393L;

	private SocialActivity socialActivity;
	
	public SocialActivityAnnotation(AnnotationType ann) {
		super(ann);
	}
	
	public SocialActivityAnnotation(){}

	@Transient
	@Override
	public BaseEntity getResource() {
		return this.socialActivity;
	}

	@Override
	public void setResource(BaseEntity resource) {
		this.socialActivity = (SocialActivity) resource;
	}
	
	@OneToOne
	public SocialActivity getSocialActivity() {
		return socialActivity;
	}

	public void setSocialActivity(SocialActivity socialActivity) {
		this.socialActivity = socialActivity;
	}

}
