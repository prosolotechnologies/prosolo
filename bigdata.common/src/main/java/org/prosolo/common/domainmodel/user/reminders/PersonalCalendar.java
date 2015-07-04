package org.prosolo.common.domainmodel.user.reminders;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.common.domainmodel.user.reminders.PersonalCalendar;
import org.prosolo.common.domainmodel.user.reminders.Reminder;

@Entity
public class PersonalCalendar implements Serializable  {

	private static final long serialVersionUID = -2797595531230059732L;

	private User user;
	private Set<Reminder> reminders;
	private List<Notification> notifications;

	public PersonalCalendar() {
		reminders = new  HashSet<Reminder>();
		notifications = new LinkedList<Notification>();
	}
	
	@Id @OneToOne 
	@JoinColumn (name="id") 
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.ALL})
	public Set<Reminder> getReminders() {
		return reminders;
	}

	public void setReminders(Set<Reminder> reminders) {
		this.reminders = reminders;
	}

	public void addReminder(Reminder reminder) {
		if (reminder != null && !reminders.contains(reminder)) {
			reminders.add(reminder);
		}
	}

	public boolean removeReminder(Reminder reminder) {
		if (reminder != null) {
			return getReminders().remove(reminder);
		}
		return false;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@Cascade({org.hibernate.annotations.CascadeType.ALL})
	public List<Notification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	}

	public void addNotification(Notification notification) {
		if (notification != null) {
			getNotifications().add(notification);
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getUser().hashCode();
		result = (int) (prime * result+getUser().getId());
		return result;
	}
 
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		
		PersonalCalendar other = (PersonalCalendar) obj;
		if (this.getUser().getId() > 0 && other.getUser().getId() > 0) {
			return this.getUser().getId() == other.getUser().getId();
		}  
		return true;
	}

}
