package org.prosolo.services.nodes.observers.assessments;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.impl.util.activity.ActivityExternalAutogradeVisitor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service("org.prosolo.services.nodes.observers.assessments.ActivityAssessmentAutogradeObserver")
public class ActivityAssessmentAutogradeObserver extends EventObserver {

	private static Logger logger = Logger.getLogger(ActivityAssessmentAutogradeObserver.class.getName());
	
	@Inject private AssessmentManager assessmentManager;
	@Inject private DefaultManager defaultManager;
	@Inject private EventFactory eventFactory;
	
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

		Result<Void> result = null;
		try {
			transaction = session.beginTransaction();
			long userId = event.getActorId();
			long tActId = event.getObject().getId();
			TargetActivity1 ta = (TargetActivity1) session.get(TargetActivity1.class, tActId);
			//if automatic grade by activity completion is set maximum grade for all activity assessments is set
			ActivityExternalAutogradeVisitor visitor = new ActivityExternalAutogradeVisitor();
			ta.getActivity().accept(visitor);
			boolean externalAutograde = visitor.isAutogradeByExternalGrade();
			if (ta.getActivity().getGradingMode() == GradingMode.AUTOMATIC && !externalAutograde) {
				PageContextData lcd = new PageContextData();
				lcd.setLearningContext("name:autograde|id:" + ta.getId());
				result = assessmentManager.updateActivityAutomaticGradeInAllAssessmentsAndGetEvents(
						userId, ta.getActivity().getId(), ta.getActivity().getMaxPoints(), session,
						UserContextData.of(event.getActorId(), event.getOrganizationId(), event.getSessionId(), lcd));
			}
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			transaction.rollback();
		} finally {
			HibernateUtil.close(session);
		}

		if (result != null) {
			eventFactory.generateEvents(result.getEventQueue());
		}
	}

}
