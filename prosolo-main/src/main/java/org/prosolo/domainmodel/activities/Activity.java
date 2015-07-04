package org.prosolo.domainmodel.activities;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.general.Node;

@Entity
////@Table(name="act_Activity")
public class Activity extends Node {

	private static final long serialVersionUID = 5698106325669307105L;

	private Boolean mandatory;

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public Boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

}
