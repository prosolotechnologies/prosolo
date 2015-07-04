package org.prosolo.domainmodel.user.reminders;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.user.reminders.Reminder;

@Entity
public class EventReminder extends Reminder {

	private static final long serialVersionUID = 4632290612930345168L;

	private Date startDate;
	private Date endDate;
	private User author;
	private Set<User> guests;
	private boolean editable;

	public EventReminder() {
		setGuests(new HashSet<User>());
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "startdate", length = 19)
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "enddate", length = 19)
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "event_reminder_guests")
	public Set<User> getGuests() {
		return guests;
	}

	public void addGuest(User guest) {
		if (!guests.contains(guest)) {
			guests.add(guest);
		}
	}

	public void setGuests(Set<User> guests) {
		this.guests = guests;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	@Type(type = "true_false")
	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
}
