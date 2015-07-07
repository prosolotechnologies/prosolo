package org.prosolo.reminders;

import org.prosolo.common.domainmodel.user.reminders.Reminder;
import org.prosolo.services.event.Event;

public interface UpdatePersonalCalendarReminder {
	
	void updateReminder(Event event);
	
	Reminder getDeadlineReminderForResource(Event event);

//	void processResourceCompletedEvent(Event event);
	
}
