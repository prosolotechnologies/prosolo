/**
 * 
 */
package org.prosolo.common.domainmodel.user.socialNetworks;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;

/**
 * @author "Nikola Milikic"
 *
 */
@Entity
public class SocialNetworkAccount implements Serializable {

	private static final long serialVersionUID = 2006199348802402979L;

	private long id;
	private SocialNetworkName socialNetwork;
	private String link = "";
	@Transient
	private String linkEdit = "";
	@Transient
	private String label;
	@Transient
	private boolean changed;

	public SocialNetworkAccount(SocialNetworkName socialNetworkName) {
		this.socialNetwork = socialNetworkName;
		String socialNetwork = socialNetworkName.toString();
		label = socialNetwork.substring(0, 1) + socialNetwork.substring(1).toLowerCase() + " username";
	}

	public SocialNetworkAccount() {
	}

	@Id
	@Column(name = "id", unique = true, nullable = false, insertable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Type(type = "long")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Enumerated(EnumType.STRING)
	public SocialNetworkName getSocialNetwork() {
		return socialNetwork;
	}

	public void setSocialNetwork(SocialNetworkName socialNetwork) {
		this.socialNetwork = socialNetwork;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLinkEdit() {
		return linkEdit;
	}

	public void setLinkEdit(String linkEdit) {
		this.linkEdit = linkEdit;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SocialNetworkAccount other = (SocialNetworkAccount) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
