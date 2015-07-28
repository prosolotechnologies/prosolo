package org.prosolo.web.goals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityHandler;
import org.prosolo.services.activityWall.SocialStreamObserver;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.services.nodes.impl.PortfolioData;
import org.prosolo.services.twitter.TwitterStreamsManager;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.util.string.StringUtil;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.ActivityWallBean;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.courses.CoursePortfolioBean;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.goals.cache.LearningGoalPageDataCache;
import org.prosolo.web.goals.competences.ActivitiesRecommendationBean;
import org.prosolo.web.goals.competences.CompWallBean;
import org.prosolo.web.goals.competences.CompetenceStatusCache;
import org.prosolo.web.goals.data.CompetenceData;
import org.prosolo.web.goals.data.NewLearningGoalFormData;
import org.prosolo.web.home.LearningProgressBean;
import org.prosolo.web.home.RemindersBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.portfolio.PortfolioBean;
import org.prosolo.web.portfolio.data.AchievedCompetenceData;
import org.prosolo.web.portfolio.util.AchievedCompetenceDataConverter;
import org.prosolo.web.portfolio.util.CompletedGoalDataConverter;
import org.prosolo.web.useractions.VisibilityActionBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "learninggoals")
@Component("learninggoals")
@Scope("session")
public class LearningGoalsBean implements Serializable {

	private static final long serialVersionUID = 1782559979962114120L;

	private static Logger logger = Logger.getLogger(LearningGoalsBean.class);

	@Autowired private LearningGoalManager goalManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private PortfolioManager portfolioManager;
	@Autowired private VisibilityActionBean visibilityActionBean;
	@Autowired private CompletedGoalDataConverter completedGoalDataConverter;
	@Autowired private EventFactory eventFactory;
	@Autowired private TagManager tagManager;
  
	@Autowired private ApplicationBean applicationBean;
	@Autowired private CompetenceStatusCache competenceStatusCache;
	@Autowired private TwitterStreamsManager twitterStreamsManager;
	@Autowired private RemindersBean remindersBean;
	@Autowired private AchievedCompetenceDataConverter achievedCompetenceDataConverter;
	
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	@Autowired private SocialActivityHandler socialActivityHandler;
	@Autowired private ActivityWallBean activityWallBean;
	@Autowired private ActivitiesRecommendationBean activitiesRecommendationBean;
	
	private LearningGoalPageDataCache data;
	private GoalDataCache selectedGoalData;

	public boolean initialized;
	public NewLearningGoalFormData newLearningGoalFormData;
//	private List<Long> targetLearningGoalsIds=new ArrayList<Long>();
	
	
	/*
	 * Components
	 */

	public void initializeGoals() {
		if (!initialized) {
			List<TargetLearningGoal> userGoals = goalManager.getUserTargetGoals(loggedUser.getUser());
//			for(TargetLearningGoal tglg:userGoals){
//				targetLearningGoalsIds.add(tglg.getId());
//			}
			data = ServiceLocator.getInstance().getService(LearningGoalPageDataCache.class);
			data.setLoggedUser(loggedUser);
			selectedGoalData = data.init(loggedUser.getUser(), userGoals);
			initialized = true;
		}
	}

	/*
	 * ACTIONS
	 */

//	public List<Long> getTargetLearningGoalsIds() {
//		return targetLearningGoalsIds;
//	}
//
//	public void setTargetLearningGoalsIds(List<Long> targetLearningGoalsIds) {
//		this.targetLearningGoalsIds = targetLearningGoalsIds;
//	}

