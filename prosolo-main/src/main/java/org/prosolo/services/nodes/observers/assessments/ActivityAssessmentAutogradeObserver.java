package org.prosolo.services.nodes.observers.assessments;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.nodes.observers.assessments.ActivityAssessmentAutogradeObserver")
public class ActivityAssessmentAutogradeObserver extends EventObserver {

	private static Logger logger = Logger.getLogger(ActivityAssessmentAutogradeObserver.class.getName());
	
	@Inject private AssessmentManager assessmentManager;
	@Inject private DefaultManager defaultManager;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
				EventType.Completion
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] {
				TargetActivity1.class
		};
	}

	public void handleEvent(Event event) {		
		Session session = (Session) defaultManager.getPersistence().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			long userId = event.getActorId();
			long tActId = event.getObject().getId();
			TargetActivity1 ta = (TargetActivity1) session.get(TargetActivity1.class, tActId);
			//if autograde option equals true maximum grade for all activity assessments is set
			if(ta.getActivity().isAutograde()) {
				assessmentManager.createOrUpdateActivityAssessmentsForExistingCompetenceAssessments(
						userId, 0, ta.getTargetCompetence().getId(), ta.getId(), 
						ta.getActivity().getMaxPoints(), session);
			}
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			transaction.rollback();
		} finally {
			HibernateUtil.close(session);
		}
	}

}
