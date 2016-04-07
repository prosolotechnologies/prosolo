/**
 * 
 */
package org.prosolo.common.domainmodel.user.socialNetworks;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;

/**
 * @author "Nikola Milikic"
 *
 */
@Entity
public class UserSocialNetworks implements Serializable {

	private static final long serialVersionUID = 7268042893027258178L;

	private long id;
	private User user;
	private Set<SocialNetworkAccount> socialNetworkAccounts;
	
	public UserSocialNetworks() {
		socialNetworkAccounts = new HashSet<SocialNetworkAccount>();
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

	@OneToOne
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@OneToMany
	public Set<SocialNetworkAccount> getSocialNetworkAccounts() {
		return socialNetworkAccounts;
	}

	public void setSocialNetworkAccounts(Set<SocialNetworkAccount> socialNetworkAccounts) {
		this.socialNetworkAccounts = socialNetworkAccounts;
	}
	
	public void addSocialNetworkAccount(SocialNetworkAccount socialNetworkAccount) {
		if (socialNetworkAccount != null) {
			socialNetworkAccounts.add(socialNetworkAccount);
		}
	}
	
	public SocialNetworkAccount getAccount(SocialNetworkName name) {
		for (SocialNetworkAccount account : socialNetworkAccounts) {
			if (account.getSocialNetwork().equals(name)) {
					return account;
			}
		}
		return null;
	}
}