	public void selectGoal(GoalDataCache newSelectedGoalCache) {
		logger.info("select goal:"+newSelectedGoalCache.getData().getTargetGoalId());
		selectedGoalData = newSelectedGoalCache;
		loggedUser.loadGoalWallFilter(newSelectedGoalCache.getData().getTargetGoalId());
		
		if (selectedGoalData != null) {
			selectedGoalData.setSelectedCompetence(null);
			
			// reseting Goal Wall filter
			GoalWallBean goalWall = PageUtil.getViewScopedBean("goalwall", GoalWallBean.class);
			
			if (goalWall != null) {
				goalWall.removeUserFilter();
			}
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", "learn");
			
			loggingNavigationBean.logEvent(
					EventType.SELECT_GOAL, 
					TargetLearningGoal.class.getSimpleName(), 
					selectedGoalData.getData().getTargetGoalId(),
					parameters);
		}
	}

	public void changeNodeVisibility(GoalData goalData) {
		VisibilityType visType = VisibilityType.valueOf(PageUtil.getPostParameter("visType"));
		String context = PageUtil.getPostParameter("context");
		
		visibilityActionBean.changeVisibility(goalData.getTargetGoalId(), visType, context);
		goalData.setVisibility(visType);
		PageUtil.fireSuccessfulInfoMessage("Goal visibility updated");
	}

	public void createNewLearningGoal() {
		loggedUser.refreshUser();

		logger.debug("Creating new learning goal for the user "
				+ loggedUser.getUser());

		try {
			String keywords = newLearningGoalFormData.getKeywords();
			String hashtags = newLearningGoalFormData.getHashtags();
			
			TargetLearningGoal newTargetGoal = goalManager.createNewLearningGoal(
					loggedUser.getUser(), 
					StringUtil.cleanHtml(newLearningGoalFormData.getName()), 
					StringUtil.cleanHtml(newLearningGoalFormData.getDescription()), 
					newLearningGoalFormData.getDeadline(),
					tagManager.getOrCreateTags(AnnotationUtil.getTrimmedSplitStrings(keywords)),
					tagManager.getOrCreateTags(AnnotationUtil.getTrimmedSplitStrings(hashtags)),
					false);
			
			eventFactory.generateEvent(EventType.Create, loggedUser.getUser(), newTargetGoal);
			eventFactory.generateChangeProgressEvent(loggedUser.getUser(), newTargetGoal, 0);

			logger.debug("New learning goal (" + newTargetGoal.getTitle()	+ ") for the user " + loggedUser.getUser());
			PageUtil.fireSuccessfulInfoMessage("goalDetailsFormGrowl", "Learning goal " + newLearningGoalFormData.getName() + " is created!");

			selectedGoalData = data.addGoal(loggedUser.getUser(), newTargetGoal);
		} catch (EventException e) {
			logger.error("Error creating new Learnign Goal by the user " + loggedUser.getUser() + " " + e.getMessage());
			PageUtil.fireErrorMessage("goalDetailsFormGrowl",
					"There was an error creating learning goal " + newLearningGoalFormData.getName() + ".");
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e.getMessage());
		}

