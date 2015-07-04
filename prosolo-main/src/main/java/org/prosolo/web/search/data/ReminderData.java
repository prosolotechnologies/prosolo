/**
 * 
 */
package org.prosolo.web.search.data;

import java.io.Serializable;
import java.util.Date;

import org.prosolo.domainmodel.activities.requests.Request;
import org.prosolo.domainmodel.user.reminders.EventReminder;
import org.prosolo.domainmodel.user.reminders.Reminder;
import org.prosolo.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.domainmodel.user.reminders.ReminderType;
import org.prosolo.domainmodel.user.reminders.RequestReminder;
import org.prosolo.web.util.ResourceUtilBean;

/**
 * @author "Nikola Milikic"
 *
 */
public class ReminderData implements Serializable {

	private static final long serialVersionUID = 8611892356068741911L;
	
	private String title;
	private String description;
	private String resourceType;
	private Date deadline;
	private ReminderStatus status;
	private ReminderType reminderType;
	
	private boolean viewed;
	private boolean completedOrDismissed;
	
	private Reminder reminder;
	
	public ReminderData(Reminder reminder) {
		if (reminder instanceof RequestReminder) {
			Request request = ((RequestReminder) reminder).getRequestResource();
			if(request.getTitle()==null && request.getResource()!=null){
				this.title=request.getResource().getTitle();
			}else{
				this.title = request.getTitle();
			}
			
			setReminderType(ReminderType.REQUEST);
		} else if (reminder instanceof EventReminder) {
			this.title = reminder.getTitle();
			setReminderType(ReminderType.PERSONALEVENT);
		} else {
			this.title = reminder.getResource().getTitle();
			setReminderType(ReminderType.DEADLINE);
		}
		
		this.resourceType = ResourceUtilBean.findReminderResourceType(reminder);
		this.deadline = reminder.getDeadline();
		this.status = reminder.getReminderStatus();
		this.reminder = reminder;
		
		if (reminder instanceof RequestReminder) {
			this.description = ((RequestReminder) reminder).getRequestResource().getDescription();
		} else if (reminder instanceof EventReminder) {
			this.description = reminder.getDescription();
		} else {
			this.description = reminder.getResource().getDescription();
		}
		
		if (reminder.getReminderStatus().equals(ReminderStatus.COMPLETED) || 
				(reminder.getReminderStatus().equals(ReminderStatus.DISMISSED))){
			this.completedOrDismissed=true;
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public ReminderStatus getStatus() {
		return status;
	}

	public void setStatus(ReminderStatus status) {
		this.status = status;
	}

	public boolean isViewed() {
		return viewed;
	}

	public void setViewed(boolean viewed) {
		this.viewed = viewed;
	}

	public boolean isCompleted() {
		return completedOrDismissed;
	}

	public void setCompleted(boolean completed) {
		this.completedOrDismissed = completed;
	}

	public Reminder getReminder() {
		return reminder;
	}

	public void setReminder(Reminder reminder) {
		this.reminder = reminder;
	}

	public ReminderType getReminderType() {
		return reminderType;
	}

	public void setReminderType(ReminderType reminderType) {
		this.reminderType = reminderType;
	}
	
}
