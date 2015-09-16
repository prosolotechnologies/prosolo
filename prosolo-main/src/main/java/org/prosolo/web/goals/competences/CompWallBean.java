package org.prosolo.web.goals.competences;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.activitywall.data.NodeData;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.goals.RecommendedLearningPlansBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.data.AvailableLearningPlan;
import org.prosolo.web.goals.util.CompWallActivityConverter;
import org.prosolo.web.portfolio.PortfolioBean;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.nodes.ActivityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name="compwall")
@Component("compwall")
@Scope("view")
public class CompWallBean implements Serializable {
	
	private static final long serialVersionUID = 431515679405885640L;

	protected static Logger logger = Logger.getLogger(CompWallBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private LearningGoalManager goalManager;
	@Autowired private CompetenceManager compManager;
	@Autowired private ActivityManager activityManager;
	@Autowired private CompWallActivityConverter compWallActivityConverter;
	@Autowired private UploadManager uploadManager;
	@Autowired private EventFactory eventFactory;
	
	@Autowired private RecommendedLearningPlansBean recommendedLearningPlans;
	@Autowired private LearningGoalsBean goalsBean;
	@Autowired private ApplicationBean applicationBean;
	
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private NewPostData newActFormData = new NewPostData();
	
	public void init() {
		logger.debug("Initializing");
		this.newActFormData = new NewPostData();
	}
	
	public void initializeActivities() {
		logger.debug("Initializing goal wall");
		
		if (goalsBean.getSelectedGoalData() != null) {
			goalsBean.getSelectedGoalData().getSelectedCompetence().initializeActivities();
		}
	}
	
	public void connectAllActivities(AvailableLearningPlan alPlan) {
		List<TargetActivity> newActivities = null;		
		long targetCompId = goalsBean.getSelectedGoalData().getSelectedCompetence().getData().getId();
		
		try {
			
			long targetGoalId = goalsBean.getSelectedGoalData().getData().getTargetGoalId();
			
			newActivities = goalManager.cloneActivitiesAndAddToTargetCompetence(
					loggedUser.getUser(),
					targetCompId, 
					alPlan.getPlan().getActivities(),
					true,
					"learn.targetGoal."+targetGoalId+".targetComp."+targetCompId+".activityRecommend");
			
			addActivities(goalsBean.getSelectedGoalData().getSelectedCompetence(), newActivities);
			
			// mark activities as contained in the selected competence
//			goalsBean.getSelectedGoalData().getSelectedCompetence().markActivitiesAsAdded(newActivities);
			recommendedLearningPlans.disableAddingActivities(alPlan);
			recommendedLearningPlans.calculateConnectAllFlag(alPlan);
		} catch (EventException e) {
			logger.error(e.getMessage());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e.getMessage());
		}
		
		if (newActivities != null) {
			// update progress of goal and competence
			refreshCompetenceData(goalsBean.getSelectedGoalData().getSelectedCompetence());
			goalsBean.recalculateSelectedGoalProgress();
			
			logger.debug("Added all activities of a learning plan ("+alPlan.getPlan()+
					") to a Target Competence \""+
					targetCompId+"\" of the user "+ 
					loggedUser.getUser().getName()+" "+loggedUser.getUser().getLastname() + 
					" ("+loggedUser.getUser().getId()+")" );
			PageUtil.fireSuccessfulInfoMessage("All activities added!");
		} else {
			logger.error("There was an error connecting all activities of a LearningPlan " +
					"("+alPlan.getPlan()+") " +
					"to competence \""+targetCompId+"\" of the user "+ 
					loggedUser.getUser().getName()+" "+loggedUser.getUser().getLastname() + 
					" ("+loggedUser.getUser().getId()+")");
			PageUtil.fireErrorMessage("There was an error adding all activities!");
		}
	}
	
	// Called also from the activity search box
	public void connectActivity(Activity activity) {
		long targetGoalId = goalsBean.getSelectedGoalData().getData().getTargetGoalId();
		
		long targetCompId = goalsBean.getSelectedGoalData().getSelectedCompetence().getData().getId();
		connectActivity(
				activity, 
				goalsBean.getSelectedGoalData().getSelectedCompetence(), 
				"learn.targetGoal."+targetGoalId+".targetComp."+targetCompId+".activitySearch");
	}
	
