package org.prosolo.reminders.dal.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.reminders.EventReminder;
import org.prosolo.common.domainmodel.user.reminders.PersonalCalendar;
import org.prosolo.common.domainmodel.user.reminders.Reminder;
import org.prosolo.common.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.common.domainmodel.user.reminders.ReminderType;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.reminders.dal.PersonalCalendarManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.web.calendar.ProsoloPersonalScheduleEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service("org.prosolo.reminders.dal.PersonalCalendarManager")
public class PersonalCalendarManagerImpl extends AbstractManagerImpl implements PersonalCalendarManager {
	
	private static final long serialVersionUID = -8008275715915848264L;
	
	private static Logger logger = Logger.getLogger(PersonalCalendarManager.class);
	
	@Override
	public Reminder loadExistingReminder(BaseEntity resource, long userId, ReminderType reminderType){
		String query =
			"SELECT DISTINCT reminder " +
			"FROM PersonalCalendar calendar " +
			"LEFT JOIN calendar.user user " +
			"LEFT JOIN calendar.reminders reminder "+
			"LEFT JOIN reminder.resource resource "+
			"WHERE user.id = :userId " +
				"AND resource = :resource";
		
		return (Reminder) persistence.currentManager().createQuery(query)
			.setLong("userId", userId)
			.setEntity("resource", resource)
			.uniqueResult();
	}
	
	@Override
	public List<Reminder> readNotDismissedReminders(long userId){
		String query =
			"SELECT DISTINCT reminder " +
			"FROM PersonalCalendar calendar " +
			"LEFT JOIN calendar.user user " +
			"LEFT JOIN calendar.reminders reminder " +
			//"LEFT JOIN fetch reminder.resource " +
			"WHERE user.id = :userId " +
				"AND reminder.reminderStatus != :statusDismissed " +
			"ORDER BY reminder.deadline ASC";
		
		@SuppressWarnings("unchecked")
		List<Reminder> result = persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				//.setString("type", ReminderType.DEADLINE.toString())
				.setString("statusDismissed", ReminderStatus.DISMISSED.toString())
				.list();
	
		if (result != null && !result.isEmpty()) {
			return result;
		} else {
			return new ArrayList<Reminder>();
		}
	}
	
	@Override
	public List<Reminder> readReminders(long userId, ReminderStatus status){
		String query =
			"SELECT DISTINCT reminder " +
			"FROM PersonalCalendar calendar " +
			"LEFT JOIN calendar.user user " +
			"LEFT JOIN calendar.reminders reminder "+
			"WHERE user.id = :userId " ;
		
		if (status != null) {
			query += "AND reminder.reminderStatus = :status ";
		} else {
			query += "AND reminder.reminderStatus != :status ";
		}
		query += "ORDER BY reminder.deadline DESC";
		
		Query q = persistence.currentManager().createQuery(query)
				.setLong("userId", userId);
		
		if (status != null) {
			q.setString("status", status.toString());
		} else {
			q.setString("status", ReminderStatus.DISMISSED.toString());
		}
		
		@SuppressWarnings("unchecked")
		List<Reminder> result = q.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return new ArrayList<Reminder>();
	}
	
	@Override
	@Transactional(readOnly = false)
	public PersonalCalendar getOrCreateCalendar(User user) {
		try {
			return getOrCreateCalendar(user.getId(), persistence.currentManager());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		return null;
	}
	
	@Override
	@Transactional(readOnly = false)
	public PersonalCalendar getOrCreateCalendar(long userId, Session session) throws ResourceCouldNotBeLoadedException {
		String query = 
			"SELECT DISTINCT calendar " +
			"FROM PersonalCalendar calendar " +
			"WHERE calendar.user.id = :userId";
		
		PersonalCalendar calendar = (PersonalCalendar) session.createQuery(query).
				setLong("userId", userId).
				uniqueResult();
		
		if (calendar == null) {
			calendar = new PersonalCalendar();
			calendar.setUser(loadResource(User.class, userId));
			session.save(calendar);
		}
		return calendar;
	}
	
	@Override
	@Transactional(readOnly=false)
	public EventReminder createScheduledEventReminder(ProsoloPersonalScheduleEvent event, long userId) throws ResourceCouldNotBeLoadedException{
		User author = loadResource(User.class, userId);

		EventReminder eventReminder = new EventReminder();
		eventReminder.setTitle(event.getTitle());
		eventReminder.setDescription(event.getDescription());
		eventReminder.setAuthor(author);
		eventReminder.setReminderType(ReminderType.PERSONALEVENT);
		eventReminder.setReminderStatus(ReminderStatus.ACTIVE);
		eventReminder.setStartDate(event.getStartDate());
		eventReminder.setDeadline(event.getStartDate());
		eventReminder.setEndDate(event.getEndDate());
		eventReminder.setEditable(event.isGuestsCanModify());
		
		List<UserData> guestsList = ((ProsoloPersonalScheduleEvent) event).getGuestsList();
		
		if (guestsList != null && !guestsList.isEmpty()) {
			for (UserData userData : guestsList) {
				long guestId = userData.getId();
				
				try {
					User guest = this.loadResource(User.class, guestId);
					eventReminder.addGuest(guest);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		}
		
		eventReminder = saveEntity(eventReminder);
		
		PersonalCalendar persCalendar = getOrCreateCalendar(author);
		persCalendar.addReminder(eventReminder);
		saveEntity(persCalendar);
		
		return eventReminder;
	}
	
}
