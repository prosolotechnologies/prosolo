package org.prosolo.common.domainmodel.user.reminders;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.util.nodes.comparators.CreatedAscComparator;
import org.prosolo.common.domainmodel.user.reminders.Reminder;
import org.prosolo.common.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.common.domainmodel.user.reminders.ReminderType;
//import org.prosolo.util.nodes.comparators.CreatedAscComparator;

@Entity
//@Table(name="user_Reminder")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Reminder extends BaseEntity implements Comparable<Reminder> {

	private static final long serialVersionUID = -7142566118403866795L;
	
	private ReminderType reminderType;
	private Date deadline;
	private Node resource;
	private ReminderStatus reminderStatus;

	@Enumerated(EnumType.STRING)
	public ReminderType getReminderType() {
		return reminderType;
	}

	public void setReminderType(ReminderType reminderType) {
		this.reminderType = reminderType;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "deadline", length = 19)
	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	
	@OneToOne
	//(fetch = FetchType.LAZY)
	@Cascade({CascadeType.MERGE})
	public Node getResource() {
		return resource;
	}

	public void setResource(Node resource) {
		this.resource = resource;
	}

	@Override
	public int compareTo(Reminder o) {
		return new CreatedAscComparator().compare(this, o);
	}
	
	@Enumerated(EnumType.STRING)
	public ReminderStatus getReminderStatus() {
		return reminderStatus;
	}

	public void setReminderStatus(ReminderStatus reminderStatus) {
		this.reminderStatus = reminderStatus;
	}

}
