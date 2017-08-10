package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.ChangeProgressEvent;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.indexing.UserEntityESService;

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
	
	public UserNodeChangeProcessor(Event event, Session session, UserEntityESService userEntityESService,
			CredentialESService credESService, CompetenceESService compESService, EventUserRole userRole) {
		this.event = event;
		this.session = session;
		this.userEntityESService = userEntityESService;
		this.credESService = credESService;
		this.compESService = compESService;
		this.userRole = userRole;
	}
	
	@Override
	public void process() {
		User user = null;
		EventType eventType = event.getAction();
		Map<String, String> params = event.getParameters();

		if (eventType == EventType.Delete) {
			userEntityESService.removeUserFromIndex((User) session.load(User.class, event.getObject().getId()));
		} else if (eventType == EventType.ADD_USER_TO_UNIT) {
			String roleIdStr = params.get("roleId");
			Unit unit = (Unit) session.load(Unit.class, event.getTarget().getId());
			userEntityESService.addUserToUnitWithRole(unit.getOrganization().getId(),
					event.getObject().getId(), unit.getId(), Long.parseLong(roleIdStr));
		} else if (eventType == EventType.REMOVE_USER_FROM_UNIT) {
			String roleIdStr = params.get("roleId");
			Unit unit = (Unit) session.load(Unit.class, event.getTarget().getId());
			userEntityESService.removeUserFromUnitWithRole(unit.getOrganization().getId(),
					event.getObject().getId(), unit.getId(), Long.parseLong(roleIdStr));
		} else if (eventType == EventType.USER_ASSIGNED_TO_ORGANIZATION) {
			userEntityESService.addUserToOrganization(
					(User) session.load(User.class, event.getObject().getId()), event.getTarget().getId(), session);
		} else if (eventType == EventType.USER_REMOVED_FROM_ORGANIZATION) {
			userEntityESService.removeUserFromOrganization(
					(User) session.load(User.class, event.getObject().getId()), event.getTarget().getId());
		} else if (eventType == EventType.ENROLL_COURSE) {
			Credential1 cred = (Credential1) event.getObject();
			long instructorId = Long.parseLong(params.get("instructorId"));
			String dateEnrolledString = params.get("dateEnrolled");
			String prog = params.get("progress");
			int progress = prog != null ? Integer.parseInt(prog) : 0;
			userEntityESService.addCredentialToUserIndex(
					cred.getId(), 
					event.getActorId(), 
					instructorId,
					progress,
					dateEnrolledString);
			//add student to credential index
			credESService.addStudentToCredentialIndex(cred.getId(), event.getActorId());
		} else if(eventType == EventType.ENROLL_COMPETENCE) {
			Competence1 comp = (Competence1) event.getObject();
			String date = params.get("dateEnrolled");
			userEntityESService.addCompetenceToUserIndex(
					comp.getId(), 
					event.getActorId(),  
					date);
			compESService.addStudentToCompetenceIndex(comp.getId(), event.getActorId());
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
	    	BaseEntity object = cpe.getObject();
	    	if (object instanceof TargetCredential1) {
		    	TargetCredential1 tc = (TargetCredential1) cpe.getObject();
		    	Credential1 cr = tc.getCredential();
		    	
				if (cr != null) {
			    	userEntityESService.changeCredentialProgress(cpe.getActorId(), cr.getId(), cpe.getNewProgressValue());
		    	}
	    	} else if (object instanceof TargetCompetence1) {
	    		TargetCompetence1 tc = (TargetCompetence1) cpe.getObject();
		    	Competence1 c = tc.getCompetence();
		    	
				if (c != null) {
					String dateCompleted = null;
					if (params != null) {
						dateCompleted = params.get("dateCompleted");
					}
			    	userEntityESService.updateCompetenceProgress(cpe.getActorId(), c.getId(), cpe.getNewProgressValue(),
			    			dateCompleted);
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
			userEntityESService.saveUserNode((User) session.load(User.class, event.getActorId()), session);
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
