package org.prosolo.web.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.activities.requests.Request;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.reminders.Reminder;
import org.prosolo.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.domainmodel.user.reminders.RequestReminder;
import org.prosolo.reminders.dal.PersonalCalendarManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.util.date.DateUtil;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.home.util.ReminderConverter;
import org.prosolo.web.search.data.ReminderData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "remindersBean")
@Component("remindersBean")
@Scope("session")
public class RemindersBean  implements Serializable{

	private static final long serialVersionUID = -7028661095152394983L;
	
	private static Logger logger = Logger.getLogger(RemindersBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private PersonalCalendarManager personalCalendarQueries;
	@Autowired private DefaultManager defaultManager;
	 
	private List<ReminderData> reminders; 
	private final int elementsPerPage = 3;

	public void init() {
		if (reminders == null) {
			logger.debug("initializing");
			
			reminders = new ArrayList<ReminderData>();

			Collection<Reminder> personalReminders = personalCalendarQueries.readNotDismissedReminders(loggedUser.getUser());
			
			for (Reminder reminder : personalReminders) {
				Date deadline = reminder.getDeadline();
				long difference = DateUtil.daysBetween(new Date(), deadline);
				
				if (reminder.getReminderStatus().equals(ReminderStatus.ACTIVE) && difference < 5) {
					reminder.setReminderStatus(ReminderStatus.CRITICAL);
				} else if (reminder.getReminderStatus().equals(ReminderStatus.CRITICAL)	&& difference > 5) {
					reminder.setReminderStatus(ReminderStatus.ACTIVE);
				}
				reminders.add(ReminderConverter.convertReminderToReminderData(reminder));
			}
		}
	}
	
	public boolean hasMoreReminders(){
		if (reminders != null) {
			return reminders.size() > elementsPerPage;
		}
		return false;
	}

	public ReminderData addReminder(Reminder reminder) {
		if (reminders != null) {
			
			// check if reminder exists in the collection
			for (ReminderData r : reminders) {
				if (r.getReminder().equals(reminder)) {
					return null;
				}
			}
			ReminderData reminderData = ReminderConverter.convertReminderToReminderData(reminder);
			reminders.add(reminderData);
			return reminderData;
		}
		return null;
	}

	public void resourceCompleted(Node node) {
		resourceCompleted(node.getId());
	}
	
	public void resourceCompleted(long resourceId) {
		if (reminders != null) {
			for (ReminderData rData : reminders) {
				Reminder reminder = rData.getReminder();
				
				if (reminder.getResource() != null && 
						reminder.getResource().getId() == resourceId) {
					rData.setCompleted(true);
					rData.setStatus(ReminderStatus.COMPLETED);
					break;
				}
			}
		}
	}

	public boolean checkRemindersStatus(Reminder reminder) {
		return !reminder.getReminderStatus().equals(ReminderStatus.COMPLETED);
	}

	public int getRemindersSize() {
		if (reminders != null) {
			if (reminders.size() < elementsPerPage) {
				return reminders.size();
			} else
				return elementsPerPage;
		}
		return 0;
	}

	public void removeReminder(ReminderData reminder) {
		if (reminders != null && reminders.contains(reminder)) {
			Iterator<ReminderData> iterator = reminders.iterator();
    		
    		while (iterator.hasNext()) {
    			ReminderData r = (ReminderData) iterator.next();
    			
    			if (r.equals(reminder)) {
    				iterator.remove();
    				break;
    			}
    		}
		}
	}
	
	public void removeReminder(Node resource) {
		if (reminders != null) {
			ListIterator<ReminderData> iterator = this.reminders.listIterator();
			
			while (iterator.hasNext()) {
				ReminderData rData = iterator.next();
				
				if ((!(rData.getReminder() instanceof RequestReminder)) && 
						(rData.getReminder().getResource().equals(resource))) {
					iterator.remove();
				}
			}
		}
	}
	
	public void removeReminder(Request resource){
		if (reminders != null) {
			ListIterator<ReminderData> iterator = this.reminders.listIterator();
			
			while (iterator.hasNext()) {
				ReminderData rData = iterator.next();
				
				if (((rData.getReminder() instanceof RequestReminder)) && 
						(((RequestReminder) rData.getReminder()).getRequestResource().equals(resource))) {
					iterator.remove();
				}
			}
		}
	}
	
	public void updateReminder(Reminder reminder) {
		if (reminder instanceof RequestReminder) {
			removeReminder(((RequestReminder) reminder).getRequestResource());
		} else {
			removeReminder(reminder.getResource());
		}
		addReminder(reminder);
	}
	
	/*
	 * GETTER / SETTERS
	 */
	public Collection<ReminderData> getReminders() {
		return reminders;
	}

	public void setReminders(List<ReminderData> reminders) {
		this.reminders = reminders;
	}

}
