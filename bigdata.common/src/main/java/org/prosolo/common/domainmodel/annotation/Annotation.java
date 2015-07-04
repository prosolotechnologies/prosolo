package org.prosolo.common.domainmodel.annotation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.annotation.AnnotationType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Annotation extends BaseEntity {

	private static final long serialVersionUID = 3612180881212279434L;

//	private BaseEntity resource;
	private String content;
	private User maker;

	private AnnotationType annotationType;

	public Annotation(AnnotationType annType) {
		setAnnotationType(annType);
	}

	public Annotation() { }

	@Transient
	public abstract BaseEntity getResource();
	
	public abstract void setResource(BaseEntity resource);
	
	@Enumerated(EnumType.STRING)
	public AnnotationType getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(AnnotationType annotationType) {
		this.annotationType = annotationType;
	}

	@Column(name = "content", nullable = true)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@OneToOne (fetch=FetchType.LAZY)
	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		this.maker = maker;
	}

	public boolean isAnnotationOfType(AnnotationType annType) {
		return this.annotationType.equals(annType);
	}

	@Override
	public String toString() {
		return "Annotation [id=" + getId() + ", title=" + getTitle() + ", content=" + content + ", maker=" + maker
				+ ", annotationType=" + annotationType + "]";
	}
	
}