package org.prosolo.domainmodel.outcomes;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.prosolo.domainmodel.general.BaseEntity;

/**
@author Zoran Jeremic Dec 26, 2014
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Outcome  extends BaseEntity {

	private static final long serialVersionUID = 5991500189156878656L;
	
}

