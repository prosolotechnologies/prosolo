/**
 * 
 */
package org.prosolo.web.goals.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.activitywall.comments.Comment;
import org.prosolo.domainmodel.outcomes.Outcome;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.nodes.BadgeManager;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.SocialActivityCommentData;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.home.SuggestedLearningBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 * 
 */
@Service("org.prosolo.web.goals.cache.LearningGoalPageDataCache")
@Scope("prototype")
public class LearningGoalPageDataCache implements Serializable {

	private static final long serialVersionUID = -8090625502703594734L;
	
	private static Logger logger = Logger.getLogger(LearningGoalPageDataCache.class);
	
	private LoggedUserBean loggedUser;
	
	@Autowired private SuggestedLearningBean suggestedLearningBean;
	@Autowired private EvaluationManager evaluationManager;
	@Autowired private BadgeManager badgeManager;
	
	private List<GoalDataCache> goals = new ArrayList<GoalDataCache>();
	
	public GoalDataCache init(User user, List<TargetLearningGoal> targetGoals) {
		logger.debug("Init goal page data");
		
		if (targetGoals != null && !targetGoals.isEmpty()) {
			// get last action on each goal or its competence or activity
			
			for (TargetLearningGoal targetGoal : targetGoals) {
				addGoal(user, targetGoal);
			}
			
			return this.goals.get(0);
		}
		return null;
	}
	

	public GoalDataCache addGoal(User user, TargetLearningGoal targetGoal, Session session) {
		if (!containsDataForGoal(targetGoal.getLearningGoal())) {	
			Date lastActivity = ServiceLocator.getInstance().getService(ActivityWallManager.class).getLastActivityForGoal(user, targetGoal, session);
			
			GoalData goalData = new GoalData(targetGoal);
			goalData.setLastActivity(lastActivity);
			goalData.setEvaluationCount(evaluationManager.getApprovedEvaluationCountForResource(TargetLearningGoal.class, targetGoal.getId(), session));
			goalData.setRejectedEvaluationCount(evaluationManager.getRejectedEvaluationCountForResource(TargetLearningGoal.class, targetGoal.getId(), session));
			goalData.setBadgeCount(badgeManager.getBadgeCountForResource(TargetLearningGoal.class, targetGoal.getId(), session));
			
			GoalDataCache newGoalDataCache = ServiceLocator.getInstance().getService(GoalDataCache.class);
			newGoalDataCache.setLoggedUser(loggedUser);
			newGoalDataCache.setData(goalData);
			
			goals.add(newGoalDataCache);
			
			return newGoalDataCache;
		}
		return getDataForGoal(targetGoal.getLearningGoal());
	}
	
	public GoalDataCache addGoal(User user, TargetLearningGoal targetGoal) {
		return addGoal(
				user, 
				targetGoal, 
				(Session) evaluationManager.getPersistence().currentManager());
	}
	
	public GoalDataCache removeGoal(GoalDataCache goalData) {
		Iterator<GoalDataCache> iterator = goals.iterator();
		
		while (iterator.hasNext()) {
			GoalDataCache g = (GoalDataCache) iterator.next();
			
			if (g.getData().getGoalId() == goalData.getData().getGoalId()) {
				iterator.remove();
				break;
			}
		}
		
		if (!goals.isEmpty()) {
			return goals.get(0);
		} else {
			return null;
		}
	}
	
	public GoalDataCache removeGoalByTargetId(long targetGoalId) {
		Iterator<GoalDataCache> iterator = goals.iterator();
		
		while (iterator.hasNext()) {
			GoalDataCache g = (GoalDataCache) iterator.next();
			
			if (g.getData().getTargetGoalId() == targetGoalId) {
				iterator.remove();
				break;
			}
		}
		
		if (!goals.isEmpty()) {
			return goals.get(0);
		} else {
			return null;
		}
	}
	
