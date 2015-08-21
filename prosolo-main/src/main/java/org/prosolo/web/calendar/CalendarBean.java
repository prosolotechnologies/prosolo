package org.prosolo.web.calendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.reminders.EventReminder;
import org.prosolo.common.domainmodel.user.reminders.Reminder;
import org.prosolo.common.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.common.domainmodel.user.reminders.ReminderType;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.reminders.dal.PersonalCalendarManager;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.dialogs.LearningGoalDialogBean;
import org.prosolo.web.home.RemindersBean;
import org.prosolo.web.search.data.ReminderData;
import org.prosolo.web.util.ResourceDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="calendar")
@Component("calendar")
@Scope("view")
public class CalendarBean implements Serializable {
	
	private static final long serialVersionUID = -5212660766004640671L;
	private static Logger logger = Logger.getLogger(CalendarBean.class);
	
	@Autowired private RemindersBean remindersBean;
	@Autowired private LearningGoalDialogBean learningGoalDialogBean; 
	@Autowired private TextSearch textSearch;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private PersonalCalendarManager personalCalendarManager;
	@Autowired private EventFactory eventFactory;
	
	private ScheduleModel eventModel;
	private ProsoloDefaultScheduleEvent event;
	
	private String keyword;
	private List<UserData> userSearchResults;
	
	@PostConstruct
	public void init() {
		logger.debug("initializing");
		eventModel = new DefaultScheduleModel();
		userSearchResults = new ArrayList<UserData>();  
		initializeCalendarEvents();
	}

	public void initializeCalendarEvents() {
		Collection<ReminderData> remindersData = remindersBean.getReminders();
		
		for (ReminderData reminderData : remindersData) {
			addReminderDataToEventModel(reminderData);
		}
	}
	
	public void addReminderDataToEventModel(ReminderData reminderData){
		String title = reminderData.getTitle();
		Date deadline = reminderData.getDeadline();
		String styleClass = "default-event";

		if (reminderData.getStatus().equals(ReminderStatus.CRITICAL)) {
			styleClass = "critical-event";
		} else if (reminderData.getStatus().equals(ReminderStatus.COMPLETED)) {
			styleClass = "completed-event";
		}
		
		DefaultScheduleEvent scheduledEvent = null;
		
		if (reminderData.getReminderType().equals(ReminderType.PERSONALEVENT)) {
			scheduledEvent = new ProsoloPersonalScheduleEvent(title, deadline, deadline, styleClass);
		} else {
			scheduledEvent = new ProsoloLearningEvent(title, deadline, deadline, styleClass);
			Reminder reminder = reminderData.getReminder();
			
			if (reminder.getResource() != null) {
				((ProsoloLearningEvent) scheduledEvent).setResource(reminder.getResource());
			}
		}
	
		eventModel.addEvent(scheduledEvent);
	}
    
	public void addEvent() {
		if (event.getId() == null) {
			eventModel.addEvent(event);
		} else {
			eventModel.updateEvent(event);
		}
		
		EventReminder eventReminder = personalCalendarManager.createScheduledEventReminder((ProsoloPersonalScheduleEvent) event, loggedUser.getUser());
		remindersBean.addReminder(eventReminder);
		
		try {
			eventFactory.generateEvent(EventType.CREATE_PERSONAL_SCHEDULE, loggedUser.getUser(),eventReminder);
		} catch (EventException e) {
			logger.error(e);
		}
		event = new ProsoloPersonalScheduleEvent();
	}

	public void onEventSelect(SelectEvent selectEvent) {
		event = (ProsoloDefaultScheduleEvent) selectEvent.getObject();
	}
	
	public void onProsoloEventSelect(SelectEvent selectEvent) {
		if (selectEvent.getObject() instanceof ProsoloLearningEvent){
			event = (ProsoloLearningEvent) selectEvent.getObject();
			Node resource = ((ProsoloLearningEvent) event).getResource();

			if (resource != null && resource instanceof LearningGoal) {
				learningGoalDialogBean.initializeOtherGoalDialogById(resource.getId());
				//goal = (LearningGoal) resource;
			}
			//Ajax.update(":goalDetailsDialogForm");
			//Ajax.oncomplete("$('#goalDetailsModalDialog').dialog('open');");
		} else {
			event = (ProsoloPersonalScheduleEvent) selectEvent.getObject();
		}
	}

	public void onDateSelect(SelectEvent selectEvent) {
		Date now = new Date();
		long difference = DateUtil.daysBetween(now,	(Date) selectEvent.getObject());
		String styleClass = "default-event";
		
		if (difference < 5) {
			styleClass = "critical-event";
		}
		event = new ProsoloPersonalScheduleEvent(null, (Date) selectEvent.getObject(),(Date) selectEvent.getObject(),styleClass);
		//((DefaultScheduleEvent) event).setStartDate((Date) selectEvent.getObject());
		//((DefaultScheduleEvent) event).setEndDate((Date) selectEvent.getObject());
		((DefaultScheduleEvent) event).setAllDay(true);

		//event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());
	}
	
	public void executeTextSearch(String toExcludeString) {
		long[] moreToExclude = StringUtil.fromStringToLong(toExcludeString);
		
		List<Long> totalListToExclude = new ArrayList<Long>();
		
		if (moreToExclude != null) {
			for (long l : moreToExclude) {
				totalListToExclude.add(l);
			}
		}
		userSearchResults.clear();
		
		TextSearchResponse usersResponse = textSearch.searchUsers(keyword, 0, 4, false, totalListToExclude);
		
		@SuppressWarnings("unchecked")
		List<User> result = (List<User>) usersResponse.getFoundNodes();
		
		for (User user : result) {
			UserData userData = new UserData(user);
			userSearchResults.add(userData);
		}
	}
	
	public void onEventMove(ScheduleEntryMoveEvent event) {
		
	}
	
	public void onEventResize(ScheduleEntryResizeEvent event) {
		
	}
	
	/* GUESTS */
	
	public void removeUser(UserData userData) {
		((ProsoloPersonalScheduleEvent) this.event).removeGuest(userData);
		userSearchResults.clear();
	}
	
	public void addUser(UserData userData){
		userSearchResults.clear();
		keyword = "";
		
		if (userData != null) {
			((ProsoloPersonalScheduleEvent) this.event).addGuest(userData);
		}
	}
	
	public String getToExcludeIds() {
		if (event instanceof ProsoloPersonalScheduleEvent) {
			return ResourceDataUtil.getUserIds(((ProsoloPersonalScheduleEvent) this.event).getGuestsList()).toString();
		}
		return "";
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public ScheduleModel getEventModel() {
		return eventModel;
	}
	
	public ProsoloDefaultScheduleEvent getEvent() {
		return event;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	public List<UserData> getUserSearchResults() {
		return userSearchResults;
	}

	public void setUserSearchResults(List<UserData> userSearchResults) {
		this.userSearchResults = userSearchResults;
	}

}
