package org.prosolo.web.goals.competences;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.activityWall.SocialActivityHandler;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.nodes.impl.LearningGoalManagerImpl.GoalTargetCompetenceAnon;
import org.prosolo.services.nodes.impl.LearningGoalManagerImpl.NodeEvent;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.ActivityWallBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.dialogs.data.CompetenceFormData;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.goals.RecommendedLearningPlansBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.portfolio.PortfolioBean;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name="competencesBean")
@Component("competencesBean")
@Scope("session")
public class CompetencesBean implements Serializable {
	
	private static final long serialVersionUID = 9028419867146414938L;

	protected static Logger logger = Logger.getLogger(CompetencesBean.class);
	
	@Autowired private CompetenceManager compManager;
	@Autowired private LearningGoalManager goalManager;
	@Autowired private EventFactory eventFactory;
	@Autowired private TagManager tagManager;
	@Autowired private CourseManager courseManager;
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private RecommendedLearningPlansBean chooseCompetencePlanBean;
	@Autowired private LearningGoalsBean goalBean;
	@Autowired private CompetenceStatusCache competenceStatusCache;
	//@Autowired private ActivityWallUtilBean activityWallUtilBean;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	@Autowired private ApplicationBean applicationBean;
	@Autowired private SocialActivityHandler socialActivityHandler;
	@Autowired private ActivityWallBean activityWallBean;
	
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private CompetenceDataCache selectedComp;
	private CompetenceFormData formData = new CompetenceFormData();
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	// OPERATIONS
	
	// Creating new competence from the Competence Dialog
	public void saveCompetence(){
		logger.debug("Creating new Competence with the name \""+formData.getTitle()+"\" for the user "+ 
				loggedUser.getUser());
	
		// put user into session
		loggedUser.refreshUser();
		
		String context = PageUtil.getPostParameter("context");
		
		try {
			long targetGoalId = goalBean.getSelectedGoalData().getData().getTargetGoalId();
			
			NodeEvent nodeEvent = goalManager.createCompetenceAndAddToGoal(
					loggedUser.getUser(),
					StringUtil.cleanHtml(formData.getTitle()),
					StringUtil.cleanHtml(formData.getDescription()),
					formData.getValidity(),
					formData.getDuration(),
					goalBean.getSelectedGoalData().getData().getVisibility(),
					new HashSet<Tag>(tagManager.parseCSVTagsAndSave(formData.getTagsString())),
					targetGoalId,
					true,
					context+".targetGoal."+targetGoalId);
			
			TargetCompetence newCompetence = (TargetCompetence) nodeEvent.getNode();
			
			addTargetCompetence(newCompetence, goalBean.getSelectedGoalData(), nodeEvent.getEvent());
			
			logger.debug("New Competence \""+newCompetence.getTitle()+"\" ("+newCompetence.getId()+
					") created for the user "+ loggedUser.getUser());
			
			PageUtil.fireSuccessfulInfoMessage("newCompForm:newCompFormGrowl", "Competence '"+formData.getTitle()+"' is created!");
			
			goalBean.recalculateGoalProgress(goalBean.getSelectedGoalData());
			
			formData = new CompetenceFormData();
		} catch (EventException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("newCompForm:newCompFormGrowl", "Competence '"+formData.getTitle()+"' could not be created.");
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("newCompForm:newCompFormGrowl", "Competence '"+formData.getTitle()+"' could not be created.");
		}
	}
	
	public void resetFormData() {
 		this.formData = new CompetenceFormData();
	}
	
