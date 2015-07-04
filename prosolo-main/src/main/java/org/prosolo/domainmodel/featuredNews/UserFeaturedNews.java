/**
 * 
 */
package org.prosolo.domainmodel.featuredNews;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.featuredNews.FeaturedNews;
import org.prosolo.domainmodel.general.BaseEntity;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
public class UserFeaturedNews extends BaseEntity {
	
	private static final long serialVersionUID = 2584910439341201740L;
	
	private FeaturedNews news;
	private boolean dismissed;
	
	@OneToOne
	public FeaturedNews getNews() {
		return news;
	}
	
	public void setNews(FeaturedNews news) {
		this.news = news;
	}
	
	@Type(type = "true_false")
	public boolean isDismissed() {
		return dismissed;
	}
	
	public void setDismissed(boolean dismissed) {
		this.dismissed = dismissed;
	}
	
}
