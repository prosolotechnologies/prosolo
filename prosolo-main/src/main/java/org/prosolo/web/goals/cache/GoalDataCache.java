/**
 * 
 */
package org.prosolo.web.goals.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.course.Course;
import org.prosolo.domainmodel.course.CourseCompetence;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.workflow.evaluation.Badge;
import org.prosolo.domainmodel.workflow.evaluation.Evaluation;
import org.prosolo.recommendation.CollaboratorsRecommendation;
import org.prosolo.recommendation.CompetenceRecommendation;
import org.prosolo.recommendation.DocumentsRecommendation;
import org.prosolo.recommendation.impl.RecommendedDocument;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.BadgeManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.goals.RecommendedLearningPlansBean;
import org.prosolo.web.goals.competences.ActivitiesRecommendationBean;
import org.prosolo.web.goals.data.CompetenceData;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.search.SearchPeopleBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 * 
 */
@Service
@Scope("prototype")
public class GoalDataCache implements Serializable {

	private static final long serialVersionUID = 8250365559916623033L;
	private static Logger logger = Logger.getLogger(GoalDataCache.class);
	
	private LoggedUserBean loggedUser;
	
	private GoalData data;
	private boolean showGoalWall = true;

	// competences data
	private List<CompetenceDataCache> competences;
	
	// in Credential-based goals
	private List<CompetenceDataCache> predefinedCompetences;
	private boolean competencesInitialized;
	
	private CompetenceDataCache selectedCompetence;
	private Collection<CompetenceData> recommendedCompetences;

	// goal wall
//	private List<SocialActivityData1> goalWallActivities;
//	private List<SocialActivityData1> newActivities = new LinkedList<SocialActivityData1>();
//	private boolean moreToLoad;
//	private int goalWallLimit = Settings.getInstance().config.application.goals.goalWallPageSize;
//	private long userFilterId;
	
	// collaborators
	private List<UserData> collaborators;
	private List<UserData> recommendedCollaborators;
	
	// recommended documents
	public Collection<RecommendedDocument> documents;
	
	// evaluations
	private List<Evaluation> evaluations;
	
	// badges
	private List<Badge> badges;
	private boolean badgeCountInitialised = false;
	
	public GoalDataCache(){}
	
	public GoalDataCache(TargetLearningGoal targetGoal) {
		this.data = new GoalData(targetGoal);
	}
	
	public TargetLearningGoal updateGoalWithData(TargetLearningGoal targetGoal) {
		// TODO: goal
//		goal.setTitle(StringUtil.cleanHtml(data.getTitle()));
//		goal.setDescription(StringUtil.cleanHtml(data.getDescription()));
//		
//		if (data.getDeadline() != null)
//			goal.setDeadline(data.getDeadline());
		
		TagManager tagManager = ServiceLocator.getInstance().getService(TagManager.class);
		
		Set<Tag> newTagList = tagManager.parseCSVTagsAndSave(data.getTagsString());
		targetGoal.setTags(new HashSet<Tag>(newTagList));

		Set<Tag> newHashtagList = tagManager.parseCSVTagsAndSave(data.getHashtagsString());
		targetGoal.setHashtags(newHashtagList);
		
		//goal.setHashTags(hashtagsList);
		
		targetGoal.setProgress(data.getProgress());
		
		// TODO: goal
//		targetGoal.setFreeToJoin(data.isFreeToJoin());
		targetGoal.setProgressActivityDependent(data.isProgressActivityDependent());
		
		return targetGoal;
	}
	
