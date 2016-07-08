package org.prosolo.reminders.dal;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.reminders.EventReminder;
import org.prosolo.common.domainmodel.user.reminders.PersonalCalendar;
import org.prosolo.common.domainmodel.user.reminders.Reminder;
import org.prosolo.common.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.common.domainmodel.user.reminders.ReminderType;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.calendar.ProsoloPersonalScheduleEvent;

public interface PersonalCalendarManager extends AbstractManager {

	Reminder loadExistingReminder(BaseEntity resource, long userId, ReminderType reminderType);

	List<Reminder> readNotDismissedReminders(long userId);

	List<Reminder> readReminders(long userId, ReminderStatus status);

	PersonalCalendar getOrCreateCalendar(User user) throws ResourceCouldNotBeLoadedException;

	PersonalCalendar getOrCreateCalendar(long userId, Session session) throws ResourceCouldNotBeLoadedException;

	EventReminder createScheduledEventReminder(ProsoloPersonalScheduleEvent event, long authorId) throws ResourceCouldNotBeLoadedException;

}