	public boolean containsDataForGoal(LearningGoal goal) {
		if (!goals.isEmpty()) {
			for (GoalDataCache goalData : goals) {
				if (goalData.getData().getGoalId() == goal.getId()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public GoalDataCache getDataForGoal(LearningGoal goal) {
		return getDataForGoal(goal.getId());
	}
	
	public GoalDataCache getDataForGoal(long goalId) {
		if (!goals.isEmpty()) {
			for (GoalDataCache goalData : goals) {
				if (goalData.getData().getGoalId() == goalId) {
					return goalData;
				}
			}
		}
		return null;
	}
	
	public GoalDataCache getDataForTargetGoal(long targetGoalId) {
		if (!goals.isEmpty()) {
			for (GoalDataCache goalData : goals) {
				if (goalData.getData().getTargetGoalId() == targetGoalId) {
					return goalData;
				}
			}
		}
		return null;
	}
	
	public CompetenceDataCache getDataForTargetCompetence(long tCompId) {
		if (!goals.isEmpty()) {
			for (GoalDataCache goalData : goals) {
				
				if (goalData.getCompetences() != null) {
					for (CompetenceDataCache compData : goalData.getCompetences()) {
						if (compData.getData().getId() == tCompId) {
							return compData;
						}
					}
				}
			}
		}
		return null;
	}
	
	public void updateGoalData(GoalData goalData) {
		for (GoalDataCache goalCache : goals) {
			if (goalCache.getData().getGoalId() == goalData.getGoalId()) {
				System.out.println("update goal data");
				goalCache.setData(goalData);
				break;
			}
		}
	}

	public void removeWallActivityData(long socialActivityToRemoveId) {
//		for (GoalDataCache goalData : goals) {
//			 TODO: Nikola
//			if (goalData.getGoalWallActivities() != null) {
//				Iterator<SocialActivityData1> actIterator = goalData.getGoalWallActivities().iterator();
//				
//				while (actIterator.hasNext()) {
//					SocialActivityData1 socialActivityWallData = (SocialActivityData1) actIterator.next();
//					
//					if (socialActivityWallData.getSocialActivity().getId() == socialActivityToRemoveId) {
//						actIterator.remove();
//					}
//				}
//			}
//		}
	}
	
	public void updateSocialActivity(SocialActivity socialActivity) {
//		outer: for (GoalDataCache goalData : goals) {
//			// TODO: Nikola
//			if (goalData.getGoalWallActivities() != null) {
//				Iterator<SocialActivityData1> actIterator = goalData.getGoalWallActivities().iterator();
//				
//				while (actIterator.hasNext()) {
//					SocialActivityData1 socialActivityWallData = (SocialActivityData1) actIterator.next();
//					
//					if (socialActivityWallData.getSocialActivity().getId() == socialActivity.getId()) {
//						socialActivityWallData.setCommentsDisabled(socialActivity.isCommentsDisabled());
//						socialActivityWallData.setLikeCount(socialActivity.getLikeCount());
//						socialActivityWallData.setDislikeCount(socialActivity.getDislikeCount());
//						socialActivityWallData.setShareCount(socialActivity.getShareCount());
//						break outer;
//					}
//				}
//			}
//		}
	}
	
	public void disableSharing(SocialActivity socialActivity) {
//		outer: for (GoalDataCache goalData : goals) {
//			// TODO: Nikola
//			if (goalData.getGoalWallActivities() != null) {
//				Iterator<SocialActivityData1> actIterator = goalData.getGoalWallActivities().iterator();
//				
//				while (actIterator.hasNext()) {
//					SocialActivityData1 socialActivityWallData = (SocialActivityData1) actIterator.next();
//					
//					if (socialActivityWallData.getSocialActivity().getId() == socialActivity.getId()) {
//						socialActivityWallData.setShared(true);
//						break outer;
//					}
//				}
//			}
//		}
	}
	
	public void addCommentToSocialActivity(long socialActivityId, Comment comment) {
//		outer: for (GoalDataCache goalData : goals) {
			// TODO: Nikola
//			if (goalData.getGoalWallActivities() != null) {
//				Iterator<SocialActivityData1> actIterator = goalData.getGoalWallActivities().iterator();
//				
//				while (actIterator.hasNext()) {
//					SocialActivityData1 socialActivityWallData = (SocialActivityData1) actIterator.next();
//					
//					if (socialActivityWallData.getSocialActivity().getId() == socialActivityId) {
//						// TODO: Nikola
////						ActivityCommentData commentData = new ActivityCommentData(comment, 0, false, socialActivityWallData);
////						socialActivityWallData.addComment(commentData);
////						break outer;
//					}
//				}
//			}
//		}
	}
	
	public void updateCommentDataOfSocialActivity(long socialActivityId, Comment comment, int likeCount, int dislikeCount) {
		// TODO: Nikola
//		outer: for (GoalDataCache goalData : goals) {
//			if (goalData.getGoalWallActivities() != null) {
//				Iterator<SocialActivityData1> actIterator = goalData.getGoalWallActivities().iterator();
//				
//				while (actIterator.hasNext()) {
//					SocialActivityData1 socialActivityWallData = (SocialActivityData1) actIterator.next();
//					
//					if (socialActivityWallData.getSocialActivity().getId() == socialActivityId) {
//						List<ActivityCommentData> comments = socialActivityWallData.getComments();
//						
//						for (ActivityCommentData activityCommentData : comments) {
//							if (activityCommentData.getId() == comment.getId()) {
//								activityCommentData.setLikeCount(likeCount);
//								activityCommentData.setDislikeCount(dislikeCount);
//								break outer;
//							}
//						}
//					}
//				}
//			}
//		}
	}

	public void refreshGoalCollaborators(LearningGoal goal) {
		GoalDataCache goalData = getDataForGoal(goal);
		
		if (goalData != null) {
			goalData.setCollaborators(null);
			goalData.initializeCollaborators();
		}
	}
	
	public void updateAllActivityData(ActivityWallData actData) {
		if (goals != null) {
			for (GoalDataCache goalCacheData : goals) {
				
				if (goalCacheData.isCompetencesInitialized()) {
					for (CompetenceDataCache compDataCache : goalCacheData.getCompetences()) {
						
						if (compDataCache.getActivities() != null) {
							for (ActivityWallData actData1 : compDataCache.getActivities()) {
								
								if (actData1.getActivity().getId() == actData.getActivity().getId()) {
									actData1.setLikeCount(actData.getLikeCount());
									actData1.setDislikeCount(actData.getDislikeCount());
									actData1.setShareCount(actData.getShareCount());
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void addCommentToActivity(long activityId, Comment comment) {
		if (goals != null) {
			for (GoalDataCache goalCacheData : goals) {
				
				if (goalCacheData.isCompetencesInitialized()) {
					for (CompetenceDataCache compDataCache : goalCacheData.getCompetences()) {
						
						if (compDataCache.getActivities() != null) {
							for (ActivityWallData actData1 : compDataCache.getActivities()) {
								
								if (actData1.getActivity().getId() == activityId) {
									SocialActivityCommentData commentData = new SocialActivityCommentData(comment, 0, false, actData1);
									actData1.addComment(commentData);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void updateCommentDataOfActivity(long activityId, Comment comment, int likeCount, int dislikeCount) {
		if (goals != null) {
			for (GoalDataCache goalCacheData : goals) {
				
				if (goalCacheData.isCompetencesInitialized()) {
					compLoop: for (CompetenceDataCache compDataCache : goalCacheData.getCompetences()) {
						
						if (compDataCache.getActivities() != null) {
							for (ActivityWallData actData1 : compDataCache.getActivities()) {
								
								if (actData1.getActivity().getId() == activityId) {
									List<SocialActivityCommentData> comments = actData1.getComments();
									
									for (SocialActivityCommentData activityCommentData : comments) {
										if (activityCommentData.getId() == comment.getId()) {
											activityCommentData.setLikeCount(likeCount);
											activityCommentData.setDislikeCount(dislikeCount);
											continue compLoop;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public boolean updateActivityOutcome(long targetActivityId, Outcome outcome) {
		if (goals != null) {
			for (GoalDataCache goalCacheData : goals) {
				
				if (goalCacheData.isCompetencesInitialized()) {
					for (CompetenceDataCache compDataCache : goalCacheData.getCompetences()) {
						
						if (compDataCache.getActivities() != null) {
							boolean updated = compDataCache.updateActivityOutcome(targetActivityId, outcome);
							
							if (updated) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public List<GoalDataCache> getGoals() {
		return goals;
	}
	
	public void setGoals(List<GoalDataCache> goals) {
		this.goals = goals;
	}


	public void setLoggedUser(LoggedUserBean loggedUser) {
		this.loggedUser = loggedUser;
	}
	
}
