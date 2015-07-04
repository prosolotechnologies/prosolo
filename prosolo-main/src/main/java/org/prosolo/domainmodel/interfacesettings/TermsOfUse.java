/**
 * 
 */
package org.prosolo.domainmodel.interfacesettings;

import java.util.Date;

import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.general.BaseEntity;

/**
 * @author "Nikola Milikic"
 *
 */
@Entity
public class TermsOfUse extends BaseEntity {

	private static final long serialVersionUID = -2489772455536146183L;
	
	private boolean accepted;
	private Date date;

	@Type(type = "true_false")
	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
