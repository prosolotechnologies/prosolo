package org.prosolo.services.nodes.observers.privilege;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Map;

@Service("org.prosolo.services.nodes.observers.privilege.UserPrivilegePropagationObserver")
public class UserPrivilegePropagationObserver extends EventObserver {

private static Logger logger = Logger.getLogger(UserPrivilegePropagationObserver.class.getName());
	
	@Inject private UserGroupManager userGroupManager;
	@Inject private DefaultManager defaultManager;
	@Inject private EventFactory eventFactory;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
				EventType.Attach,
				EventType.Detach,
				EventType.USER_GROUP_ADDED_TO_RESOURCE,
				EventType.USER_GROUP_REMOVED_FROM_RESOURCE,
				EventType.ENROLL_COURSE
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] {
			Competence1.class,
			UserGroup.class,
			CredentialUserGroup.class,
			Credential1.class
		};
	}

	public void handleEvent(Event event) {
		logger.info("UserPrivilegePropagationObserver triggered with event: " + event.getAction());
		Session session = (Session) defaultManager.getPersistence().openSession();
		
		BaseEntity object = event.getObject();
		BaseEntity target = event.getTarget();
		Map<String, String> params = event.getParameters();
		
		Result<Void> res = null;
		boolean success = false;
		
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			switch(event.getAction()) {
			    //on attach propagate all credential user groups to competence attached
				case Attach:
					if (object instanceof Competence1) {
						res = userGroupManager.propagateUserGroupPrivilegesFromCredentialToCompetenceAndGetEvents(target.getId(), 
								object.getId(), UserContextData.of(event.getActorId(), event.getOrganizationId(),
										event.getSessionId(), event.getIpAddress(), null),  session);
					}
					break;
			    //on detach remove all comp groups in detached competence inherited from that credential
				case Detach:
					if (object instanceof Competence1) {
						res = userGroupManager.removeUserGroupPrivilegesPropagatedFromCredentialAndGetEvents(object.getId(), 
								target.getId(), UserContextData.of(event.getActorId(), event.getOrganizationId(),
										event.getSessionId(), event.getIpAddress(), null), session);
					}
					break;
				//on user group added to resource add that group to all competences in a credential
				case USER_GROUP_ADDED_TO_RESOURCE:
					if (target instanceof Credential1) {
						long credUserGroupId = Long.parseLong(params.get("credentialUserGroupId"));
						UserGroupPrivilege privilege = UserGroupPrivilege.valueOf(params.get("privilege"));
						//only edit and instruct privileges should be propagated when user group is added to credential
						if (privilege == UserGroupPrivilege.Edit || privilege == UserGroupPrivilege.Instruct) {
							res = userGroupManager.propagateUserGroupPrivilegeFromCredentialAndGetEvents(credUserGroupId,
									UserContextData.of(event.getActorId(), event.getOrganizationId(),
											event.getSessionId(), event.getIpAddress(), null), session);
						}
					}
					break;
				//on user group removed from resource remove that group from all competences too
				case USER_GROUP_REMOVED_FROM_RESOURCE:
					if (target instanceof Credential1) {
						//user group removed should be propagated only if it is added with Edit privilege
						if (params != null) {
							String priv = params.get("privilege");
							if (priv != null) {
								UserGroupPrivilege privilege = UserGroupPrivilege.valueOf(priv);
								if (privilege == UserGroupPrivilege.Edit) {
									res = userGroupManager.removeUserGroupPrivilegePropagatedFromCredentialAndGetEvents(
											target.getId(), object.getId(), UserContextData.of(
													event.getActorId(), event.getOrganizationId(), event.getSessionId(),
													event.getIpAddress(),null), session);
								}
							}
						}
					}
					break;
				case ENROLL_COURSE:
					res = userGroupManager.addLearnPrivilegeToCredentialCompetencesAndGetEvents(
							object.getId(), event.getActorId(), UserContextData.of(event.getActorId(),
									event.getOrganizationId(), event.getSessionId(), event.getIpAddress(), null), session);
					break;
				default:
					break;
			}
			transaction.commit();
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			transaction.rollback();
		} finally {
			HibernateUtil.close(session);
		}
		if(success == true && res != null) {
			eventFactory.generateEvents(res.getEventQueue(), event.getObserversToExclude());
		}
		logger.info("UserPrivilegePropagationObserver finished");
	}
	
}
