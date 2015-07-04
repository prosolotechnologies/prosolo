package org.prosolo.common.domainmodel.featuredNews;

/**
 * @author Zoran Jerimport javax.persistence.Column;
 */
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.featuredNews.FeaturedNews;
import org.prosolo.common.domainmodel.featuredNews.FeaturedNewsType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class FeaturedNews extends BaseEntity implements Comparable<FeaturedNews> {
	
	private static final long serialVersionUID = 1550544746835295579L;
	
	private Date date;
	private FeaturedNewsType featuredNewsType;
	private User actor;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date", length = 19)
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	@Enumerated(EnumType.STRING)
	public FeaturedNewsType getFeaturedNewsType() {
		return featuredNewsType;
	}
	
	public void setFeaturedNewsType(FeaturedNewsType featuredNewsType) {
		this.featuredNewsType = featuredNewsType;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	public User getActor() {
		return actor;
	}
	
	public void setActor(User actor) {
		this.actor = actor;
	}
	
	@Override
	public int compareTo(FeaturedNews o) {
		if (this.getDate().before(o.getDate()))
			return -1;
		if (this.getDate().after(o.getDate()))
			return 1;
		return 0;
	}
	
}