	public void initCompetences() {
		try {
			logger.debug("Init competences");
			this.competences = new ArrayList<CompetenceDataCache>();
			this.predefinedCompetences = new ArrayList<CompetenceDataCache>();
			
			DefaultManager defaultManager = ServiceLocator.getInstance().getService(DefaultManager.class);
			
			TargetLearningGoal goal = defaultManager.loadResource(TargetLearningGoal.class, data.getTargetGoalId(), true);
			List<TargetCompetence> competences = new ArrayList<TargetCompetence>(goal.getTargetCompetences());
			
			CourseData courseData = data.getCourse();
			Course course = null;
			
			if (courseData != null) {
				course = defaultManager.loadResource(Course.class, courseData.getCourse().getId());
			}
			
			for (TargetCompetence tComp : competences) {
				CompetenceDataCache compDataCache = ServiceLocator.getInstance().getService(CompetenceDataCache.class);
				compDataCache.init(this, tComp);
				
				boolean predefined = false;
				
				if (course != null) {
					List<CourseCompetence> courseCompetnces = course.getCompetences();
					
					if (courseCompetnces != null) {
						cc: for (CourseCompetence courseCompetence : courseCompetnces) {
							if (courseCompetence.getCompetence().getId() == compDataCache.getData().getCompetenceId()) {
								compDataCache.getData().setShouldNotBeDeleted(ActionDisabledReason.DELETION_DISABLED_COMPETENCE_PREDEFINED_FOR_COURSE);
								predefined = true;
								break cc;
							}
						}
					}
				}
				
				if (predefined) {
					this.predefinedCompetences.add(compDataCache);
				} else {
					this.competences.add(compDataCache);
				}
			}
			sortCompetences();
			
			this.competencesInitialized = true;
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	/*
	 * GOAL WALL
	 */
//	public synchronized List<SocialActivityData1> getGoalWallActivities(long lastActivityToDisplayId) {
//		if (goalWallActivities != null) {
//			int index = getIndexOfSocialActivity(lastActivityToDisplayId);
//			
//			if (index == -1) {
//				int goalsToDisplay = goalWallActivities.size() > goalWallLimit ? goalWallLimit : goalWallActivities.size();
//				
//				return new ArrayList<SocialActivityData1>(goalWallActivities.subList(0, goalsToDisplay));
//			} else {
//				return new ArrayList<SocialActivityData1>(goalWallActivities.subList(0, index+1));
//			}
//		}
//		return null;
//	}
	
//	private int getIndexOfSocialActivity(long lastActivityToDisplayId) {
//		if (goalWallActivities != null) {
//			int index = 0;
//			
//			for (SocialActivityData1 goalWallData : goalWallActivities) {
//				if (goalWallData.getSocialActivity().getId() == lastActivityToDisplayId) {
//					return index;
//				}
//				index++;
//			}
//		}
//		return -1;
//	}
	
	/*
	 * END GOALL WALL
	 */
	
	public void selectCompetence(long targetCompId) {
		// force initialization of competences
		getCompetences();
		
		CompetenceDataCache compData = getCompetenceDataCache(targetCompId);
		
		if (compData != null) {
			setSelectedCompetence(compData);
			setShowGoalWall(false);
		}
	}
	
	public synchronized void addCompetence(TargetCompetence tComp) {
		if (competences != null) {
			CompetenceDataCache compDataCache = ServiceLocator.getInstance().getService(CompetenceDataCache.class);
			compDataCache.init(this, tComp);
			this.competences.add(compDataCache);
			
			sortCompetences();
		}
	}

	public synchronized boolean removeCompetence(long targetCompId) {
		Iterator<CompetenceDataCache> iterator = competences.iterator();
		
		while (iterator.hasNext()) {
			CompetenceDataCache compData = (CompetenceDataCache) iterator.next();
			
			if (compData.getData().getId() == targetCompId) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}
	
	public CompetenceDataCache getCompetenceDataCacheByCompId(long compId) {
		if (!competencesInitialized)
			initCompetences();
		
		for (CompetenceDataCache compData : competences) {
			if (compData.getData().getCompetenceId() == compId) {
				return compData;
			}
		}
		
		for (CompetenceDataCache compData : predefinedCompetences) {
			if (compData.getData().getCompetenceId() == compId) {
				return compData;
			}
		}
		return null;
	}
	
	public boolean containsCompetence(long compId) {
		return getCompetenceDataCacheByCompId(compId) != null;
	}
	
	public CompetenceDataCache getCompetenceDataCache(long targetCompId) {
		if (!competencesInitialized)
			initCompetences();

		for (CompetenceDataCache compData : competences) {
			if (compData.getData().getId() == targetCompId) {
				return compData;
			}
		}
		
		for (CompetenceDataCache compData : predefinedCompetences) {
			if (compData.getData().getId() == targetCompId) {
				return compData;
			}
		}
		return null;
	}

	/*
	 * COLLABORATORS
	 */
	public void initializeCollaborators() {
		if (collaborators == null) {
			logger.debug("initializeCollaborators");
			
			LearningGoalManager goalManager = ServiceLocator.getInstance().getService(LearningGoalManager.class);
			
			List<User> collab = goalManager.retrieveCollaborators(data.getGoalId(), loggedUser.getUser());
			List<UserData> collabData = new ArrayList<UserData>();
			
			for (User user : collab) {
				collabData.add(new UserData(user));
			}
			collaborators = collabData;
		}
	}
	
	public void initializeCollaboratorRecommendations(){
		if (recommendedCollaborators == null) {
			logger.debug("initializeCollaboratorsRecommendations for user:"+loggedUser.getUser().getId()+" targetGoal:"+data.getTargetGoalId());
			
			CollaboratorsRecommendation collaboratorsRecommendation = ServiceLocator.getInstance().getService(CollaboratorsRecommendation.class);
			SearchPeopleBean searchPeopleBean = ServiceLocator.getInstance().getService(SearchPeopleBean.class);
			
			List<User> collaborators = collaboratorsRecommendation
					.getRecommendedCollaboratorsForLearningGoal(
							loggedUser.getUser(), 
							data.getTargetGoalId(),
							Settings.getInstance().config.application.defaultLikeThisItemsNumber);
			if (collaborators.size() > 3) {
				this.recommendedCollaborators = searchPeopleBean.convertToUserData(collaborators.subList(0, 3));
			} else {
				this.recommendedCollaborators = searchPeopleBean.convertToUserData(collaborators);
			}
		}
	}
	
	/*
	 * END COLLABORATORS
	 */
	
	/*
	 * RECOMMENDED COMPETENCES
	 */
	public void initializeRecommendedCompetences() {
		logger.debug("initializeRecommendedCompetences");
		
		if (recommendedCompetences == null) {
			recommendedCompetences = new ArrayList<CompetenceData>();
			
			try {
				LearningGoalManager goalManager = ServiceLocator.getInstance().getService(LearningGoalManager.class);
				CompetenceRecommendation compRecommender = ServiceLocator.getInstance().getService(CompetenceRecommendation.class);
				
				TargetLearningGoal targetGoal = goalManager.loadResource(TargetLearningGoal.class, data.getTargetGoalId(),true);
				targetGoal=HibernateUtil.initializeAndUnproxy(targetGoal);
				final List<Competence> comps = compRecommender.recommendCompetences(
						loggedUser.refreshUser(), 
						targetGoal, 
						Settings.getInstance().config.application.defaultLikeThisItemsNumber);
				
				if (comps != null) {
					for (Competence competence : comps) {
						this.recommendedCompetences.add(new CompetenceData(competence));
					}
					
//					new Thread(
//						new Runnable() {
//							public void run() {
//								for (Competence comp : comps) {
//									competenceAnalytics.analyzeCompetence(comp.getId());
//								}
//							}
//						}
//					).run();
				}
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
	}
	
	public boolean getHasMoreRecommendedCompetences() {
		if (recommendedCompetences != null) {
			return recommendedCompetences.size() > Settings.getInstance().config.application.defaultSideBoxElementsNumber;
		} else {
			return false;
		}
	}

	public void removeRecommendedCompetence(Competence comp) {
		if (comp != null && recommendedCompetences != null) {
			Iterator<CompetenceData> iterator = recommendedCompetences.iterator();
			
			while (iterator.hasNext()) {
				CompetenceData competenceData = (CompetenceData) iterator.next();
				
				if (competenceData.getId() == comp.getId()) {
					iterator.remove();
					break;
				}
			}
		}
	}

	/*
	 * RECOMMENDED DOCUMENTS
	 */
	public void initRecommendedDocuments() {
		logger.debug("initRecommendedDocuments");
		
		if (documents == null) {
			try {
				logger.debug("Recommended documents was null. Initializing:user:" + loggedUser.getUser()+" goal:" + data.getGoalId());
				
				LearningGoalManager goalManager = ServiceLocator.getInstance().getService(LearningGoalManager.class);
				DocumentsRecommendation documentsRecommendation = ServiceLocator.getInstance().getService(DocumentsRecommendation.class);
				
				TargetLearningGoal targetGoal = goalManager.loadResource(TargetLearningGoal.class, data.getTargetGoalId());
					this.documents = documentsRecommendation.recommendDocuments(loggedUser.getUser(), goalManager.merge(targetGoal), 3);
				logger.debug("Recommended documents:"+this.documents.size());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
	}
	
	/*
	 * Evaluations
	 */
	public void initializeEvaluations() {
		if (evaluations == null) {
			fetchEvaluations();
			data.setEvaluationCount(evaluations.size());
		}
	}

	public void fetchEvaluations() {
		EvaluationManager evaluationManager = ServiceLocator.getInstance().getService(EvaluationManager.class);
		evaluations = evaluationManager.getApprovedEvaluationsForResource(TargetLearningGoal.class, data.getTargetGoalId());
	}
	
	public List<Long> getEvaluationIds() {
		if (evaluations != null) {
			List<Long> ids = new ArrayList<Long>();
			
			for (Evaluation ev : evaluations) {
				ids.add(ev.getId());
			}
			return ids;
		}
		return new ArrayList<Long>();
	}
	
	/*
	 * Badges
	 */
	public void initializeBadges() {
		if (!badgeCountInitialised) {
			BadgeManager badgeManager = ServiceLocator.getInstance().getService(BadgeManager.class);
			
			data.setBadgeCount(badgeManager.getBadgeCountForResource(TargetLearningGoal.class, data.getTargetGoalId()));
			badgeCountInitialised = true;
		}
	}
	
	/*
	 * UTILITY
	 */
	
	private void sortCompetences() {
		Collections.sort(this.competences);
		Collections.sort(this.predefinedCompetences);
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public GoalData getData() {
		return data;
	}

	public void setData(GoalData data) {
		this.data = data;
	}

	public List<CompetenceDataCache> getCompetences() {
		if (competences == null) {
			initCompetences();
		}
		return competences;
	}

	public void setCompetences(List<CompetenceDataCache> competences) {
		this.competences = competences;
	}
	
	public List<CompetenceDataCache> getPredefinedCompetences() {
		if (predefinedCompetences == null) {
			initCompetences();
		}
		return predefinedCompetences;
	}

	public CompetenceDataCache getSelectedCompetence() {
		return selectedCompetence;
	}

	public void setSelectedCompetence(CompetenceDataCache selectedCompetence) {
 
		this.selectedCompetence = selectedCompetence;
		
		if (this.selectedCompetence != null) {
//			this.selectedCompetence.setActivities(null);
			//this.selectedCompetence.initializeRecommendedLearningPlans();
			 
			
			//RecommendedLearningPlansBean recommendedLearningPlansBean = PageUtil.getSessionScopedBean("recommendedplans", RecommendedLearningPlansBean.class);
			
			//recommendedLearningPlansBean.init(selectedCompetence.getData().getId());
			
	//		RecommendedActivitiesBean recommendedActivitiesBean = PageUtil.getSessionScopedBean("recommendedactivitiesbean", RecommendedActivitiesBean.class);
		//	recommendedActivitiesBean.init(data.getTargetGoalId(),selectedCompetence.getData().getId());
//			ActivitiesRecommendationBean activitiesRecommendationBean=PageUtil.getSessionScopedBean("activitiesRecommendation", ActivitiesRecommendationBean.class);
//			activitiesRecommendationBean.setCompData(this.selectedCompetence);
//			activitiesRecommendationBean.initializeActivities();
	    	LoggingNavigationBean loggingNavigationBean = ServiceLocator.getInstance().getService(LoggingNavigationBean.class);
	    	
	    	Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", "learn.targetGoal." + data.getTargetGoalId());
			
			loggingNavigationBean.logEvent(
					EventType.SELECT_COMPETENCE, 
					TargetCompetence.class.getSimpleName(), 
					selectedCompetence.getData().getId(),
					parameters);
		}
	}

	public List<UserData> getCollaborators() {
		initializeCollaborators();
		return collaborators;
	}

	public void setCollaborators(List<UserData> collaborators) {
		this.collaborators = collaborators;
	}

	public List<UserData> getRecommendedCollaborators() {
		return recommendedCollaborators;
	}

	public void setRecommendedCollaborators(List<UserData> recommendedCollaborators) {
//		if (recommendedCollaborators == null) {
//			initializeCollaboratorRecommendations();
//		}
		this.recommendedCollaborators = recommendedCollaborators;
	}

	public Collection<CompetenceData> getRecommendedCompetences() {
//		if (recommendedCompetences == null) {
//			initializeRecommendedCompetences();
//		}
		return recommendedCompetences;
	}

	public Collection<RecommendedDocument> getDocuments() {
//		if (documents == null) {
//			initRecommendedDocuments();
//		}
		return documents;
	}

	public void setDocuments(Collection<RecommendedDocument> documents) {
		this.documents = documents;
	}

	public boolean isCompetencesInitialized() {
		return competencesInitialized;
	}

	public boolean isShowGoalWall() {
		return showGoalWall;
	}

	public void setShowGoalWall(boolean showGoalWall) {
		this.showGoalWall = showGoalWall;
	}

	public List<Evaluation> getEvaluations() {
		return evaluations;
	}
	
	public List<Badge> getBadges() {
		return badges;
	}

	public boolean isBadgeCountInitialised() {
		return badgeCountInitialised;
	}

	public void setLoggedUser(LoggedUserBean loggedUser) {
		this.loggedUser = loggedUser;
	}

}
