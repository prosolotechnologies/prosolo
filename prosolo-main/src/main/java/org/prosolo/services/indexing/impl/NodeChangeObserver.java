package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.ExternalToolActivity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.indexing.NodeEntityESService;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.indexing.NodeChangeObserver")
public class NodeChangeObserver implements EventObserver {
	private static Logger logger = Logger.getLogger(NodeChangeObserver.class.getName());
	@Autowired private NodeEntityESService nodeEntityESService;
	@Autowired private UserEntityESService userEntityESService;
	@Autowired private DefaultManager defaultManager;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
			EventType.Create,
			EventType.Edit,
			EventType.Delete,
			EventType.ChangeVisibility,
			EventType.Registered,
			EventType.Attach
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
			User.class
		};
	}

	@Override
	public void handleEvent(Event event) {
		Session session = (Session) defaultManager.getPersistence().openSession();
		try{
		BaseEntity node = event.getObject();
		if (event.getAction().equals(EventType.Registered)) {
			User newuser = event.getActor();
			// User user=(User) session.load(User.class, newuser.getId());
			 userEntityESService.saveUserNode(newuser, session);
			 
		 } else if (event.getAction().equals(EventType.Create)
				|| event.getAction().equals(EventType.Edit)
				|| event.getAction().equals(EventType.ChangeVisibility)) {
			if (node instanceof User) {
				User user = (User) session.load(User.class, node.getId());
				userEntityESService.saveUserNode(user, session);
				
			//added for testing
			} else if (node instanceof TargetLearningGoal) {
				long actorId = event.getActor().getId();
				User actor = (User) session.load(User.class, actorId);
				//User actor = event.getActor();
//				actor = (User) session.merge(actor);
				userEntityESService.saveUserNode(actor, session);
			} else {
				nodeEntityESService.saveNodeToES(node);
			}
		} else if (event.getAction().equals(EventType.Delete)) {
			nodeEntityESService.deleteNodeFromES(event.getObject());
		}else if (event.getAction().equals(EventType.Attach)){
			if(event.getObject() instanceof TargetActivity && event.getTarget() instanceof TargetCompetence){
				logger.info("attaching targetActivity to target Competence...Should add competence to activity");
				Activity activityToUpdate=((TargetActivity) event.getObject()).getActivity();
				nodeEntityESService.saveNodeToES(activityToUpdate);
				//nodeEntityESService.attachTargetActivityToCompetence((TargetActivity) event.getObject(),(TargetCompetence) event.getTarget());
			}
		}
		
		if (node instanceof LearningGoal) {
			//node = (LearningGoal) node;
			long actorId = event.getActor().getId();
			User actor = (User) session.load(User.class, actorId);
			//User actor = event.getActor();
//			actor = (User) session.merge(actor);
			userEntityESService.saveUserNode(actor, session);
		}
		session.flush();
		}catch(Exception e){
			logger.error("Exception in handling message",e);
		}finally{
			HibernateUtil.close(session);
		}
	}
}
