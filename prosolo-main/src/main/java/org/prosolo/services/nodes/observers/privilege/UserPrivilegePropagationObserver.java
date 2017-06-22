package org.prosolo.services.nodes.observers.privilege;

import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.springframework.stereotype.Service;

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
				EventType.Create,
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
								object.getId(), session);
					}
					break;
			    //on detach remove all comp groups in detached competence inherited from that credential
				case Detach:
					if (object instanceof Competence1) {
						res = userGroupManager.removeUserGroupPrivilegesPropagatedFromCredentialAndGetEvents(object.getId(), 
								target.getId(), session);
					}
					break;
				//on user group added to resource add that group to all competences in a credential
				case USER_GROUP_ADDED_TO_RESOURCE:
					if (target instanceof Credential1) {
						long credUserGroupId = Long.parseLong(params.get("credentialUserGroupId"));
						UserGroupPrivilege privilege = UserGroupPrivilege.valueOf(params.get("privilege"));
						//only edit privilege should be propagated when user group is added to credential
						if (privilege == UserGroupPrivilege.Edit) {
							res = userGroupManager.propagateUserGroupPrivilegeFromCredentialAndGetEvents(credUserGroupId,
									session);
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
											target.getId(), object.getId(), session);
								}
							}
						}
					}
					break;
				case Create:
					if (object instanceof Credential1) {
						Credential1 credObj = (Credential1) object;
						if(credObj.getType() == CredentialType.Delivery) {
							res = userGroupManager
									.propagateUserGroupEditPrivilegesFromCredentialToDeliveryAndGetEvents(
											credObj.getDeliveryOf().getId(), credObj.getId(), session);
						}
					} else if (object instanceof Competence1) {
						//todo observer refactor - remove this because we should only react to attach event for competence
						if (params != null) {
							String credIdString = params.get("credentialId");
							if (credIdString != null) {
								long credId = Long.parseLong(credIdString);
								if (credId > 0) {
									res = userGroupManager
											.propagateUserGroupPrivilegesFromCredentialToCompetenceAndGetEvents(credId,
													object.getId(), session);
								}
							}
						}
					}
					break;
				case ENROLL_COURSE:
					res = userGroupManager.addLearnPrivilegeToCredentialCompetencesAndGetEvents(
							object.getId(), event.getActorId(), 0, null, session);
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
			try {
				for (EventData ev : res.getEvents()) {
	    			eventFactory.generateEvent(ev);
	    		}
			} catch (EventException ee) {
				logger.error(ee);
			}
		}
		logger.info("UserPrivilegePropagationObserver finished");
	}
	
}