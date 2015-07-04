package org.prosolo.reminders.dal;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.user.reminders.EventReminder;
import org.prosolo.domainmodel.user.reminders.PersonalCalendar;
import org.prosolo.domainmodel.user.reminders.Reminder;
import org.prosolo.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.domainmodel.user.reminders.ReminderType;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.calendar.ProsoloPersonalScheduleEvent;

public interface PersonalCalendarManager extends AbstractManager {

	Reminder loadExistingReminder(BaseEntity resource, User performedBy, ReminderType reminderType);

	List<Reminder> readNotDismissedReminders(User user);

	List<Reminder> readReminders(User user, ReminderStatus status);

	PersonalCalendar getOrCreateCalendar(User user);

	PersonalCalendar getOrCreateCalendar(User user, Session session);

	EventReminder createScheduledEventReminder(ProsoloPersonalScheduleEvent event, User author);

}