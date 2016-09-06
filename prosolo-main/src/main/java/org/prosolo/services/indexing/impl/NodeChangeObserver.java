package org.prosolo.services.indexing.impl;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialBookmark;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
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
	@Inject NodeChangeProcessorFactory nodeChangeProcessorFactory;
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
			EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR,
			EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR,
			EventType.INSTRUCTOR_ASSIGNED_TO_COURSE,
			EventType.INSTRUCTOR_REMOVED_FROM_COURSE,
			EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR,
			EventType.USER_ROLES_UPDATED,
			EventType.COURSE_WITHDRAWN,
			EventType.ACTIVATE_COURSE,
			EventType.Bookmark,
			EventType.RemoveBookmark,
			EventType.ChangeProgress
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] { 
			User.class,
			CourseEnrollment.class,
			Credential1.class,
			Competence1.class, 
			CredentialBookmark.class,
			TargetCredential1.class
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