	public void connectCompetenceById(long competenceToConnectId, String context){
		try {
			Competence competenceToConnect = goalManager.loadResource(Competence.class, competenceToConnectId);
			connectCompetence(competenceToConnect, context);
			
			// mark this competence as added
			CompetenceComparisonBean competenceComparison = PageUtil.getViewScopedBean("competenceComparisonBean", CompetenceComparisonBean.class);
		
			if (competenceComparison != null) {
				competenceComparison.markCompetenceAsAdded(competenceToConnectId);
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void connectCompetence(Competence competenceToConnect, String context){
		connectCompetenceAndUpdateCourse(competenceToConnect, goalBean.getSelectedGoalData(), context);
	}
	
	public void connectCompetence(Competence competenceToConnect){
		String context = PageUtil.getPostParameter("context");
		
		connectCompetenceAndUpdateCourse(competenceToConnect, goalBean.getSelectedGoalData(), context);
	}

	public void connectCompetenceAndUpdateCourse(Competence competenceToConnect, GoalDataCache goalData, String context){
		connectCompetence(competenceToConnect, goalData, context);
		
		// updating course enrollment
		if (goalData.getData().isConnectedWithCourse()) {
			try {
				courseManager.addCompetenceToEnrollment(goalData.getData().getCourse().getEnrollmentId(), competenceToConnect.getId());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
	}
	
	public void connectCompetence(Competence competenceToConnect, GoalDataCache goalData, String context){
		try {
			NodeEvent nodeEvent = goalManager.addCompetenceToGoal(
					loggedUser.getUser(), 
					goalData.getData().getTargetGoalId(), 
					competenceToConnect, 
					true,
					context);

			TargetCompetence newCompetence = (TargetCompetence) nodeEvent.getNode();
			
			addTargetCompetence(newCompetence, goalData, nodeEvent.getEvent());
			
			goalBean.recalculateGoalProgress(goalData);
			
			logger.debug("Competence \""+competenceToConnect.getTitle()+
					"\" ("+competenceToConnect.getId()+") connected to the Learning Goal \""+
					goalData.getData().getTitle()+"\" ("+
					goalData.getData().getGoalId()+") of the user "+ 
					loggedUser.getUser().getName()+" "+loggedUser.getUser().getLastname() + 
					" ("+loggedUser.getUser().getId()+")" );
			
			PageUtil.fireSuccessfulInfoMessage("compSearchGrowl", "Competence '"+competenceToConnect.getTitle()+
					"' is added to the learning goal '"+goalData.getData().getTitle()+"'.");
		} catch (Exception e) {
			logger.error("There was an error connecting Competence \""+competenceToConnect+" to the Learning Goal \""+
					goalData.getData().getTitle()+"\" ("+
					goalData.getData().getGoalId()+") of the user "+ 
					loggedUser.getUser() + ". " + e);
			
			PageUtil.fireErrorMessage("compSearchGrowl", "There was an error connecting competence "+competenceToConnect.getTitle()+".");
		}
	}

	public void addTargetCompetence(TargetCompetence newCompetence, GoalDataCache goalData, Event event) throws EventException {
		goalData.addCompetence(newCompetence);
		goalData.removeRecommendedCompetence(newCompetence.getCompetence());
		goalData.setData(new GoalData(newCompetence.getParentGoal()));
		
		// update Competence status cache
		competenceStatusCache.addInProgressCompetence(newCompetence.getCompetence().getId());
		//SocialActivityWallData wallData=
				socialActivityHandler.addSociaActivitySyncAndPropagateToStatusAndGoalWall(event);
//				socialActivityHandler.addSociaActivitySyncAndPropagateToGoalWall(event, goalData, loggedUser.getUser(), 
//						 loggedUser.getLocale());
		// add to status wall
		//activityWallBean.addWallActivity(wallData);
		//activityWallUtilBean.addSociaActivitySyncAndPropagate(event, goalData, loggedUser.getUser(), true, true);
	}

	public void setCompletionOfSelectedCompetence(boolean completed, String context) {
		completeCompetence(this.goalBean.getSelectedGoalData().getSelectedCompetence(), completed, context);
	}

	public void completeCompetence(CompetenceDataCache competenceDataCache, final boolean completed, final String context) {
		
		// update competence cache
		competenceDataCache.setCompleted(completed);
		
		Locale locale = loggedUser.getLocale();
		
		try {
			String completedLabel = ResourceBundleUtil.getMessage(
				"goals.competences.changeCompleted.completed", 
				locale);
		
			String notCompletedLabel = ResourceBundleUtil.getMessage(
				"goals.competences.changeCompleted.notCompleted", 
				locale);
			
			PageUtil.fireSuccessfulInfoMessageFromBundle(
				"goals.competences.changeCompleted",
				locale, 
				competenceDataCache.getData().getTitle(),
				completed ? completedLabel : notCompletedLabel);
		} catch (KeyNotFoundInBundleException e2) {
			logger.error(e2);
		}
		
		goalBean.recalculateSelectedGoalProgress();
		
		final CompetenceDataCache competenceDataCache1 = competenceDataCache;
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
			 
					// update Portfolio cache if exists
			    	final PortfolioBean portfolioBean = (PortfolioBean) applicationBean.getUserSession(loggedUser.getUser().getId()).getAttribute("portfolio");
			    	portfolioBean.populateWithActiveCompletedCompetences();

			    	Session session = (Session) compManager.getPersistence().openSession();
					try{
					TargetCompetence tComp = compManager.loadResource(
							TargetCompetence.class, 
							competenceDataCache1.getData().getId(),
							session);
				
					tComp.setCompleted(completed);
					tComp.setCompletedDay(new Date());
					compManager.saveEntity(tComp, session);
					
					// updating competence status cache
					competenceStatusCache.addCompletedCompetence(competenceDataCache1.getData().getCompetenceId());
					
					try {
						Map<String, String> parameters = new HashMap<String, String>();
						parameters.put("context", context);
						parameters.put("targetGoalId", String.valueOf(tComp.getParentGoal().getId()));
						
						EventType event = completed ? EventType.Completion : EventType.NotCompleted;
						
						eventFactory.generateEvent(event, loggedUser.getUser(), tComp, parameters);
					} catch (EventException e) {
						logger.error(e);
					}
					
					session.flush();
				 
				} catch (ResourceCouldNotBeLoadedException e1) {
					logger.error(e1);
				} 
					finally{
	 				HibernateUtil.close(session);
	 			} 
			}
		});
	}
	
	@SuppressWarnings("unused")
	private void completeActivitiesAsync(final List<ActivityWallData> activities) {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				for (ActivityWallData activityData : activities) {
					try {
						TargetActivity activity = compManager.loadResource(TargetActivity.class, activityData.getObject().getId(), true);
					
						if (!activity.isCompleted()) {
//							compWallBean.updateActivityDataInUserInboxAsync(activity);
		
							logger.debug("Setting as completed activity \""+activity+
									"\" by the user "+ loggedUser.getUser());
						}
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error(e);
					}
				}
			}
		});
	}
	
	public void deleteCompetence(CompetenceDataCache compData) {
		GoalDataCache goalDataCache = goalBean.getSelectedGoalData();
		
		boolean successful = deleteCompetence(compData, goalDataCache);
		
		// if the goal is connected to a course, then delete the CourseCompetence too
		if (goalDataCache.getData().isConnectedWithCourse()) {
			try {
				courseManager.removeCompetenceFromEnrollment(goalDataCache.getData().getCourse().getEnrollmentId(), compData.getData().getCompetenceId());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
		
		if (successful) {
			logger.debug("Competence '"+compData.getData().getTitle()+"' is removed from the learning goal \""+
					goalDataCache.getData().getTitle()+"\" ("+
					goalDataCache.getData().getGoalId()+") by the user "+ 
					loggedUser.getUser());
			PageUtil.fireSuccessfulInfoMessage("deleteCompForm:deleteCompGrowl", "Competence '"+compData.getData().getTitle()+"' is removed!");
		} else {
			logger.error("Could not remove competence '"+compData.getData().getTitle()+"' from the learning goal \""+
					goalDataCache.getData().getTitle()+"\" ("+
					goalDataCache.getData().getGoalId()+") by the user "+ 
					loggedUser.getUser() );
			PageUtil.fireErrorMessage(":deleteCompForm:deleteCompGrowl", "There was an error removing competence.");
		}
	}
	
	public boolean deleteCompetence(CompetenceDataCache compData, GoalDataCache goalDataCache) {
		logger.debug("Deleting competence '"+compData.getData().getTitle()+"\" by the user "+ 
				loggedUser.getUser());
		
		try {
			GoalTargetCompetenceAnon goalComp = goalManager.deleteTargetCompetenceFromGoal(
					loggedUser.getUser(), 
					goalDataCache.getData().getTargetGoalId(), 
					compData.getData().getId());

			goalDataCache.removeCompetence(compData.getData().getId());
			goalDataCache.setSelectedCompetence(null);
			
			goalBean.recalculateGoalProgress(goalBean.getSelectedGoalData());
			
			eventFactory.generateEvent(EventType.Detach, loggedUser.getUser(), goalComp.getTargetCompetence(), goalComp.getTargetGoal());
			
			return true;
		} catch (EventException e) {
			logger.error(e);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		return false;
	}
	
	/*
	 * GETTERS/SETTERS
	 */
	
	public CompetenceDataCache getSelectedComp() {
		return selectedComp;
	}

	public List<CompetenceDataCache> getCompetences() {
		if (goalBean.getSelectedGoalData() == null) {
			return null;
		}
		// if not initialized
		if (goalBean.getSelectedGoalData().getCompetences() == null) {
			goalBean.getSelectedGoalData().initCompetences();
			//goalBean.getSelectedGoalData().selectCompetence(goalBean.getTargetCompId());
		}
		return goalBean.getSelectedGoalData().getCompetences();
	}

	public CompetenceFormData getFormData() {
		return formData;
	}
	
	public void useCompetenceDialog(Competence competence, String context){
		useCompetenceDialogId(competence.getId(), context);
	}
	
	public void useCompetenceDialogId(long competenceId, String context){
		loggingNavigationBean.logServiceUse(
				ComponentName.COMPETENCE_DIALOG,
				"action",  "openCompetenceDialog",
				"context", context,
				"competenceId", String.valueOf(competenceId));
	}
	
	public void useTargetCompetenceDialogId(long targetCompId, String context){
		loggingNavigationBean.logServiceUse(
				ComponentName.COMPETENCE_DIALOG,
				"action", "openCompetenceDialog",
				"context", context,
				"targetCompetenceId", String.valueOf(targetCompId));
	}
	
	public void useTargetCompetenceDialog(TargetCompetence targetComp, String context){
		useTargetCompetenceDialogId(targetComp.getId(), context);
	}

}
