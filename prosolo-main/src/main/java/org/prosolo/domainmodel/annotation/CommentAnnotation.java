package org.prosolo.domainmodel.annotation;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.activitywall.comments.Comment;
import org.prosolo.domainmodel.annotation.Annotation;
import org.prosolo.domainmodel.annotation.AnnotationType;
import org.prosolo.domainmodel.general.BaseEntity;
 
/**
@author Zoran Jeremic Jan 1, 2014
 */
@Entity
public class CommentAnnotation extends Annotation{

	private static final long serialVersionUID = 1641998210345790940L;
	
	private Comment comment;
	
	public CommentAnnotation(){}
	
	public CommentAnnotation(AnnotationType ann) {
		super(ann);
	}

	@Transient
	@Override
	public BaseEntity getResource() {
		return this.comment;
	}

	@Override
	public void setResource(BaseEntity resource) {
		this.comment = (Comment) resource;
	}
	
	@OneToOne (fetch = FetchType.LAZY)
	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

}
