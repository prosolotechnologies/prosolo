package org.prosolo.domainmodel.annotation;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.prosolo.domainmodel.annotation.AnnotationType;
import org.prosolo.domainmodel.annotation.SimpleAnnotation;
import org.prosolo.domainmodel.workflow.Scale;

@Entity
public class UserRating extends SimpleAnnotation {

	private static final long serialVersionUID = 7640153224837706191L;

	private double ratingValue;
	private Scale scale;
	
	public UserRating(){
		super(AnnotationType.UserRating);
	}

	public double getRatingValue() {
		return ratingValue;
	}

	public void setRatingValue(double ratingValue) {
		this.ratingValue = ratingValue;
	}
	
	@OneToOne
	public Scale getScale() {
		return scale;
	}
	
	public void setScale(Scale scale) {
		this.scale = scale;
	}
}
