package org.prosolo.services.notifications.eventprocessing.deprecated;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.evaluation.EvaluationSubmission;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;

public class EvaluationGivenProcessor extends NotificationEventProcessor {
	
	public EvaluationGivenProcessor(Event event, Session session, 
					NotificationManager notificationManager, 
					NotificationsSettingsManager notificationsSettingsManager) {
		super(event, session, notificationManager, notificationsSettingsManager);
	}

	@Override
	List<User> getReceivers() {
		List<User> users = new ArrayList<>();

		EvaluationSubmission evSubmission = (EvaluationSubmission) resource;	
		User receiver = evSubmission.getRequest().getMaker();
		users.add(receiver);
		
		return users;
	}

	@Override
	User getSender() {
		EvaluationSubmission evSubmission = (EvaluationSubmission) resource;	
		return evSubmission.getMaker();
	}

	@Override
	String getNotificationMessage() {
		EvaluationSubmission evSubmission = (EvaluationSubmission) resource;
		return evSubmission.getMessage();
	}

	@Override
	boolean isConditionMet(User sender, User receiver) {
		return true;
	}
	
}
