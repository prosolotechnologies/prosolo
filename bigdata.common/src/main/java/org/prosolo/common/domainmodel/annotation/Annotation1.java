package org.prosolo.common.domainmodel.annotation;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class Annotation1 extends BaseEntity {

	private static final long serialVersionUID = -7054377604777009891L;

	/*
	 * combination of maker, annotatedResourceId, annotatedResource, annotationType
	 * uniquely identify Annotation
	 */
	private User maker;
    private long annotatedResourceId;
    private AnnotatedResource annotatedResource;
	private AnnotationType annotationType;

	public Annotation1() {

	}
	
	@Enumerated(EnumType.STRING)
	public AnnotationType getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(AnnotationType annotationType) {
		this.annotationType = annotationType;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		this.maker = maker;
	}

	public long getAnnotatedResourceId() {
		return annotatedResourceId;
	}

	public void setAnnotatedResourceId(long annotatedResourceId) {
		this.annotatedResourceId = annotatedResourceId;
	}

	@Enumerated(EnumType.STRING)
	public AnnotatedResource getAnnotatedResource() {
		return annotatedResource;
	}

	public void setAnnotatedResource(AnnotatedResource annotatedResource) {
		this.annotatedResource = annotatedResource;
	}

}