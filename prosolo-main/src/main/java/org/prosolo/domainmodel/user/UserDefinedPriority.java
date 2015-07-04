package org.prosolo.domainmodel.user;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.UserPriorityType;
import org.prosolo.domainmodel.workflow.Scale;

/**
 * This class represents the priority of certain 
 * resources (topics, competences, learning goals, etc) 
 * for the given user defined by the user him/herself.
 * @author Jelena
 *
 */

@Entity
//@Table(name="user_UserDefinedPriority")
public class UserDefinedPriority extends BaseEntity {

	private static final long serialVersionUID = -1390549703147678453L;

	/**
	 * the specific level (value) assigned to the (user-defined) priority
	 * using the priorityScale
	 */
	private double priorityLevel;
	
	/**
	 * defines the type of the property; allowed values are
	 * predefined (public static) objects of the PriorityType class 
	 */
	private UserPriorityType priorityType;

	/**
	 * the scale used for defining the level of priority 
	 * (of a specific type of resource, e.g., the priority of competences)
	 */
	private Scale priorityScale;
	
	/**
	 * @return the priorityLevel
	 */
	public double getPriorityLevel() {
		return priorityLevel;
	}

	/**
	 * @param priorityLevel the priorityLevel to set
	 */
	public void setPriorityLevel(double priorityLevel) {
		this.priorityLevel = priorityLevel;
	}

	/**
	 * @return the priorityType
	 */
	@Enumerated(EnumType.STRING)
	public UserPriorityType getPriorityType() {
		return priorityType;
	}

	/**
	 * @param priorityType the priorityType to set
	 */
	public void setPriorityType(UserPriorityType priorityType) {
		this.priorityType = priorityType;
	}

	/**
	 * @return the priorityScale
	 */
	@OneToOne
	public Scale getPriorityScale() {
		return priorityScale;
	}

	/**
	 * @param priorityScale the priorityScale to set
	 */
	public void setPriorityScale(Scale priorityScale) {
		this.priorityScale = priorityScale;
	}
	
}
