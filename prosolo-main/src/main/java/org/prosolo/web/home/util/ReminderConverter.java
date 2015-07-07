package org.prosolo.web.home.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.prosolo.common.domainmodel.user.reminders.Reminder;
import org.prosolo.web.search.data.ReminderData;

/*
 * @author Zoran Jeremic 2013-05-29
 */
public class ReminderConverter {
	
	public static ReminderData convertReminderToReminderData(Reminder reminder) {
		ReminderData reminderData = null;
		if (reminder != null) {
			reminderData = new ReminderData(reminder);
		}
		return reminderData;
	}
	
	public static Collection<ReminderData> convertToReminderData(List<Reminder> reminders, boolean viewed) {
		List<ReminderData> remindersData = new ArrayList<ReminderData>();
		
		if (!reminders.isEmpty()) {
			for (Reminder r : reminders) {
				ReminderData rData = convertReminderToReminderData(r);
				if (rData != null) {
					remindersData.add(rData);
				}
			}
		}
		return remindersData;
	}
}

