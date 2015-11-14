package org.prosolo.services.learningProgress;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.learningProgress.LearningProgressObserver")
public class LearningProgressObserver implements EventObserver {
	
	@Autowired private ApplicationBean applicationBean;

	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
			EventType.Create, 
			EventType.Attach,
			EventType.Detach, 
			EventType.Delete, 
			EventType.Edit, 
			EventType.Like, 
			EventType.AddNote,
			EventType.JOIN_GOAL_INVITATION_ACCEPTED
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] { 
			TargetLearningGoal.class,
			TargetCompetence.class,
		};
	}

	@Override
	public void handleEvent(Event event) {
		User maker = event.getActor();
		BaseEntity object = event.getObject();
		BaseEntity target = event.getTarget();
		
		HttpSession userSession = applicationBean.getUserSession(maker.getId());
		
		if (userSession != null) {
			
			if (object != null && object instanceof TargetLearningGoal ||
					target != null && target instanceof TargetLearningGoal) {
				
				TargetLearningGoal targetGoal = null;
				
				if (object != null && object instanceof TargetLearningGoal) {
					targetGoal = (TargetLearningGoal) object;
				} else {
					targetGoal = (TargetLearningGoal) target;
				}
				
				LearnBean goalsBean = (LearnBean) userSession.getAttribute("learninggoals");
				
				if (goalsBean != null) {
					GoalDataCache goalData = goalsBean.getData().getDataForTargetGoal(targetGoal.getId());
					
					if (goalData != null) {
						Date dateCreated = event.getDateCreated();
						
						goalData.getData().setLastActivity(dateCreated);
						return;
					}
				}
			}
			
//			if (object != null && object instanceof TargetCompetence ||
//					target != null && target instanceof TargetCompetence) {
//				
//				TargetCompetence targetComp = null;
//				
//				if (object != null && object instanceof TargetCompetence) {
//					targetComp = (TargetCompetence) object;
//				} else {
//					targetComp = (TargetCompetence) target;
//				}
//				
//				LearningGoalsBean goalsBean = (LearningGoalsBean) userSession.getAttribute("learninggoals");
//				
//				if (goalsBean != null) {
//					CompetenceDataCache compData = goalsBean.getData().getDataForTargetCompetence(targetComp.getId());
//					
//					if (compData != null) {
//						Date dateCreated = event.getDateCreated();
//						
//						compData.getData().setLastActivity(dateCreated);
//						return;
//					}
//				}
//			}
		}
	}
}
