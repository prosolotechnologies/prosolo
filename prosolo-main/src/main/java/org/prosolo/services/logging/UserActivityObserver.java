package org.prosolo.services.logging;

import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zoran Jeremic
 * @deprecated
 */
@Deprecated
@Service("org.prosolo.services.logging.UserActivityObserver")
public class UserActivityObserver extends EventObserver {
	
	protected static Logger logger = Logger.getLogger(UserActivityObserver.class);
	
	@Autowired private LoggingService loggingService;
	@Autowired private AnalyticalServiceCollector analyticalServiceCollector;
	@Autowired private DefaultManager defaultManager;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
				EventType.Create, 
				EventType.Attach,
				EventType.Detach, 
				EventType.Delete, 
 				EventType.Comment,
				EventType.Edit, 
				EventType.Like, 
				EventType.Dislike,
				EventType.RemoveDislike,
				EventType.Post, 
				EventType.PostShare, 
				EventType.AddNote,
				EventType.FileUploaded,
				EventType.LinkAdded,
				EventType.JOIN_GOAL_REQUEST,
				EventType.JOIN_GOAL_INVITATION,
				EventType.EVALUATION_REQUEST,
				EventType.SEND_MESSAGE,
				EventType.START_MESSAGE_THREAD,
				EventType.ENROLL_COURSE,
				EventType.CREDENTIAL_COMPLETED,
				EventType.RemoveLike,
				EventType.UPDATE_HASHTAGS
			};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		Class<? extends BaseEntity>[] resources=new Class[] { 
				Credential1.class, 
				User.class,
			};
		return resources;
	}

	@Override
	public void handleEvent(Event event) {
		Session session = (Session) defaultManager.getPersistence().openSession();
		
 		try {
			if (event.getActorId() > 0) {
				long userid = event.getActorId();
				
				BaseEntity object = (BaseEntity) session.merge(event.getObject());
				if (Settings.getInstance().config.analyticalServerConfig.enabled) {
					logger.debug("Handling user action:" + event.getAction().name() + " " + event.getObject().getClass().getName() + " userid:" + userid);
					analyticalServiceCollector.increaseUserActivityLog(userid, DateUtil.getDaysSinceEpoch());
				} else {
					loggingService.increaseUserActivityLog(userid, DateUtil.getDaysSinceEpoch());
				}			
				logger.debug("Handling user action:" + event.getAction().name() + " " + event.getObject().getClass().getName() + " userid:" + userid);
				 
				BaseEntity target = event.getTarget();
				
				if (object != null) {
					object = HibernateUtil.initializeAndUnproxy(object);
				}
			
				if (Settings.getInstance().config.analyticalServerConfig.enabled) {
					//TODO remove if not needed
//					if (object instanceof TargetActivity) {
//						TargetActivity targetActivity = (TargetActivity) session.load(TargetActivity.class, object.getId());
//						Activity activity = targetActivity.getActivity();
//						TargetCompetence targetCompetence = targetActivity.getParentCompetence();
//						Competence competence = targetCompetence.getCompetence();						
//				
//						analyticalServiceCollector.createActivityInteractionData(competence.getId(), activity.getId());
//						
//						List<TargetActivity> tActivities=targetCompetence.getTargetActivities();
//						analyticalServiceCollector.createTargetCompetenceActivitiesData(competence.getId(), targetCompetence.getId(),tActivities);
//					}
//					TargetLearningGoal targetLearningGoal=null;
//					if(target instanceof TargetLearningGoal){						
//						targetLearningGoal=(TargetLearningGoal) target;
//					}else if(target instanceof TargetCompetence){
//						TargetCompetence targetCompetence =(TargetCompetence) target;
//						targetLearningGoal=targetCompetence.getParentGoal();
//
//					}else if(target instanceof TargetActivity){
//						//TargetActivity targetActivity =(TargetActivity) target;
//						TargetActivity targetActivity = (TargetActivity) session.load(TargetActivity.class, target.getId());
//						TargetCompetence targetCompetence = targetActivity.getParentCompetence();
//						targetLearningGoal=targetCompetence.getParentGoal();
//
//					}else if(object instanceof TargetActivity){
//						TargetActivity targetActivity =(TargetActivity) object;
//						TargetCompetence targetCompetence = targetActivity.getParentCompetence();
//						targetLearningGoal=targetCompetence.getParentGoal();
//					}
//					if(targetLearningGoal!=null){
//						analyticalServiceCollector.increaseUserActivityForCredentialLog(userid, targetLearningGoal.getLearningGoal().getId(),DateUtil.getDaysSinceEpoch());
//					}
					if(event.getAction().equals(EventType.UPDATE_HASHTAGS)){
						long credId=0, userId=0;
						if(object instanceof Credential1) {
							credId = object.getId();
						} else if(object instanceof User) {
							userId = object.getId();
						}
						Map<String, String> parameters=event.getParameters();
						System.out.println("SHOULD UPDATE HASHTAGS ON SERVER HERE..."+parameters.toString());
						analyticalServiceCollector.sendUpdateHashtagsMessage(parameters, credId, userId);
					}
		
				}
			} else {
				logger.debug("Event without actor:" + event.getAction().name() + " " + event.getObject().getClass().getName());
			}
			
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		}
		finally {
			HibernateUtil.close(session);
		}
	}

}
