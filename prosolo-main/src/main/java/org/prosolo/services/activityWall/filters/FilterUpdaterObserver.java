package org.prosolo.services.activityWall.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.course.CourseEnrollment;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.nodes.VisibilityManager;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic Mar 1, 2015
 *
 */
@Service("org.prosolo.services.activitystream.filters.FilterUpdaterObserver")
public class FilterUpdaterObserver implements EventObserver {

	private static Logger logger = Logger.getLogger(FilterUpdaterObserver.class
			.getName());
	@Autowired
	private ApplicationBean applicationBean;
	@Autowired
	private TagManager tagManager;
	@Autowired
	private VisibilityManager visibilityManager;

	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { EventType.Create, EventType.Attach,
				EventType.Detach, EventType.ENROLL_COURSE,
				EventType.ACTIVATE_COURSE, EventType.Follow,
				EventType.Unfollow, EventType.UPDATE_HASHTAGS };
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] { LearningGoal.class, TargetLearningGoal.class,
				TargetCompetence.class, Activity.class, TargetActivity.class,
				User.class, CourseEnrollment.class };
	}

	@Override
	public void handleEvent(Event event) {
		logger.info("comming in event with action: " + event.getAction());
		logger.info("comming in event with actor: " + event.getActor());
		BaseEntity object=null;
		if(event.getObject()!=null){
			 object=HibernateUtil.initializeAndUnproxy(event.getObject());
			 logger.info("comming in event with object: "
						+ object.getClass().getSimpleName()+" id:"+object.getId());
		}		
		BaseEntity target=null;
		if(event.getTarget()!=null){
			target=HibernateUtil.initializeAndUnproxy(event.getTarget());
			logger.info("comming in event with target: "
					+ target.getClass().getSimpleName()+" id:"+target.getId());
		}
		// User actor=event.getActor();
	
		VisibilityType objectVisibility = null;// VisibilityType.PUBLIC;
		if (target != null && (target instanceof Node)) {
			objectVisibility = this.getObjectVisibitity((Node) target);
		}else if(object !=null  && (object instanceof Node)) {
			objectVisibility = this.getObjectVisibitity((Node) object);
		}

		if (event.getAction().equals(EventType.Attach)) {
			if ((target instanceof TargetLearningGoal)
					&& (object instanceof TargetCompetence)) {
				updateFiltersOnAttachTargetCompetenceToTargetLearningGoal(target.getId(), object.getId(), event
						.getActor().getId(), objectVisibility);
			} else if ((target instanceof TargetCompetence)
					&& (object instanceof TargetActivity)) {
				updateFiltersOnAttachTargetActivityToTargetCompetence(event
						.getTarget().getId(), object.getId(), event
						.getActor().getId(), objectVisibility);
			}
		} else if (event.getAction().equals(EventType.Detach)) {
			if ((target instanceof TargetLearningGoal)
					&& (object instanceof TargetCompetence)) {
				System.out
						.println("Detaching Target Competence from Target learning goal...");
				updateFiltersOnDetachTargetCompetenceFromTargetLearningGoal(
						target.getId(), object.getId(),
						event.getActor().getId());
			} else if ((target instanceof TargetCompetence)
					&& (object instanceof TargetActivity)) {
				System.out
						.println("Detaching Target Activity from Target Competence...");
				updateFiltersOnDetachTargetActivityFromTargetCompetence(event
						.getTarget().getId(), object.getId(), event
						.getActor().getId());
			} else if (object instanceof TargetLearningGoal) {
				System.out
						.println("Detaching/Deleting Target learning Goal. Not processed yet");
			}
		} else if (event.getAction().equals(EventType.ENROLL_COURSE)) {
			CourseEnrollment courseEnrollment = (CourseEnrollment) event
					.getObject();
	
			this.updateFiltersForCourseEnrollement(event.getActor().getId(), courseEnrollment.getCourse().getId(), courseEnrollment.getTargetGoal().getId());
			
			// System.out.println("ENROLL TO THE COURSE:"+courseEnrollment.getCourse().getId()+" LearningGoal:"+courseEnrollment.getTargetGoal().getId());
		} else if (event.getAction().equals(EventType.Follow)) {
			if (object instanceof User) {
				this.updateFiltersOnFollowUser(event.getActor().getId(), event
						.getObject().getId());
			}
		} else if (event.getAction().equals(EventType.Unfollow)) {
			if (object instanceof User) {
				this.updateFiltersOnUnfollowUser(event.getActor().getId(),
						object.getId());
			}
		} else if (event.getAction().equals(EventType.UPDATE_HASHTAGS)) {
			long resourceid = 0;
			long userid = 0;
			if (object instanceof User) {
				userid = object.getId();
			} else {
				resourceid = object.getId();
			}
			Map<String, String> parameters = event.getParameters();
			updateFiltersForUpdateHashtags(event.getActor().getId(),
					parameters.get("oldhashtags"),
					parameters.get("newhashtags"), resourceid, userid);
		}
	}

	private VisibilityType getObjectVisibitity(Node object) {
		// Visibility is inherited from TargetLearningGoal
		if(object instanceof TargetLearningGoal){
			return ((TargetLearningGoal) object).getVisibility();
		}else if (object instanceof TargetLearningGoal) {
			return object.getVisibility();
		} else if (object instanceof TargetActivity) {
			return visibilityManager.retrieveTargetActivityVisibility(object
					.getId());
		}else if(object instanceof TargetCompetence){
			return visibilityManager.retrieveTargetCompetenceVisibility(object
					.getId());
		}else{
			System.out.println("Return null visibility. Check this");
		}
			return null;
	}
	private void updateFiltersForCourseEnrollement(long actorId, long courseId, long targetLearningGoalId){

		// Update Course filters for all logged users
		Map<Long, HttpSession> httpSessions = applicationBean
				.getAllHttpSessions();
		for (Long id : httpSessions.keySet()) {
			HttpSession httpSession = httpSessions.get(id);
			if (httpSession != null) {
				LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
						.getAttribute("loggeduser");
				if (loggedUserBean.getSelectedStatusWallFilter() instanceof CourseFilter) {
					CourseFilter courseFilter = (CourseFilter) loggedUserBean
							.getSelectedStatusWallFilter();
					System.out.println("SELECTED COURSE FILTER: user:" + id
							+ " " + loggedUserBean.getName() + " "
							+ loggedUserBean.getLastName());
					if (!courseFilter.containsTargetLearningGoal(targetLearningGoalId)) {
						System.out.println("Adding Target LearningGoal:"
								+ targetLearningGoalId);
						courseFilter.addTargetActivity(targetLearningGoalId);
					}
				}
			}
		}
	}

	private void updateFiltersForUpdateHashtags(long actorId,
			String oldhashtags, String newhashtags, long resource, long userid) {
		HttpSession httpSession = applicationBean.getUserSession(actorId);
		if (httpSession != null) {
			LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
					.getAttribute("loggeduser");
			List<Tag> removedHashTags = new ArrayList<Tag>();
			List<Tag> addedHashtags = new ArrayList<Tag>();
			Set<Tag> oldHashTagsList = tagManager
					.getOrCreateTags(AnnotationUtil
							.getTrimmedSplitStrings(oldhashtags));
			Set<Tag> newHashTagsList = tagManager
					.getOrCreateTags(AnnotationUtil
							.getTrimmedSplitStrings(newhashtags));
			for (Tag oldHashtag : oldHashTagsList) {
				if (!newHashTagsList.contains(oldHashtag)) {
					removedHashTags.add(oldHashtag);
				}
			}
			for (Tag newHashtag : newHashTagsList) {
				if (!oldHashTagsList.contains(newHashtag)) {
					addedHashtags.add(newHashtag);
				}
			}
			if (loggedUserBean.getSelectedStatusWallFilter() instanceof TwitterFilter) {
				TwitterFilter filter = (TwitterFilter) loggedUserBean
						.getSelectedStatusWallFilter();
				for (Tag removed : removedHashTags) {
					filter.removeHashtag(removed);
				}
				for (Tag added : addedHashtags) {
					filter.addHashtag(added);
				}

			}
			LearningGoalFilter goalFilter = loggedUserBean
					.getSelectedLearningGoalFilter();
			if (goalFilter!=null && ((goalFilter.getGoalId() == resource)
					|| (goalFilter.getTargetGoalId() == resource))) {
				for (Tag removed : removedHashTags) {
					goalFilter.removeHashtag(removed);
				}
				for (Tag added : addedHashtags) {
					goalFilter.addHashtag(added);
				}
			}
		}
	}

	private void updateFiltersOnFollowUser(long actorId, long followedUser) {
		HttpSession httpSession = applicationBean.getUserSession(actorId);
		if (httpSession != null) {
			LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
					.getAttribute("loggeduser");
			if (loggedUserBean.getSelectedStatusWallFilter() instanceof MyNetworkFilter) {
				MyNetworkFilter myNetworkFilter = (MyNetworkFilter) loggedUserBean
						.getSelectedStatusWallFilter();
				System.out.println("SELECTED MY NETWORK FILTER: user:"
						+ loggedUserBean.getName() + " "
						+ loggedUserBean.getLastName());
				myNetworkFilter.addUserId(followedUser);

			}
		}
	}

	private void updateFiltersOnUnfollowUser(long actorId, long unfollowedUser) {
		HttpSession httpSession = applicationBean.getUserSession(actorId);
		if (httpSession != null) {
			LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
					.getAttribute("loggeduser");
			if (loggedUserBean.getSelectedStatusWallFilter() instanceof MyNetworkFilter) {
				MyNetworkFilter myNetworkFilter = (MyNetworkFilter) loggedUserBean
						.getSelectedStatusWallFilter();
				System.out.println("SELECTED MY NETWORK FILTER: user:"
						+ loggedUserBean.getName() + " "
						+ loggedUserBean.getLastName());
				myNetworkFilter.removeUserId(unfollowedUser);

			}

		}
	}

	private void updateFiltersOnDetachTargetActivityFromTargetCompetence(
			long targetCompetenceId, long targetActivityId, long actorId) {
		// Update Course filters for all logged users
		Map<Long, HttpSession> httpSessions = applicationBean
				.getAllHttpSessions();
		for (Long id : httpSessions.keySet()) {
			HttpSession httpSession = httpSessions.get(id);
			if (httpSession != null) {
				LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
						.getAttribute("loggeduser");
				if (loggedUserBean.getSelectedStatusWallFilter() instanceof CourseFilter) {
					CourseFilter courseFilter = (CourseFilter) loggedUserBean
							.getSelectedStatusWallFilter();
					System.out.println("SELECTED COURSE FILTER: user:" + id
							+ " " + loggedUserBean.getName() + " "
							+ loggedUserBean.getLastName());
					if (courseFilter.containsTargetActivity(targetActivityId)) {
						System.out.println("Remove Target activity:"
								+ targetActivityId);
						courseFilter.removeTargetActivity(targetActivityId);
					}
				}
			}
			// }
		}
		// Update LearningGoal filter for actor
		HttpSession httpSession = applicationBean.getUserSession(actorId);
		if (httpSession != null) {
			LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
					.getAttribute("loggeduser");

			System.out.println("SELECTED GOAL FILTER 1: user :"
					+ loggedUserBean.getName() + " "
					+ loggedUserBean.getLastName());
			LearningGoalFilter goalFilter = loggedUserBean
					.getSelectedLearningGoalFilter();
			if (goalFilter!=null && goalFilter.containsTargetActivity(targetActivityId)) {
				System.out
						.println("Remove Target activity:" + targetActivityId);
				goalFilter.removeTargetActivity(targetActivityId);
			}
		}

	}

	private void updateFiltersOnDetachTargetCompetenceFromTargetLearningGoal(
			long targetLearningGoalId, long targetCompetenceId, long actorId) {
		// Update Course filters for all logged users
		Map<Long, HttpSession> httpSessions = applicationBean
				.getAllHttpSessions();
		for (Long id : httpSessions.keySet()) {
			// if(objectVisibility.equals(VisibilityType.PUBLIC) ||
			// (id==actorId)){
			HttpSession httpSession = httpSessions.get(id);
			if (httpSession != null) {
				LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
						.getAttribute("loggeduser");
				if (loggedUserBean.getSelectedStatusWallFilter() instanceof CourseFilter) {
					CourseFilter courseFilter = (CourseFilter) loggedUserBean
							.getSelectedStatusWallFilter();
					System.out.println("SELECTED COURSE FILTER: user:" + id
							+ " " + loggedUserBean.getName() + " "
							+ loggedUserBean.getLastName());
					if (courseFilter
							.containsTargetCompetence(targetCompetenceId)) {
						System.out.println("Remove Target competence:"
								+ targetCompetenceId);
						courseFilter.removeTargetCompetence(targetCompetenceId);
					}
				}
			}
			// }
		}

		// Update LearningGoal filter for actor
		HttpSession httpSession = applicationBean.getUserSession(actorId);
		if (httpSession != null) {
			LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
					.getAttribute("loggeduser");
			System.out.println("TargetLearningGoal here");
			LearningGoalFilter goalFilter = loggedUserBean
					.getSelectedLearningGoalFilter();
			if (goalFilter.getTargetGoalId() == targetLearningGoalId) {
				System.out
						.println("Remove Target competence from target learning goal filter:"
								+ targetCompetenceId);
				goalFilter.removeTargetCompetence(targetCompetenceId);
			}
		}
	}

	public void updateFiltersOnAttachTargetCompetenceToTargetLearningGoal(
			long targetLearningGoalId, long targetCompetenceId, long actorId,
			VisibilityType objectVisibility) {
		// Update Course filters for all logged users
		Map<Long, HttpSession> httpSessions = applicationBean
				.getAllHttpSessions();
		for (Long id : httpSessions.keySet()) {
			if (objectVisibility.equals(VisibilityType.PUBLIC)
					|| (id == actorId)) {
				HttpSession httpSession = httpSessions.get(id);
				if (httpSession != null) {
					LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
							.getAttribute("loggeduser");
					if (loggedUserBean.getSelectedStatusWallFilter() instanceof CourseFilter) {
						CourseFilter courseFilter = (CourseFilter) loggedUserBean
								.getSelectedStatusWallFilter();
						System.out.println("SELECTED COURSE FILTER: user:" + id
								+ " " + loggedUserBean.getName() + " "
								+ loggedUserBean.getLastName());
						if (courseFilter
								.containsTargetLearningGoal(targetLearningGoalId)) {
							System.out.println("Added new Target competence:"
									+ targetCompetenceId);
							courseFilter
									.addTargetCompetence(targetCompetenceId);
						}
					}
				}
			}
		}

		// Update LearningGoal filter for actor
		HttpSession httpSession = applicationBean.getUserSession(actorId);
		if (httpSession != null) {
			LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
					.getAttribute("loggeduser");
			System.out.println("TargetLearningGoal Filter updating for user:"
					+ loggedUserBean.getName() + " "
					+ loggedUserBean.getLastName());
			LearningGoalFilter goalFilter = loggedUserBean
					.getSelectedLearningGoalFilter();
			if (goalFilter != null
					&& goalFilter.getTargetGoalId() == targetLearningGoalId) {
				System.out.println("Added new Target competence:"
						+ targetCompetenceId);
				goalFilter.addTargetCompetence(targetCompetenceId);
			}
		}
	}

	public void updateFiltersOnAttachTargetActivityToTargetCompetence(
			long targetCompetenceId, long targetActivityId, long actorId,
			VisibilityType objectVisibility) {
		// Update Course filters for all logged users
		Map<Long, HttpSession> httpSessions = applicationBean
				.getAllHttpSessions();
		for (Long id : httpSessions.keySet()) {
			if (objectVisibility.equals(VisibilityType.PUBLIC)
					|| (id == actorId)) {
				HttpSession httpSession = httpSessions.get(id);
				if (httpSession != null) {
					LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
							.getAttribute("loggeduser");
					if (loggedUserBean.getSelectedStatusWallFilter() instanceof CourseFilter) {
						CourseFilter courseFilter = (CourseFilter) loggedUserBean
								.getSelectedStatusWallFilter();
						if (courseFilter
								.containsTargetCompetence(targetCompetenceId)) {
									courseFilter.addTargetActivity(targetActivityId);
						}
					}
				}
			}
		}
		// Update LearningGoal filter for actor
		HttpSession httpSession = applicationBean.getUserSession(actorId);
		if (httpSession != null) {
			LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
					.getAttribute("loggeduser");

			System.out.println("SELECTED GOAL FILTER 2: user:"
					+ loggedUserBean.getName() + " "
					+ loggedUserBean.getLastName());
			LearningGoalFilter goalFilter = loggedUserBean
					.getSelectedLearningGoalFilter();
			if(goalFilter==null)System.out.println("GOAL FILTER IS NOT SELECTED");
			if (goalFilter!=null && goalFilter.containsTargetCompetence(targetCompetenceId)) {
				System.out.println("Added new Target Activity:"
						+ targetActivityId);
				goalFilter.addTargetActivity(targetActivityId);
			}
		}
	}

}
