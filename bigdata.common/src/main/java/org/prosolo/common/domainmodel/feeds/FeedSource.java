package org.prosolo.common.domainmodel.feeds;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.feeds.FeedSource;

/**
 * @author Zoran Jeremic 2013-09-23
 *
 */
@Entity
public class FeedSource extends BaseEntity {
	
	private static final long serialVersionUID = 2772153689882763553L;
	
	private String link;
	private Date lastCheck;
	
	public FeedSource() {}
	
	public FeedSource (String title, String link) {
		setTitle(title);
		this.link = link;
	}
	
	@Column(name = "link", nullable = true, length = 500)
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastCheck() {
		return lastCheck;
	}

	public void setLastCheck(Date lastCheck) {
		this.lastCheck = lastCheck;
	}
	
	@Override
	public boolean equals(Object obj) {
		return link.equals(((FeedSource) obj).getLink());
	}

	@Override
	public String toString() {
		return "FeedSource [link=" + link + ", lastCheck=" + lastCheck + "]";
	}
	
}
