package org.prosolo.common.domainmodel.feeds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.user.TimeFrame;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Entity
@DiscriminatorColumn (length = 50)
public class FeedsDigest implements Serializable {
	
	private static final long serialVersionUID = -2522142169180752546L;
	
	private long id;
	private List<FeedEntry> entries;
	@Deprecated
	private TimeFrame timeFrame;
	private Date dateCreated;
	/*
	 * digest period lower bound
	 */
	private Date from;
	/*
	 * digest period upper bound
	 */
	private Date to;
	/*
	 * number of users that got digest via email
	 */
	private long numberOfUsersThatGotEmail;
	
	public FeedsDigest() {
	 	this.entries = new ArrayList<FeedEntry>();
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

 	@ManyToMany
 	public List<FeedEntry> getEntries() {
 		return entries;
 	}
 
 	public void setEntries(List<FeedEntry> entries) {
 		this.entries = entries;
 	}

	@Enumerated (EnumType.STRING)
	@Column(nullable = false)
	public TimeFrame getTimeFrame() {
		return timeFrame;
	}

	public void setTimeFrame(TimeFrame timeFrame) {
		this.timeFrame = timeFrame;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created", length = 19)
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Column(name="date_from")
	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	@Column(name="date_to")
	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public long getNumberOfUsersThatGotEmail() {
		return numberOfUsersThatGotEmail;
	}

	public void setNumberOfUsersThatGotEmail(long numberOfUsersThatGotEmail) {
		this.numberOfUsersThatGotEmail = numberOfUsersThatGotEmail;
	}
	
}
