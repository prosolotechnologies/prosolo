package org.prosolo.common.domainmodel.activities;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.Node;

@Entity
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
