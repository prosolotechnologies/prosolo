package org.prosolo.web.home;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.reminders.Reminder;
import org.prosolo.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.search.SearchRemindersBean;
import org.prosolo.web.search.data.ReminderData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "reminderBeanDialog")
@Component("reminderBeanDialog")
@Scope("view")
public class ReminderBeanDialog implements Serializable{

	private static final long serialVersionUID = 1326919575807029452L;
	
	private static Logger logger = Logger.getLogger(ReminderBeanDialog.class);
	
	private @Autowired RemindersBean remindersBean;
	private @Autowired DefaultManager defaultManager;
	private @Autowired SearchRemindersBean searchRemindersBean;
	private @Autowired LoggingNavigationBean loggingNavigationBean;
	private @Autowired LoggedUserBean loggedUser;
	
	private ReminderData reminder;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	public Node getResource() {
		if (reminder != null) {
			return reminder.getReminder().getResource();
		} else
			return null;
	}
	
	public boolean isHaveReminder(){
		return reminder != null;
	}
	
	public void dismissReminder(ReminderData reminderToDismiss){
		this.reminder = reminderToDismiss;
		dismissReminder();
		searchRemindersBean.removeReminder(reminder);
		remindersBean.removeReminder(reminder);
		
		PageUtil.fireSuccessfulInfoMessage("Reminder dismissed.");
	}
	
	public void dismissReminder() {
		if (reminder != null) {
			Reminder reminderResource = reminder.getReminder();
			reminderResource.setReminderStatus(ReminderStatus.DISMISSED);
			defaultManager.saveEntity(reminderResource);
			reminder.setStatus(ReminderStatus.DISMISSED);
			searchRemindersBean.removeReminder(reminder);
			remindersBean.removeReminder(reminder);
			
			PageUtil.fireSuccessfulInfoMessage("Reminder dismissed.");
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public ReminderData getReminder() {
		return reminder;
	}

	public void setReminder(ReminderData reminder, String context) {
		if (reminder != null) {
			loggingNavigationBean.logServiceUse(
					ComponentName.REMINDER_DIALOG,
					"reminderId", String.valueOf(reminder.getReminder().getId()),
					"reminderTitle", reminder.getTitle(),
					"reminderDeadline", String.valueOf(reminder.getDeadline()),
					"context", context);
		}
		this.reminder = reminder;
	}

}
