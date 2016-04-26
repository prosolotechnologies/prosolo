/**
 * 
 */
package org.prosolo.common.domainmodel.evaluation;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.prosolo.common.domainmodel.evaluation.BadgeType;
import org.prosolo.common.domainmodel.general.BaseEntity;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
//@Table(name = "wf_Badge")
public class Badge extends BaseEntity {

	private static final long serialVersionUID = 9163401305900744914L;

	private BadgeType type;

	/**
	 * @return the type
	 */
	@Enumerated(EnumType.STRING)
	public BadgeType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(BadgeType type) {
		this.type = type;
	}

}