	public void connectActivityWallData(ActivityWallData activityData, String context) {
		connectActivityById(activityData.getActivity().getId(), context);
		activityData.setCanBeAdded(false);
	}

	public void connectActivityById(long activityId, String context) {
		try {
			Activity activity = goalManager.loadResource(Activity.class, activityId);
			connectActivity(activity, goalsBean.getSelectedGoalData().getSelectedCompetence(), context);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void connectActivity(Activity act, CompetenceDataCache compData, String context) {
		Activity activity = goalManager.merge(act);
		long parentTargetCompId = compData.getData().getId();

		String activityTitle = activity.getTitle();
		
		logger.debug("Connecting Activity \""+activity+"\" to the competence \""+
				parentTargetCompId+"\" for the user "+ loggedUser.getUser());
			
		try {
			TargetActivity newActivity = goalManager.addActivityToTargetCompetence(
					loggedUser.getUser(),
					parentTargetCompId, 
					activity,
					context);
		
			// update cache
			addActivity(compData, newActivity);
			
			refreshCompetenceData(compData);
			goalsBean.recalculateGoalProgress(compData.getParentGoalDataCache());
			
			// update suggested activities cache
			recommendedLearningPlans.disableAddingActivity(activity.getId());
			
			logger.debug("Activity \""+activityTitle+"\" ("+newActivity.getId()+
					") connected to the target competence \""+
					parentTargetCompId+"\" of the user "+ loggedUser.getUser() );
			
			PageUtil.fireSuccessfulInfoMessage("Activity "+activityTitle+ " is added!");
		} catch (EventException e) {
			logger.error(e.getMessage());
			
			logger.error("There was an error connecting activity \""+activity+" to the learning goal \""+
					parentTargetCompId+"\" of the user "+ loggedUser.getUser());
			
			PageUtil.fireErrorMessage("Error connecting activity \""+activityTitle+"!\n " +
					"Please check if the activity is already added to your goal.");
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e.getMessage());
			
			logger.error("There was an error connecting activity \""+activity+" to the learning goal \""+
					parentTargetCompId+"\" of the user "+ loggedUser.getUser());
			
			PageUtil.fireErrorMessage("Error connecting activity \""+activityTitle+"!\n " +
					"Please check if the activity is already added to your goal.");
		}
	}
	
	public void deleteActivity(ActivityWallData activityData, String context) {
		boolean successful = false;
		try {
			TargetActivity activityToRemove = activityManager.loadResource(TargetActivity.class, activityData.getObject().getId());
		
			logger.debug("Deleting activity \""+activityToRemove+"\" by the user "+ 
					loggedUser.getUser() );
			
			try {
				successful = goalManager.detachActivityFromTargetCompetence(
						loggedUser.getUser(),
						activityToRemove,
						context);
			} catch (EventException e) {
				logger.error(e.getMessage());
			}
			
			TargetCompetence parentTargetComp = (TargetCompetence) activityToRemove.getParentCompetence();
				
			if (successful) {
				// removing from selected competence's activities
				Iterator<ActivityWallData> actIterator = goalsBean.getSelectedGoalData().getSelectedCompetence().getActivities().iterator();
				
				while (actIterator.hasNext()) {
					ActivityWallData activityWallData = (ActivityWallData) actIterator.next();
					
					if (activityData.equals(activityWallData)) {
						actIterator.remove();
						break;
					}
				}
				
				// update progress of goal and competence
				refreshCompetenceData(goalsBean.getSelectedGoalData().getSelectedCompetence());
				goalsBean.recalculateSelectedGoalProgress();
				
				// update suggested learning plans-> activities - enable connecting again this activity
				recommendedLearningPlans.enableAddingActivity(activityToRemove);
				
				logger.debug("Activity \""+activityToRemove+"\" has been removed from the learning goal \""+
						parentTargetComp.getCompetence().getTitle()+"\" ("+
						parentTargetComp.getId()+
						") by the user "+ loggedUser.getUser());
				
				PageUtil.fireSuccessfulInfoMessage("Activity "+activityData.getActivity().getTitle()+" removed!");
			} else {
				logger.error("Could not remove activity \""+activityToRemove+"\" from the learning goal \""+
						parentTargetComp.getCompetence()+"\" ("+
						parentTargetComp.getId()+") by the user "+ 
						loggedUser.getUser());
				
				PageUtil.fireErrorMessage("There was an error removing activity!");
			}
		} catch (ResourceCouldNotBeLoadedException e1) {
			logger.error(e1);
		}
	}
	
	public void completeActivity(ActivityWallData activityData, String context) {
		try {
			TargetActivity tActivity = activityManager.loadResource(TargetActivity.class, activityData.getObject().getId());
			Date now = new Date();

			tActivity.setCompleted(true);
			tActivity.setDateCompleted(now);
			
			activityData.setCompleted(true);
			activityData.setDateCompleted(DateUtil.getPrettyDate(now));
		
			PageUtil.fireSuccessfulInfoMessage("Activity '"+activityData.getActivity().getTitle()+"' is completed!");
			
			// update progress of goal and competence
			refreshCompetenceData(goalsBean.getSelectedGoalData().getSelectedCompetence());
			goalsBean.recalculateSelectedGoalProgress();
			
			toggleActivityCompletedAsync(activityData, tActivity, context);
			
			// update recommended activity plans
			if (recommendedLearningPlans != null) {
				recommendedLearningPlans.toggleCompletedActivity(tActivity.getActivity().getId(), true);
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void uncompleteActivity(ActivityWallData activityData, String context) {
		try {
			TargetActivity tActivity = activityManager.loadResource(TargetActivity.class, activityData.getObject().getId());
			tActivity.setCompleted(false);
			tActivity.setDateCompleted(null);
			activityData.setCompleted(false);
		
			PageUtil.fireSuccessfulInfoMessage("Activity '"+activityData.getActivity().getTitle()+"' marked as incomplete!");
			
			// update progress of goal and competence
			refreshCompetenceData(goalsBean.getSelectedGoalData().getSelectedCompetence());
			goalsBean.recalculateSelectedGoalProgress();
			
			toggleActivityCompletedAsync(activityData, tActivity, context);
			
			// update recommended activity plans
			if (recommendedLearningPlans != null) {
				recommendedLearningPlans.toggleCompletedActivity(tActivity.getActivity().getId(), false);
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	private void toggleActivityCompletedAsync(final ActivityWallData activityData, final TargetActivity activity, final String context) {
		final TargetActivity activity1 = activityManager.saveEntity(activity);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				logger.debug("Toggled value of completed field of aActivity \""+activity+"\" by the user "+ loggedUser.getUser());
					
				try {
					Map<String, String> paramaters = new HashMap<String, String>();
					paramaters.put("context", context);
					paramaters.put("targetActivity", String.valueOf(activityData.getId()));
					
					EventType eventType = activity.isCompleted() ? EventType.Completion : EventType.NotCompleted;
					
					eventFactory.generateEvent(eventType, loggedUser.getUser(), activity1, paramaters);
				} catch (EventException e) {
					TargetCompetence parentTargetComp = (TargetCompetence) activity.getParentCompetence();
					logger.error("Could not remove activity \""+activity+"\" from the learning plan \""+
							parentTargetComp.getCompetence().getTitle()+"\" ("+
							parentTargetComp.getId()+") by the user "+ 
							loggedUser.getUser());
				}
			}
		});
	}
	
	public void createNewPost() {
		CompetenceDataCache competenceDataCache = goalsBean.getSelectedGoalData().getSelectedCompetence();
		
		long targetCompId = goalsBean.getSelectedGoalData().getSelectedCompetence().getData().getId();
		long targetGoalId = goalsBean.getSelectedGoalData().getData().getTargetGoalId();
			
		createNewActivity(
				this.newActFormData, 
				competenceDataCache, 
				"learn.targetGoal."+targetGoalId+".targetComp."+targetCompId);
	}
	
	public void createNewActivity(NewPostData newActFormData, CompetenceDataCache competenceDataCache, 
			String context) {
		
		long targetCompId = competenceDataCache.getData().getId();

		// clean html tags
		newActFormData.setText(StringUtil.cleanHtml(newActFormData.getText()));
		
		String title = newActFormData.getTitle();
		String description = newActFormData.getText();
		
		try {
			TargetActivity newActivity = goalManager.createActivityAndAddToTargetCompetence(
					loggedUser.getUser(),
					title, 
					description,
					newActFormData.getAttachmentPreview(),
					newActFormData.getVisibility(),
					targetCompId,
					newActFormData.isConnectWithStatus(),
					context);
			
			@SuppressWarnings("unused")
			ActivityWallData actData = addActivity(competenceDataCache, newActivity);
			
			logger.debug("Created new activity \""+title+"\" and connected it to the learning goal '"+
					targetCompId+"' of the user "+ 
					loggedUser.getUser());
			
			PageUtil.fireSuccessfulInfoMessage("Created new activity "+title+".");
		} catch (EventException e) {
			logger.error("There was an error creating new activity titled \""+title+
					" to the Learning Goal '"+targetCompId+"' of the user "+ 
					loggedUser.getUser()+" - "+ e.getMessage());
			PageUtil.fireErrorMessage("There was an error creating new activity!");
		} catch (Exception e) {
			logger.error("There was an error creating new activity titled \""+title+
					" to the Learning Goal '"+targetCompId+"' of the user "+ 
					loggedUser.getUser()+" - "+ e.getMessage());
			PageUtil.fireErrorMessage("There was an error creating new activity!");
		}
		
		this.newActFormData = new NewPostData();
		
		// update progress of goal and competence
		refreshCompetenceData(competenceDataCache);
		goalsBean.recalculateSelectedGoalProgress();
	}

	public void addActivities(CompetenceDataCache selectedCompetence, List<TargetActivity> targetActivities) {
		if (targetActivities != null && !targetActivities.isEmpty()) {
			for (TargetActivity tActivity : targetActivities) {
				addActivity(selectedCompetence, tActivity);
			}
		}
	}

	public ActivityWallData addActivity(CompetenceDataCache compData, TargetActivity newActivity) {
		if (compData.getActivities() != null) {
			ActivityWallData actData = compWallActivityConverter.convertTargetActivityToActivityWallData(
					compData,
					newActivity, 
					loggedUser.getUser(), 
					loggedUser.getInterfaceSettings().getLocaleSettings().createLocale(), 
					true, 
					false);
			compData.addActivity(actData);
			return actData;
		}
		return null;
	}
	
	public void updateActivity(CompetenceDataCache compData, TargetActivity targetActivity) {
		if (compData.getActivities() != null) {
			
			for (int i = 0; i < compData.getActivities().size(); i++) {
				if (compData.getActivities().get(i).getObject().getId() == targetActivity.getId()) {
					ActivityWallData actData = compWallActivityConverter.convertTargetActivityToActivityWallData(
							compData,
							targetActivity, 
							loggedUser.getUser(), 
							loggedUser.getInterfaceSettings().getLocaleSettings().createLocale(), 
							true, 
							false);
					compData.getActivities().set(i, actData);
					break;
				}
			}
		}
	}
	
	public void handleFileUpload(FileUploadEvent event) {
    	UploadedFile uploadedFile = event.getFile();
    	
		try {
			AttachmentPreview attachmentPreview = uploadManager.uploadFile(loggedUser.getUser(), uploadedFile, uploadedFile.getFileName());
			
			newActFormData.setAttachmentPreview(attachmentPreview);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
    }
	
	private void recalculateCompetenceProgress(CompetenceDataCache compData) {
		if (compData != null) {
			boolean originalCompletionState = compData.isCompleted();
			
			double progress = calculateCompetenceProgress(compData);
			
			compData.setCompleted(progress == 1 ? true : false);
			
			if (originalCompletionState != compData.isCompleted()) {
				try {
					compManager.updateTargetCompetenceProgress(compData.getData().getId(), compData.isCompleted());
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
				
				taskExecutor.execute(new Runnable() {
		            @Override
		            public void run() {
						// update Portfolio cache if exists
				    	final PortfolioBean portfolioBean = (PortfolioBean) applicationBean.getUserSession(loggedUser.getUser().getId()).getAttribute("portfolio");
				    	portfolioBean.populateWithActiveCompletedCompetences();
		            }
		        });
			}
		}
	}
	
	public static double calculateCompetenceProgress(CompetenceDataCache compData) {
		double totalActivities = 0;
		double completedActivities = 0;
		
		if (compData.getActivities() != null && compData.getActivities().size() > 0) {
			totalActivities = compData.getActivities().size();
			
			for (ActivityWallData actData : compData.getActivities()) {
				if (actData.isCompleted())
					completedActivities++;
			}
			
			return completedActivities / totalActivities;
		}
		return 0;
	}
	
	public void refreshCompetenceData(CompetenceDataCache compData) {
		recalculateCompetenceProgress(compData);
		compData.calculateCanNotBeMarkedAsCompleted();
	}
	
	public void handleAssignmentUpload(FileUploadEvent event) {
		final String context = (String) event.getComponent().getAttributes().get("context");
		long targetActivityId = (Long) event.getComponent().getAttributes().get("targetActivityId");
		final ActivityWallData activityToUploadAssignemnt = goalsBean.getSelectedGoalData().getSelectedCompetence().getActivity(targetActivityId);
		
		NodeData activity = activityToUploadAssignemnt.getActivity();
		
		logger.debug("User "+loggedUser.getUser()+" is uploading an assignemnt for activity '" + 
				activity.getTitle() + "' (" + 
				activity.getUri()+")");
		
    	UploadedFile uploadedFile = event.getFile();
    	
		try {
			final String fileName = uploadedFile.getFileName();
			final String link = uploadManager.storeFile(loggedUser.getUser(), uploadedFile, fileName);

			if (activityToUploadAssignemnt != null) {
				activityToUploadAssignemnt.getAttachmentPreview().setUploadedAssignmentLink(link);
				activityToUploadAssignemnt.getAttachmentPreview().setUploadedAssignmentTitle(fileName);
				completeActivity(activityToUploadAssignemnt, context);
				
				final ActivityWallData activityToUploadAssignemnt1 = activityToUploadAssignemnt;
				
				taskExecutor.execute(new Runnable() {
		            @Override
		            public void run() {
		            	try {
		            		Session session = (Session) activityManager.getPersistence().openSession();
		            		
							TargetActivity targetActivity = activityManager.updateTargetActivityWithAssignement(
									activityToUploadAssignemnt1.getObject().getId(), 
									link,
									fileName,
									session);
							try {
								Map<String, String> parameters = new HashMap<String, String>();
								parameters.put("context", context);
								parameters.put("targetActivityId", String.valueOf(targetActivity.getId()));
								eventFactory.generateEvent(EventType.FileUploaded, loggedUser.getUser(), targetActivity, parameters);
							} catch (EventException e) {
								logger.error(e);
							}
							
							session.flush();
							session.close();
						} catch (ResourceCouldNotBeLoadedException e) {
							logger.error(e);
						}
		            }
		        });
				
				goalsBean.recalculateSelectedGoalProgress();
			}
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
    }
	
	public void removeAssignmentFromActivity(final ActivityWallData activityData) {
		final String context = PageUtil.getPostParameter("context");
		
		activityData.getAttachmentPreview().setUploadedAssignmentLink(null);
		activityData.getAttachmentPreview().setUploadedAssignmentTitle(null);
		uncompleteActivity(activityData, context);
		
		goalsBean.recalculateSelectedGoalProgress();
		refreshCompetenceData(activityData.getCompData());
		
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	try {
            		Session session = (Session) activityManager.getPersistence().openSession();
            		
					TargetActivity targetActivity = activityManager.updateTargetActivityWithAssignement(
							activityData.getObject().getId(), 
							null,
							null,
							session);
					try {
						Map<String, String> parameters = new HashMap<String, String>();
						parameters.put("context", context);
						parameters.put("targetActivityId", String.valueOf(targetActivity.getId()));
						
						eventFactory.generateEvent(EventType.AssignmentRemoved, loggedUser.getUser(), targetActivity, parameters);
					} catch (EventException e) {
						logger.error(e);
					}
					
					session.flush();
					session.close();
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
            }
        });
	}

	/*
	 * GETTERS / SETTERS
	 */
	
	public List<ActivityWallData> getActivities() {
		if (goalsBean.getSelectedGoalData() != null && goalsBean.getSelectedGoalData().getSelectedCompetence() != null) {
			return goalsBean.getSelectedGoalData().getSelectedCompetence().getActivities();
		}
		return null;
	}
	
	public String getAllActivitiesIds() {
		return ActivityUtil.extractActivityIds(getActivities());
	}
	
	public NewPostData getNewActFormData() {
		return newActFormData;
	}

}
