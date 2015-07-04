package org.prosolo.common.domainmodel.annotation;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.annotation.Annotation;
import org.prosolo.common.domainmodel.annotation.AnnotationType;

/**
@author Zoran Jeremic Jan 1, 2014
 */
@Entity
public class SimpleAnnotation extends Annotation{

	private static final long serialVersionUID = 1381289791282159947L;
	
	public SimpleAnnotation(){}

	public SimpleAnnotation(AnnotationType annType) {
		super(annType);
	}

	@Transient
	@Override
	public BaseEntity getResource() {
		return null;
	}

	@Override
	public void setResource(BaseEntity resource) {}

}