		resetNewGoalFormData();
	}

	public void saveGoalEdit() {
		long targetGoalId = selectedGoalData.getData().getTargetGoalId();
		
		logger.debug("Updating learning goal "
				+ targetGoalId + " by the user "
				+ loggedUser.getUser());
		
		try {
		
			TargetLearningGoal targetGoal = goalManager.loadResource(TargetLearningGoal.class, targetGoalId);
			final Collection<Tag> oldHashtags=targetGoal.getLearningGoal().getHashtags();
			 final LearningGoal updatedGoal = goalManager.updateLearningGoal(
					targetGoal.getLearningGoal(),
					selectedGoalData.getData().getTitle(),
					selectedGoalData.getData().getDescription(),
					selectedGoalData.getData().getTagsString(),
					selectedGoalData.getData().getHashtagsString(),
					selectedGoalData.getData().getDeadline(),
					selectedGoalData.getData().isFreeToJoin());
			
			final TargetLearningGoal updatedTargetGoal = goalManager.updateTargetLearningGoal(
					targetGoal,
					selectedGoalData.getData().isProgressActivityDependent(),
					selectedGoalData.getData().getProgress());

			// update goal data with keywords and hashtags
			selectedGoalData.setData(new GoalData(updatedTargetGoal));
			
			if (selectedGoalData.getData().getProgress() == 100) {
				markAsComplete(this.selectedGoalData.getData(), null);
			}
			
			PageUtil.fireSuccessfulInfoMessage("goalDetailsFormGrowl", "Learning goal updated!");
			logger.debug("Learning goal (" + updatedTargetGoal.getId()+ ") updated by the user " + loggedUser.getUser());
			
			
			// update progress
			recalculateSelectedGoalProgress();
		
			// create event, but SocialStreamObserver will be updated manually
			try {
				Map<String, String> parameters = new HashMap<String, String>();
				
				parameters.put("context", "learn");
				
				@SuppressWarnings("unchecked")
				Event event = eventFactory.generateEvent(
						EventType.Edit, 
						loggedUser.getUser(),
						updatedTargetGoal, 
						new Class[]{SocialStreamObserver.class},
						parameters);
				
			/*	ActivityWallUtilBean activityWallUtilBean = PageUtil.getSessionScopedBean("activitywallutil", ActivityWallUtilBean.class);
				
				if (activityWallUtilBean != null) {
					activityWallUtilBean.addSociaActivitySyncAndPropagate(
							event, 
							selectedGoalData, 
							loggedUser.getUser(), 
							true, 
							true);
				}*/
				//SocialActivityWallData wallData=
				socialActivityHandler.addSociaActivitySyncAndPropagateToStatusAndGoalWall(
						event);
				
					/*	socialActivityHandler.addSociaActivitySyncAndPropagateToGoalWall(
						event, 
						selectedGoalData, 
						loggedUser.getUser(), 
						loggedUser.getLocale());*/
				// add to status wall
			//	activityWallBean.addWallActivity(wallData);
			} catch (EventException e) {
				logger.error(e);
			}

			// update collaborators' caches
			asyncRefreshCollaboratosData(updatedTargetGoal);
			
			taskExecutor.execute(new Runnable() {
			    @Override
			    public void run() {
			    	Collection<Tag> newHashtags=selectedGoalData.getData().getHashtags();
			    	// update twitterStreamsManager if hashtags are updated
			    	eventFactory.generateUpdateHashtagsEvent(loggedUser.getUser(),oldHashtags,newHashtags,updatedGoal,null);
			    	/*twitterStreamsManager.updateHashTagsForResourceAndRestartStream(
			    			oldHashtags, 
			    			newHashtags, 
			    			updatedGoal.getId());*/
			    	

			    }
			});
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}

	public void asyncRefreshCollaboratosData(final TargetLearningGoal updatedTargetGoal) {
		final List<UserData> collaborators = selectedGoalData.getCollaborators();
		
		taskExecutor.execute(new Runnable() {
		    @Override
		    public void run() {
		    	if (collaborators != null) {
			    	for (UserData userData : collaborators) {
			    		HttpSession userSession = applicationBean.getUserSession(userData.getId());
			    		
			    		if (userSession != null) {
			    			LearningGoalsBean userLearningGoalBean = (LearningGoalsBean) userSession.getAttribute("learninggoals");						
						
			    			if (userLearningGoalBean != null) {
								GoalDataCache goalData = userLearningGoalBean.getData().getDataForGoal(updatedTargetGoal.getLearningGoal());								
								
								if (goalData != null) {
									logger.debug("Updating goal cache of user " + userData + ". Refreshing data of edited goal " + 
											updatedTargetGoal.getTitle() + " ("+updatedTargetGoal.getId()+").");									
									goalData.setData(new GoalData(updatedTargetGoal));
								}
							}
			    		}
					}
		    	}
		    }
		});
	}

	public void markAsComplete() {
		logger.debug("User " + loggedUser.getName() + ") marked as completed learning goal "
				+ selectedGoalData.getData().getGoalId());

		selectedGoalData.getData().setProgress(100);
		
		PageUtil.fireSuccessfulInfoMessage("goalDetailsFormGrowl",
				"Learning goal " + selectedGoalData.getData().getTitle() + " is completed!");
		
		TargetLearningGoal updatedGoal = markAsComplete(selectedGoalData.getData(), "learn");
		
		logger.debug("Learning goal (" + selectedGoalData.getData().getGoalId()
				+ ") successfuly marked as completed by the user " + loggedUser.getUser());
		
		asyncRefreshCollaboratosData(updatedGoal);
	}
	
	public void recalculateSelectedGoalProgress() {
		recalculateGoalProgress(getSelectedGoalData());
	}
	
	public void recalculateGoalProgress(GoalDataCache goalData) {
		if (goalData == null || loggedUser.getUser() == null) 
			return;
		
		if (goalData.getData().isProgressActivityDependent()) {
			double newProgress = 0;
			double numberOfComps = goalData.getCompetences().size();
			
			for (CompetenceDataCache compData : goalData.getCompetences()) {
				double compProgress = CompWallBean.calculateCompetenceProgress(compData);
				
				if (compProgress != 0) {
					newProgress += 1 / numberOfComps * (compProgress);
				}
			}
			
			int scaledProgress = (int) (newProgress * 100);
			
			if (scaledProgress != goalData.getData().getProgress()) {
				goalData.getData().setProgress(scaledProgress);
				
				try {
					goalManager.updateGoalProgress(goalData.getData().getTargetGoalId(), scaledProgress);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		}
		
		// update course progress
		if (goalData.getData().getCourse() != null) {
			CoursePortfolioBean coursePortfolioBean = (CoursePortfolioBean) applicationBean.getUserSession(loggedUser.getUser().getId()).getAttribute("coursePortfolioBean");
			
			CourseData activeCourse = coursePortfolioBean.getActiveCourse(goalData.getData().getTargetGoalId());
			
			if (activeCourse != null) {
				activeCourse.setProgress(goalData.getData().getProgress());
			}
		}
		
		if (goalData.getData().getProgress() == 100) {
			markAsComplete(goalData.getData(), null);
		}
		
		final PortfolioBean portfolioBean = (PortfolioBean) applicationBean.getUserSession(loggedUser.getUser().getId()).getAttribute("portfolio");
		portfolioBean.populateWithActiveCompletedGoals();
		portfolioBean.populateWithActiveCompletedCompetences();
	}

	private TargetLearningGoal markAsComplete(final GoalData goalData, String context) {
		
		// update CompetenceStatusCache
		for (CompetenceDataCache compData : selectedGoalData.getCompetences()) {
			compData.setCompleted(true);
			competenceStatusCache.addCompletedCompetence(compData.getData().getCompetenceId());
		}
		
		final GoalDataCache selectedGoalDataRef = selectedGoalData;
		
    	try {
    		final TargetLearningGoal targetGoal = goalManager.loadResource(TargetLearningGoal.class, selectedGoalData.getData().getTargetGoalId());
    		
    		TargetLearningGoal updatedGoal = goalManager.markAsCompleted(loggedUser.getUser(), targetGoal, context);
    	
    		// updating cache with the updated goal
    		updatedGoal = goalManager.merge(updatedGoal);
    		selectedGoalDataRef.setData(new GoalData(updatedGoal));
    	
    		
    		taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
        			remindersBean.resourceCompleted(goalData.getGoalId());

	            	// update portfolio cache if exists
	            	final PortfolioBean portfolioBean = (PortfolioBean) applicationBean.getUserSession(loggedUser.getUser().getId()).getAttribute("portfolio");
	            	
	            	if (portfolioBean != null) {
	            		portfolioBean.populateWithActiveCompletedGoals();
	            		portfolioBean.populateWithActiveCompletedCompetences();
	            		portfolioBean.initGoalStatistics();
	            	}
	            }
	        });
    		
    		return updatedGoal;
    	} catch (EventException e) {
    		logger.error(e);
    	} catch (ResourceCouldNotBeLoadedException e) {
    		logger.error(e);
		}
    	return null;
	}

	public void deleteSelectedGoal() {
		GoalDataCache goalData = selectedGoalData;
		
		deleteGoal(goalData, "learn");
		
		PageUtil.fireSuccessfulInfoMessage("goalDetailsFormGrowl", "Learning goal '" + goalData.getData().getTitle() + "' is removed from your goals!");
	}
	
	public void deleteGoal(GoalDataCache goalData, String context) {
		final List<UserData> collaborators = goalData.getCollaborators();
		boolean hasMoreCollaborators = collaborators.size() > 0;
		
		try {
			final TargetLearningGoal targetGoal = goalManager.loadResource(TargetLearningGoal.class, goalData.getData().getTargetGoalId());
						
			goalManager.deleteGoal(targetGoal);
		
			LearningGoal goal = targetGoal.getLearningGoal();

			// if this was the last collaborator, then mark the goal as deleted
			if (!hasMoreCollaborators) {
				goalManager.markAsDeleted(goal);
			}
			
			selectedGoalData = data.removeGoal(goalData);
			
			// recalculate goal progress
			PortfolioBean portfolio = PageUtil.getSessionScopedBean("portfolio", PortfolioBean.class);
			
			if (portfolio != null) {
				portfolio.initGoalStatistics();
			}
			
			Map<String, String> parameters = new HashMap<String, String>();
			
			if (context != null)
				parameters.put("context", context);

			eventFactory.generateEvent(EventType.Detach, loggedUser.getUser(), targetGoal, parameters);
			
			// user's Profile cache and collaborators' data will be updated by the InterfaceCacheUpdater
		} catch (EventException e) {
			logger.error(e.getMessage());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e.getMessage());
		}
		
		//logger.debug("User " + loggedUser.getName() + " deleted learning goal "	+ selectedGoalData.getData().getId());
	}
	
	public void archiveGoal(boolean removeFromGoals) {
		GoalData goalData = selectedGoalData.getData();
		archiveGoal(goalData, removeFromGoals);
	}
	
	public void archiveGoal(GoalData goalData, boolean removeFromGoals) {
		try {
			final PortfolioData updatedPortfolioData = portfolioManager.sendGoalToPortfolio(goalData.getTargetGoalId(), loggedUser.refreshUser());
			
			if (!updatedPortfolioData.isEmpty()) {
				logger.debug("Goal " + goalData.getGoalId() + " is sent to portfolio of a user " + loggedUser.getUser());
				PageUtil.fireSuccessfulInfoMessage("goalDetailsFormGrowl", "Goal '" + goalData.getTitle() + "' is sent to your Profile!");
			
			
				// if connected with a course, update Course Portfolio 
	    		CourseData courseData = goalData.getCourse();
				if (courseData != null) {
					HttpSession session = applicationBean.getUserSession(loggedUser.getUser().getId());
					
					if (session != null) {
						CoursePortfolioBean coursePortfolioBean = (CoursePortfolioBean) session.getAttribute("coursePortfolioBean");
		    			
						if (coursePortfolioBean != null) {
		    				coursePortfolioBean.completeCourse(courseData);
		    			}
					}
	    		}
				
				if (removeFromGoals)
					selectedGoalData = data.removeGoal(selectedGoalData);

				final LearningProgressBean learningProgressBean = PageUtil.getSessionScopedBean("learningProgress", LearningProgressBean.class);
				
				if (learningProgressBean != null) {
					learningProgressBean.initializeData();
				}
				
				taskExecutor.execute(new Runnable() {
		            @Override
		            public void run() {
		            	// update Portfolio cache if exists
		            	final PortfolioBean portfolioBean = (PortfolioBean) applicationBean.getUserSession(loggedUser.getUser().getId()).getAttribute("portfolio");
		            	
		            	if (portfolioBean != null) {
		            		portfolioBean.getCompletedArchivedGoals().add(0, completedGoalDataConverter.convertCompletedGoal(updatedPortfolioData.getCompletedGoals().get(0)));
		            		
		            		outer: for (AchievedCompetence achievedComp : updatedPortfolioData.getAchievedCompetences()) {
		            			if (achievedComp != null && portfolioBean.getCompletedAchievedComps() != null) {
			            			for (AchievedCompetenceData achCompData : portfolioBean.getCompletedAchievedComps()) {
			            				if (achCompData.getCompetence().equals(achievedComp)) {
			            					continue outer;
			            				}
			            			}
			            			portfolioBean.getCompletedAchievedComps().add(0, achievedCompetenceDataConverter.convertAchievedCompetence(achievedComp));
		            			}
		            		}
		            		portfolioBean.populateWithActiveCompletedGoals();
		            		portfolioBean.initGoalStatistics();
		            	}
		            }
		        });
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void showCompetenceActivities(CompetenceDataCache compData) {
		selectedGoalData.setSelectedCompetence(compData);
	}

	public void resetNewGoalFormData() {
		this.newLearningGoalFormData = new NewLearningGoalFormData();
	}

	public void initializeCollaborators() {
		if (selectedGoalData != null) {
			selectedGoalData.initializeCollaborators();
		}
	}
	
	public void initializeCollaboratorRecommendations() {
		if (selectedGoalData != null) {
			selectedGoalData.initializeCollaboratorRecommendations();
		}
	}
	
	public void initializeRecommendedPlans() {
		if (selectedGoalData != null && selectedGoalData.getSelectedCompetence() != null) {
			selectedGoalData.getSelectedCompetence().initializeRecommendedLearningPlans();
		}
	}
	
	public void initializeRecommendedCompetences() {
		if (selectedGoalData != null) {
			selectedGoalData.initializeRecommendedCompetences();
		}
	}
	
	public void initRecommendedDocuments() {
		if (selectedGoalData != null) {
			selectedGoalData.initRecommendedDocuments();
		}
	}
	
	public void refreshGoal(LearningGoal goal) {
		data.refreshGoalCollaborators(goal);
	}
	
	public boolean hasMoreRecommendedCompetences() {
		return selectedGoalData!= null && selectedGoalData.getHasMoreRecommendedCompetences();
	}
	
	/**
	 * Used for competence search
	 * @return 
	 */
	public ArrayList<Long> getAllCompetenceIds() {
		if (selectedGoalData != null) {
			ArrayList<Long> ids = new ArrayList<Long>();
				
			if (selectedGoalData.getCompetences() != null) {
				for (CompetenceDataCache comp : selectedGoalData.getCompetences()) {
					ids.add(comp.getData().getCompetenceId());
				}
			}
			return ids;
		}
		return null;
	}
	
	/*
	 * Used for competence comparison
	 */
	public void setUserForCompComparison(User user, long id) {
		
	}
	
	/*
	 * PARAMETERS
	 */
	
	private long evaluationId;
	
	public long getEvaluationId() {
		return evaluationId;
	}
	
	public void setEvaluationId(long evaluationId) {
		this.evaluationId = evaluationId;
	}
	
	/*
	 * GETTERS/SETTERS
	 */
	
	public List<GoalDataCache> getGoals() {
		initializeGoals();
		return data.getGoals();
	}
	
	public NewLearningGoalFormData getNewLearningGoalFormData() {
		return newLearningGoalFormData;
	}

	public void setNewLearningGoalFormData(NewLearningGoalFormData newLearningGoalFormData) {
		this.newLearningGoalFormData = newLearningGoalFormData;
	}

	public int getRefreshRate() {
		return Settings.getInstance().config.application.defaultRefreshRate;
	}

	public GoalDataCache getSelectedGoalData() {
		return selectedGoalData;
	}
	
	public void setSelectedGoalData(GoalDataCache selectedGoalData) {
		this.selectedGoalData = selectedGoalData;
	}

	public void selectCompetence(CompetenceDataCache competenceDataCache) {
 
		if (this.selectedGoalData != null) {
			this.selectedGoalData.setSelectedCompetence(competenceDataCache);
			//ActivitiesRecommendationBean activitiesRecommendationBean=PageUtil.getSessionScopedBean("activitiesRecommendation", ActivitiesRecommendationBean.class);
			activitiesRecommendationBean.setCompData(competenceDataCache);
			activitiesRecommendationBean.initializeActivities();
		}
	}
	
	public LearningGoalPageDataCache getData() {
		return data;
	}
	
	public boolean isShowGoalWall() {
		return selectedGoalData != null && selectedGoalData.isShowGoalWall();
	}

	public List<UserData> getRecommendedCollaborators() {
		if (selectedGoalData != null) {
			return selectedGoalData.getRecommendedCollaborators();
		}
		return null;
	}
	
	public List<UserData> getCollaborators() {
		if (selectedGoalData != null) {
			return selectedGoalData.getCollaborators();
		}
		return null;
	}
	
	public Collection<CompetenceData> getRecommendedCompetences() {
		if (selectedGoalData != null) {
			return selectedGoalData.getRecommendedCompetences();
		}
		return null;
	}
	
	public List<GoalData> getGoalsData() {
		initializeGoals();
		
		List<GoalData> goalsData = new ArrayList<GoalData>();
		
		for (GoalDataCache goalDataCache : data.getGoals()) {
			goalsData.add(goalDataCache.getData());
		}
		
		return goalsData;
	}
	
	public List<GoalData> getCompletedGoals() {
		initializeGoals();
		
		List<GoalData> ongoingGoals = new ArrayList<GoalData>();
		
		for (GoalDataCache goalDataCache : data.getGoals()) {
			if (goalDataCache.getData().getProgress() == 100) {
				ongoingGoals.add(goalDataCache.getData());
			}
		}
		return ongoingGoals;
	}
	
	public List<AchievedCompetenceData> getCompletedCompetences() {
		initializeGoals();
		
		List<AchievedCompetenceData> ongoingCompletedCompetences = new ArrayList<AchievedCompetenceData>();
		
		for (GoalDataCache goalDataCache : data.getGoals()) {
			for (CompetenceDataCache compData : goalDataCache.getCompetences()) {
				if (compData.isCompleted()) {
					ongoingCompletedCompetences.add(AchievedCompetenceDataConverter.convertCompetence(compData));
				}
			}
		}
		return ongoingCompletedCompetences;
	}
	
	public List<GoalData> getOngoingGoals() {
		initializeGoals();
		
		List<GoalData> ongoingGoals = new ArrayList<GoalData>();
		
		for (GoalDataCache goalDataCache : data.getGoals()) {
			if (goalDataCache.getData().getProgress() != 100) {
				ongoingGoals.add(goalDataCache.getData());
			}
		}
		return ongoingGoals;
	}
	
	public List<CompetenceDataCache> getOngoingCompetences() {
		List<CompetenceDataCache> ongoingCompetences = new ArrayList<CompetenceDataCache>();
		initializeGoals();
		
		for (GoalDataCache goalDataCache : data.getGoals()) {
			for (CompetenceDataCache compData : goalDataCache.getCompetences()) {
				if (!compData.isCompleted()) {
					ongoingCompetences.add(compData);
				}
			}
		}
		return ongoingCompetences;
	}
	
	public List<AchievedCompetenceData> getOngoingCompetencesAsAchievedCompetenceData() {
		List<AchievedCompetenceData> ongoingCompetences = new ArrayList<AchievedCompetenceData>();
		
		for (CompetenceDataCache ongoingComp : getOngoingCompetences()) {
			ongoingCompetences.add(AchievedCompetenceDataConverter.convertCompetence(ongoingComp));
		}
		
		return ongoingCompetences;
	}
	
	public boolean isCompleted() {
		if (selectedGoalData != null) {
			return selectedGoalData.getData().isCompleted();
		} else {
			return false;
		}
	}

}
