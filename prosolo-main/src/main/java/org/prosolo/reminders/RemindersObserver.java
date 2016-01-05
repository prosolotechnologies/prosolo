package org.prosolo.reminders;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.reminders.EventReminder;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.reminders.RemindersObserver")
public class RemindersObserver extends EventObserver {
	
	@Autowired private UpdatePersonalCalendarReminder updatePersonalCalendarReminder;

	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[]
		{ 
			EventType.Create,
			EventType.Edit,
			EventType.Delete,
			EventType.Completion,
			EventType.EVALUATION_REQUEST,
			EventType.CREATE_PERSONAL_SCHEDULE,
			EventType.CREATE_PERSONAL_SCHEDULE_ACCEPTED
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] { 
			TargetLearningGoal.class, 
			LearningGoal.class, 
			Request.class, 
			EventReminder.class
		};
	}

	@Override
	public void handleEvent(Event event) {
			updatePersonalCalendarReminder.updateReminder(event);
	}

}
