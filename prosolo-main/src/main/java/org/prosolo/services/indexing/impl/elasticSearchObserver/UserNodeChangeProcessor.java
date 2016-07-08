package org.prosolo.services.indexing.impl.elasticSearchObserver;

import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.ChangeProgressEvent;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.UserEntityESService;

public class UserNodeChangeProcessor implements NodeChangeProcessor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(UserNodeChangeProcessor.class);
	
	private Event event;
	private Session session;
	private UserEntityESService userEntityESService;
	private EventUserRole userRole;
	
	public UserNodeChangeProcessor(Event event, Session session, UserEntityESService userEntityESService,
			EventUserRole userRole) {
		this.event = event;
		this.session = session;
		this.userEntityESService = userEntityESService;
		this.userRole = userRole;
	}
	
	@Override
	public void process() {
		User user = null;
		EventType eventType = event.getAction();
		Map<String, String> params = event.getParameters();
		if(eventType == EventType.ENROLL_COURSE) {
			Credential1 cred = (Credential1) event.getObject();
			long instructorId = Long.parseLong(params.get("instructorId"));
			String dateEnrolledString = params.get("dateEnrolled");
			userEntityESService.addCredentialToUserIndex(cred.getId(), event.getActorId(), 
					instructorId, dateEnrolledString);
		} else if(eventType == EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR
				|| eventType == EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR
				|| eventType == EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR) {
			long credId = Long.parseLong(params.get("credId"));
			/*
			 * if unassigned, we should set instructorId to 0 in user index for this credential,
			 * if assigned, we should set id of instructor that is assigned
			 */
			long instructorId = 0;
			if(eventType == EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR 
					|| eventType == EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR) {
				instructorId = event.getTarget().getId();
			}
			userEntityESService.assignInstructorToUserInCredential(event.getObject().getId(), 
					credId, instructorId);
		} else if(eventType == EventType.INSTRUCTOR_ASSIGNED_TO_COURSE) {
			String dateAssigned = params.get("dateAssigned");
			userEntityESService.addInstructorToCredential(event.getTarget().getId(), 
					event.getObject().getId(), dateAssigned);
	    } else if(eventType == EventType.INSTRUCTOR_REMOVED_FROM_COURSE) {
			userEntityESService.removeInstructorFromCredential(event.getTarget().getId(), 
					event.getObject().getId());
	    } else if(eventType == EventType.ChangeProgress) {
	    	ChangeProgressEvent cpe = (ChangeProgressEvent) event;
	    	userEntityESService.changeCredentialProgress(cpe.getActorId(), cpe.getObject().getId(), 
	    			cpe.getNewProgressValue());
	    } else {
			if(userRole == EventUserRole.Subject) {
				long userId = event.getActorId();
				user = (User) session.load(User.class, userId);
			} else if(userRole == EventUserRole.Object) {
				BaseEntity node = event.getObject();
				user = (User) session.load(User.class, node.getId());
			}
	
			userEntityESService.saveUserNode(user, session);
		}
	}

}
