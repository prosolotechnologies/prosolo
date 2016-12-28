package org.prosolo.services.indexing.impl.elasticSearchObserver;

import javax.inject.Inject;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.indexing.NodeEntityESService;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.indexing.UserGroupESService;
import org.springframework.stereotype.Service;

@Service
public class NodeChangeProcessorFactory {
			
	@Inject
	private UserEntityESService userEntityESService;
	@Inject
	private NodeEntityESService nodeEntityESService;
	@Inject
	private CredentialESService credentialESService;
	@Inject
	private CompetenceESService competenceESService;
	@Inject
	private UserGroupESService userGroupESService;
	
	public NodeChangeProcessor getNodeChangeProcessor(Event event, Session session) {
		EventType type = event.getAction();
		BaseEntity node = event.getObject();
		switch (type) {
			case Registered:
			case Edit_Profile:
			case ENROLL_COURSE:
			case COURSE_WITHDRAWN:
			case ACTIVATE_COURSE:
			case ChangeProgress:
				 return new UserNodeChangeProcessor(event, session, userEntityESService, 
						 EventUserRole.Subject);
			case Create:
			case Create_Draft:
			case Edit:
			case Edit_Draft:
			case ChangeVisibility:
			case STUDENT_ASSIGNED_TO_INSTRUCTOR:
			case STUDENT_UNASSIGNED_FROM_INSTRUCTOR:
			case INSTRUCTOR_ASSIGNED_TO_COURSE:
			case STUDENT_REASSIGNED_TO_INSTRUCTOR:
			case USER_ROLES_UPDATED:
			case INSTRUCTOR_REMOVED_FROM_COURSE:
				if (node instanceof User) {
					return new UserNodeChangeProcessor(event, session, userEntityESService, EventUserRole.Object);
				} else if (node instanceof TargetLearningGoal) {
					return new UserNodeChangeProcessor(event, session, userEntityESService, EventUserRole.Subject);
				} else if(node instanceof Credential1) {
					NodeOperation operation = null;
					if(type == EventType.Create || type == EventType.Create_Draft) {
						operation = NodeOperation.Save;
					} else {
						operation = NodeOperation.Update;
					}
					return new CredentialNodeChangeProcessor(event, credentialESService, operation, session);
				} else if(node instanceof Competence1) {
					NodeOperation operation = null;
					if(type == EventType.Create || type == EventType.Create_Draft) {
						operation = NodeOperation.Save;
					} else {
						operation = NodeOperation.Update;
					}
					return new CompetenceNodeChangeProcessor(event, competenceESService, operation, session);
				} else if(node instanceof UserGroup) {
					NodeOperation operation = null;
					if(type == EventType.Create) {
						operation = NodeOperation.Save;
					} else {
						operation = NodeOperation.Update;
					}
					return new UserGroupNodeChangeProcessor(event, userGroupESService, operation);
				} else {
					return new RegularNodeChangeProcessor(event, nodeEntityESService, NodeOperation.Save);
				}
				
			case Delete:
			case Delete_Draft:
				if(node instanceof Credential1) {
					return new CredentialNodeChangeProcessor(event, credentialESService, 
							NodeOperation.Delete, session);
				} else if(node instanceof Competence1) {
					return new CompetenceNodeChangeProcessor(event, competenceESService, 
							NodeOperation.Delete, session);
				} else if(node instanceof UserGroup) {
					return new UserGroupNodeChangeProcessor(event, userGroupESService, NodeOperation.Delete);
				}
				return new RegularNodeChangeProcessor(event, nodeEntityESService, NodeOperation.Delete);
			case Attach:
				//if(event.getObject() instanceof TargetActivity && event.getTarget() instanceof TargetCompetence) {
				//	return new AttachActivityNodeChangeProcessor(event, nodeEntityESService);
				//} 
				return null;
			case Bookmark:
				return new BookmarkNodeChangeProcessor(event, credentialESService, NodeOperation.Save);
			case RemoveBookmark:
				return new BookmarkNodeChangeProcessor(event, credentialESService, NodeOperation.Delete);
			default:
				return null;
		}
	}
	
}
