package org.prosolo.services.indexing.impl;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialBookmark;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.indexing.impl.elasticSearchObserver.NodeChangeProcessor;
import org.prosolo.services.indexing.impl.elasticSearchObserver.NodeChangeProcessorFactory;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.indexing.NodeChangeObserver")
public class NodeChangeObserver extends EventObserver {
	
	private static Logger logger = Logger.getLogger(NodeChangeObserver.class.getName());
	
	@Inject private NodeChangeProcessorFactory nodeChangeProcessorFactory;
	@Autowired private DefaultManager defaultManager;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
			EventType.Create,
			EventType.Create_Draft,
			EventType.Edit,
			EventType.Edit_Draft,
			EventType.Delete,
			EventType.Delete_Draft,
			EventType.ChangeVisibility,
			EventType.Registered,
			EventType.Attach,
			EventType.Edit_Profile,
			EventType.ENROLL_COURSE,
			EventType.ENROLL_COMPETENCE,
			EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR,
			EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR,
			EventType.INSTRUCTOR_ASSIGNED_TO_CREDENTIAL,
			EventType.INSTRUCTOR_REMOVED_FROM_CREDENTIAL,
			EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR,
			EventType.USER_ROLES_UPDATED,
			EventType.COURSE_WITHDRAWN,
			EventType.ACTIVATE_COURSE,
			EventType.Bookmark,
			EventType.RemoveBookmark,
			EventType.ChangeProgress,
			EventType.Follow,
			EventType.Unfollow,
			EventType.ADD_USER_TO_GROUP,
			EventType.REMOVE_USER_FROM_GROUP,
			EventType.RESOURCE_VISIBILITY_CHANGE,
			EventType.VISIBLE_TO_ALL_CHANGED,
			EventType.STATUS_CHANGED,
			EventType.ARCHIVE,
			EventType.RESTORE,
			EventType.USER_GROUP_CHANGE
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] { 
			User.class,
			Credential1.class,
			Competence1.class, 
			CredentialBookmark.class,
			TargetCredential1.class,
			TargetCompetence1.class,
			UserGroup.class
		};
	}

	@Override
	public void handleEvent(Event event) {
		Session session = (Session) defaultManager.getPersistence().openSession();
		try {
			NodeChangeProcessor processor = nodeChangeProcessorFactory.getNodeChangeProcessor(event, session);
			if(processor != null) {
				processor.process();
			}
			session.flush();
		} catch(Exception e) {
			logger.error("Exception in handling message",e);
		} finally {
			HibernateUtil.close(session);
		}
	}
}
