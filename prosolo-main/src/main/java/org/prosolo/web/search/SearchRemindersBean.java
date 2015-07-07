package org.prosolo.web.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.reminders.Reminder;
import org.prosolo.common.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.reminders.dal.PersonalCalendarManager;
import org.prosolo.search.TextSearch;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.util.date.DateUtil;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.home.RemindersBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.search.data.ReminderData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Zoran Jeremic"
 * @date 2013-04-12
 */
@ManagedBean(name = "searchRemindersBean")
@Component("searchRemindersBean")
@Scope("view")
public class SearchRemindersBean implements Serializable {

	private static final long serialVersionUID = 5529246826210174621L;

	private static Logger logger = Logger.getLogger(SearchRemindersBean.class);

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private RemindersBean remindersBean;
	@Autowired private PersonalCalendarManager personalCalendarQueries;
	@Autowired private TextSearch textSearch;
	@Autowired private LoggingNavigationBean loggingNavigationBean;

	private List<ReminderData> reminders = new ArrayList<ReminderData>();
	private ReminderStatus chosenFilter = null;

	@SuppressWarnings("unused")
	private int page = 0;
	private int limit = 7;
	private boolean moreToLoad;
	private int reminderSize;

	private String query;

	public void searchAllReminders() {
		searchReminders(query, true);
	}
	
	public void searchReminders(String searchQuery, boolean loadOneMore) {
		this.reminders.clear();
		this.reminderSize = 0;
		
		fetchReminders(searchQuery, loadOneMore);
		
		if (searchQuery != null && searchQuery.length() > 0) {
			loggingNavigationBean.logServiceUse(
					ComponentName.SEARCH_REMINDERS, 
					"query", searchQuery,
					"context", "search.reminders");
		}
	}
	
	public void fetchReminders(String searchQuery, boolean loadOneMore) {
		List<Reminder> foundReminders = personalCalendarQueries.readReminders(loggedUser.getUser(), chosenFilter);
		
		// if there is more than limit, set moreToLoad to true
		if (foundReminders.size() == limit + 1) {
			foundReminders = foundReminders.subList(0, foundReminders.size() - 1);
			moreToLoad = true;
		} else {
			moreToLoad = false;
		}
		
		reminders.addAll(convertToReminderData(foundReminders, false));
	}
	
	public void loadMoreReminders() {
		page++;
		fetchReminders(query, true);
	}
	

	public List<ReminderData> convertReminderData(List<Reminder> personalReminders, boolean viewed) {
		List<ReminderData> remindersData = new ArrayList<ReminderData>();
		Date now = new Date();
		
		for (Reminder reminder : personalReminders) {
			long difference = DateUtil.daysBetween(now, reminder.getDeadline());
			
			if (reminder.getReminderStatus().equals(ReminderStatus.ACTIVE) && difference < 5) {
				reminder.setReminderStatus(ReminderStatus.CRITICAL);
			} else if (reminder.getReminderStatus().equals(ReminderStatus.CRITICAL)	&& difference > 5) {
				reminder.setReminderStatus(ReminderStatus.ACTIVE);
			}
			ReminderData rData = new ReminderData(reminder);
			rData.setViewed(viewed);
			remindersData.add(rData);
		}
		return remindersData;
	}

	private Collection<ReminderData> convertToReminderData(List<Reminder> reminders, boolean viewed) {
		List<ReminderData> remindersData = new ArrayList<ReminderData>();
		
		if (reminders != null && !reminders.isEmpty()) {
			for (Reminder r : reminders) {
				ReminderData reminderData = new ReminderData(r);
				reminderData.setViewed(viewed);
				remindersData.add(reminderData);
			}
		}
		return remindersData;
	}

	public void removeReminder(ReminderData reminder) {
		if (reminders.contains(reminder)) {
			reminders.remove(reminder);
		}
	}

	public String getChosenFilterString() {
		if (chosenFilter != null) {
			return getFilterName(chosenFilter.toString().toLowerCase());
		} else {
			return getFilterName("all");
		}
	}

	public String getFilterName(String filter) {
		Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		String filterString = "";
		try {
			filterString = ResourceBundleUtil.getMessage(
					"search.reminders.status." + filter.toLowerCase(), 
					locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
		return filterString;
	}

	public void setChosenFilterString(String filterString) {
		// reseting page
		this.page = 0;
		
		if (filterString != null && !filterString.equals("")) {
			try {
				chosenFilter = ReminderStatus.valueOf(filterString
						.toUpperCase());
				return;
			} catch (IllegalArgumentException e) {
			}
		}
		chosenFilter = null;
	}

	/*
	 * GETTERS / SETTERS
	 */
	public ReminderStatus getChosenFilter() {
		return chosenFilter;
	}

	public List<ReminderData> getReminders() {
		return reminders;
	}

	public void setReminders(List<ReminderData> reminders) {
		this.reminders = reminders;
	}

	public void setChosenFilter(ReminderStatus chosenFilter) {
		this.chosenFilter = chosenFilter;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isMoreToLoad() {
		return moreToLoad;
	}

	public int getReminderSize() {
		return reminderSize;
	}
	
}