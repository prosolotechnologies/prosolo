package org.prosolo.services.indexing.impl;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.ExternalToolActivity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
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
			EventType.Edit,
			EventType.Delete,
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
			EventType.ACTIVATE_COURSE
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] { 
			LearningGoal.class, 
			TargetLearningGoal.class,
			TargetCompetence.class, 
			Competence.class,
			Course.class,
			ResourceActivity.class, 
			ExternalToolActivity.class,
			UploadAssignmentActivity.class,
			User.class,
			CourseEnrollment.class
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
