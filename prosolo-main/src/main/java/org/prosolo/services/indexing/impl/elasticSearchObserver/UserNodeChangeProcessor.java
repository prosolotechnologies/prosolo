package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.CredentialManager;

import java.util.Map;

public class UserNodeChangeProcessor implements NodeChangeProcessor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(UserNodeChangeProcessor.class);
	
	private Event event;
	private Session session;
	private UserEntityESService userEntityESService;
	private CredentialESService credESService;
	private CompetenceESService compESService;
	private EventUserRole userRole;
	private CredentialManager credManager;
	
	public UserNodeChangeProcessor(Event event, Session session, UserEntityESService userEntityESService,
			CredentialESService credESService, CompetenceESService compESService, CredentialManager credManager, EventUserRole userRole) {
		this.event = event;
		this.session = session;
		this.userEntityESService = userEntityESService;
		this.credESService = credESService;
		this.compESService = compESService;
		this.credManager = credManager;
		this.userRole = userRole;
	}
	
	@Override
	public void process() {
		User user = null;
		EventType eventType = event.getAction();
		Map<String, String> params = event.getParameters();

		if (eventType == EventType.Delete) {
			userEntityESService.removeUserFromIndex((User) session.load(User.class, event.getObject().getId()));
		} else if (eventType == EventType.ADD_USER_TO_UNIT || eventType == EventType.REMOVE_USER_FROM_UNIT) {
			userEntityESService.updateRoles(event.getObject().getId(), session);
		} else if (eventType == EventType.USER_ASSIGNED_TO_ORGANIZATION) {
			userEntityESService.addUserToOrganization(
					(User) session.load(User.class, event.getObject().getId()), event.getTarget().getId(), session);
		} else if (eventType == EventType.USER_REMOVED_FROM_ORGANIZATION) {
			userEntityESService.removeUserFromOrganization(
					(User) session.load(User.class, event.getObject().getId()), event.getTarget().getId());
		} else if (eventType == EventType.ENROLL_COURSE) {
			//reindex user credentials collection
			userEntityESService.updateCredentials(event.getOrganizationId(), event.getActorId(), session);
			//reindex credential students collection
			credESService.updateStudents(event.getOrganizationId(), event.getObject().getId());
		} else if(eventType == EventType.ENROLL_COMPETENCE) {
			//reindex user competences collection
			userEntityESService.updateCompetences(event.getOrganizationId(), event.getActorId(), session);
			//reindex competence students index
			compESService.updateStudents(event.getOrganizationId(), event.getObject().getId());
		} else if(eventType == EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR
				|| eventType == EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR
				|| eventType == EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR) {
			
			long credId = Long.parseLong(params.get("credId"));
			long userId = event.getObject().getId();

			Long instId = credManager.getInstructorUserId(userId, credId, session);
			long instructorId = instId != null ? instId.longValue() : 0;

			userEntityESService.assignInstructorToUserInCredential(event.getOrganizationId(), userId,
					credId, instructorId);
		} else if(eventType == EventType.INSTRUCTOR_ASSIGNED_TO_CREDENTIAL || eventType == EventType.INSTRUCTOR_REMOVED_FROM_CREDENTIAL) {
			userEntityESService.updateCredentialsWithInstructorRole(event.getOrganizationId(), event.getObject().getId());
			credESService.updateInstructors(event.getOrganizationId(), event.getTarget().getId(), session);
		} else if(eventType == EventType.ChangeProgress) {
	    	BaseEntity object = event.getObject();
	    	if (object instanceof TargetCredential1) {
		    	TargetCredential1 tc = (TargetCredential1) session.load(TargetCredential1.class, object.getId());
		    	if (tc != null) {
					userEntityESService.changeCredentialProgress(event.getOrganizationId(), event.getActorId(), tc.getCredential().getId(), tc.getProgress());
				}
	    	} else if (object instanceof TargetCompetence1) {
	    		TargetCompetence1 tc = (TargetCompetence1) session.load(TargetCompetence1.class, event.getObject().getId());
				if (tc != null) {
			    	userEntityESService.updateCompetenceProgress(event.getOrganizationId(), event.getActorId(), tc);
		    	}
	    	}
	    } else if (eventType == EventType.Edit_Profile) {
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
		} else if (eventType == EventType.Registered) {
			//if object is not null than object id is id of a new user, otherwise it is actor id.
			long newUserId = event.getObject() != null ? event.getObject().getId() : event.getActorId();
			userEntityESService.saveUserNode((User) session.load(User.class, newUserId), session);
		} else if (eventType == EventType.Account_Activated || eventType == EventType.USER_ROLES_UPDATED) {
			/*
			we need to update whole index when roles are updated because we don't know if user exists
			in one of the indexes (system or organization index) so maybe he should be indexed for the first time
			 */
			userEntityESService.saveUserNode((User) session.load(User.class, event.getObject().getId()),
					session);
		} else {
			//TODO check if there is a use case where this block is entered
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
