package org.prosolo.services.indexing.impl.elasticSearchObserver;

import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.ChangeProgressEvent;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.indexing.UserEntityESService;

public class UserNodeChangeProcessor implements NodeChangeProcessor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(UserNodeChangeProcessor.class);
	
	private Event event;
	private Session session;
	private UserEntityESService userEntityESService;
	private CredentialESService credESService;
	private EventUserRole userRole;
	
	public UserNodeChangeProcessor(Event event, Session session, UserEntityESService userEntityESService,
			CredentialESService credESService, EventUserRole userRole) {
		this.event = event;
		this.session = session;
		this.userEntityESService = userEntityESService;
		this.credESService = credESService;
		this.userRole = userRole;
	}
	
	@Override
	public void process() {
		User user = null;
		EventType eventType = event.getAction();
		Map<String, String> params = event.getParameters();
		
		if (eventType == EventType.ENROLL_COURSE) {
			Credential1 cred = (Credential1) event.getObject();
			long instructorId = Long.parseLong(params.get("instructorId"));
			String dateEnrolledString = params.get("dateEnrolled");
			userEntityESService.addCredentialToUserIndex(
					cred.getId(), 
					event.getActorId(), 
					instructorId, 
					dateEnrolledString);
			//add student to credential index
			credESService.addStudentToCredentialIndex(cred.getId(), event.getActorId());
		} else if(eventType == EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR
				|| eventType == EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR
				|| eventType == EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR) {
			
			long credId = Long.parseLong(params.get("credId"));
			/*
			 * if unassigned, we should set instructorId to 0 in user index for this credential,
			 * if assigned, we should set id of instructor that is assigned
			 */
			long instructorId = 0;
			if (eventType == EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR
					|| eventType == EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR) {
				instructorId = event.getTarget().getId();
			}
			userEntityESService.assignInstructorToUserInCredential(event.getObject().getId(), credId, instructorId);
		} else if(eventType == EventType.INSTRUCTOR_ASSIGNED_TO_CREDENTIAL) {
			String dateAssigned = params.get("dateAssigned");
			userEntityESService.addInstructorToCredential(event.getTarget().getId(), event.getObject().getId(), dateAssigned);
			credESService.addInstructorToCredentialIndex(event.getTarget().getId(), event.getObject().getId());
		} else if(eventType == EventType.INSTRUCTOR_REMOVED_FROM_CREDENTIAL) {
			userEntityESService.removeInstructorFromCredential(event.getTarget().getId(), event.getObject().getId());
			credESService.removeInstructorFromCredentialIndex(event.getTarget().getId(), event.getObject().getId());
		} else if(eventType == EventType.ChangeProgress) {
	    	ChangeProgressEvent cpe = (ChangeProgressEvent) event;
	    	TargetCredential1 tc = (TargetCredential1) cpe.getObject();
	    	Credential1 cr = tc.getCredential();
	    	
			if (cr != null) {
		    	userEntityESService.changeCredentialProgress(cpe.getActorId(), cr.getId(), cpe.getNewProgressValue());
	    	}
	    } else if(eventType == EventType.Edit_Profile) {
	    	BaseEntity obj = event.getObject();
	    	long userId = 0;
	    	/*
	    	 * this means that actor is not the owner of edited profile.
	    	 * This is the case when admin is editing someone else's profile.
	    	 */
			if (obj != null && obj instanceof User) {
				userId = obj.getId();
			} else {
				userId = event.getActorId();
			}
	    	user = (User) session.load(User.class, userId);
	    	userEntityESService.updateBasicUserData(user, session);
		} else {
			if (userRole == EventUserRole.Subject) {
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
