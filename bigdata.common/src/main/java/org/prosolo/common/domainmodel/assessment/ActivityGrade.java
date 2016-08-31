package org.prosolo.common.domainmodel.assessment;

import javax.persistence.Entity;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class ActivityGrade extends BaseEntity {

	private static final long serialVersionUID = 7435891438770153804L;
	
	private Integer value;

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}
	
}
