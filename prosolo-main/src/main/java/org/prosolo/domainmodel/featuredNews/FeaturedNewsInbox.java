/**
 * 
 */
package org.prosolo.domainmodel.featuredNews;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.prosolo.domainmodel.featuredNews.UserFeaturedNews;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.User;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
public class FeaturedNewsInbox extends BaseEntity {

	private static final long serialVersionUID = 2859761801813219588L;

	private User user;
	private Set<UserFeaturedNews> featuredNews;

	@OneToOne
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@OneToMany
	@JoinTable(name = "FeaturedNewsInbox_UserFeaturedNews")
	public Set<UserFeaturedNews> getFeaturedNews() {
		return featuredNews;
	}

	public void setFeaturedNews(Set<UserFeaturedNews> featuredNews) {
		this.featuredNews = featuredNews;
	}

}
