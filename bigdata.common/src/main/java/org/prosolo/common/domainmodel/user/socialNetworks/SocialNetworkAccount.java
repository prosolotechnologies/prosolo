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

	public SocialNetworkAccount(SocialNetworkName socialNetworkName) {
		this.socialNetwork = socialNetworkName;
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

}
