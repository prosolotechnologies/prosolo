package org.prosolo.web.dialogs;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.dialogs.data.AddToGoalData;
import org.prosolo.web.goals.GoalWallBean;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.goals.competences.CompetenceStatusCache;
import org.prosolo.web.goals.competences.CompetencesBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.useractions.PostActionBean;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.useractions.util.PostUtil;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "addToGoalDialog")
@Component("addToGoalDialog")
@Scope("view")
public class AddToGoalDialog implements Serializable {

	private static final long serialVersionUID = -8047212713456919507L;
	
	private static Logger logger = Logger.getLogger(AddToGoalDialog.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private LearningGoalManager goalManager;
	@Autowired private LearningGoalsBean goalsBean;
	@Autowired private CompetenceStatusCache competenceStatusCache;
	@Autowired private PostActionBean postAction;
	@Autowired private UploadManager uploadManager;
	@Autowired private CompetencesBean competencesBean;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private Competence competenceToAdd;
	private List<AddToGoalData> goalsData = new ArrayList<AddToGoalData>();
	private long chosenGoal;
	private String context;
	private String updateAfterAddToGoal;
	
	private List<SelectItem> goalsOptions = new LinkedList<SelectItem>();
	
	private NewPostData newPostData = new NewPostData();
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}

	/*
	 * ACTIONS
	 */

	public void initialize(final Competence comp, final String context) {
		reset();
		this.context = context;
		this.competenceToAdd = comp;
		initializeGoals(comp);
		
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	actionLogger.logServiceUse(
        			ComponentName.EVALUATION_LIST_DIALOG, 
        			"context", context,
        			"compId", String.valueOf(comp.getId()));
            }
		});
	}

	public void initializeGoalsByCompetenceId(final long competenceId, final String context) {
		try {
			Competence comp = goalManager.loadResource(Competence.class, competenceId, true);
			initialize(comp, context);
			
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	actionLogger.logServiceUse(
	        			ComponentName.EVALUATION_LIST_DIALOG, 
	        			"context", context,
	        			"compId", String.valueOf(competenceId));
	            }
			});
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void initializeGoals(Competence comp) {
		this.goalsData.clear();
		
		List<GoalDataCache> userGoals = goalsBean.getGoals();
		goalsOptions = new ArrayList<SelectItem>();
		
		goalsOptions.add(new SelectItem(-1, "----------", null, false));
		this.chosenGoal = -1;

		if (userGoals != null && !userGoals.isEmpty()) {
			List<TargetLearningGoal> goalWithThisCompetence = goalManager.getUserGoalsContainingCompetence(loggedUser.getUser(), comp);

			for (GoalDataCache goalCache : userGoals) {
				GoalData goalData = goalCache.getData();

				boolean disabled = false;
				
				for (TargetLearningGoal targetGoal : goalWithThisCompetence) {
					if (targetGoal.getLearningGoal().getId() == goalCache.getData().getGoalId()) {
						disabled = true;
						break;
					}
				}
				
				AddToGoalData addToGoalData = new AddToGoalData(goalCache, disabled);
				goalsData.add(addToGoalData);
				
				goalsOptions.add(new SelectItem(goalData.getGoalId(), goalData.getTitle(), null, disabled));
			}
		}
	}
	
	

	public void addCompetenceToChosenGoal() {
		GoalDataCache chosenGoalData = findChosenGoal();
		
		if (chosenGoalData != null) {
//			PageUtil.fireSuccessfulInfoMessage("achievedComps:addToGoalDialogGrowl", "You have added competence '"+competenceToAdd.getTitle()+"' to the goal '"+chosenGoalData.getGoal().getTitle()+"'.");
			
//			// update competence status cache
//			competenceStatusCache.addInProgressCompetence(competenceToAdd);
//			
//			// must assign to the final variable as the data will be reseted
//			final Competence compToAdd1 = competenceToAdd;
//			final GoalDataCache chosenGoalData1 = chosenGoalData;
			
			competencesBean.connectCompetenceAndUpdateCourse(competenceToAdd, chosenGoalData, context);

			reset();
			
//			taskExecutor.execute(new Runnable() {
//	            @Override
//	            public void run() {
//	            	try {
//		            	TargetCompetence tComp = goalManager.cloneAndAttachCompetenceToGoal(loggedUser.getUser(), chosenGoalData1.getGoal(), compToAdd1);
//		            	chosenGoalData1.addCompetence(tComp);
//		            } catch (EventException e) {
//		            	logger.error(e);
//		            }
//	            }
//			});
		}
	}

	private GoalDataCache findChosenGoal() {
		for (AddToGoalData goalData : goalsData) {
			if (goalData.getGoalId() == chosenGoal) {
				return goalData.getGoalData();
			}
		}
		return null;
	}
	
	public void prepareAddToGoalForPost(final String link, final String title, final String context) {
		reset();
		
		if (title != null && link != null) {
			newPostData = new NewPostData();
			newPostData.setTitle(title);
			newPostData.setText(title + " " + link);
			newPostData.setLink(link);
			
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	postAction.fetchLinkContents(newPostData);
	            	actionLogger.logServiceUse(
	            			ComponentName.EVALUATION_LIST_DIALOG, 
	            			"context", context,
	            			"link", link,
	            			"title", title);
	            }
			});
			
			// init goals
			initializeGoals(null);
		}
	}
	
	public void prepareAddToGoalForSocialActivityById(final long socialActivityId, final String context) {
		try {
			SocialActivity socialActivity = goalManager.loadResource(SocialActivity.class, socialActivityId);
			prepareAddToGoalForSocialActivity(socialActivity, context);
			
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	postAction.fetchLinkContents(newPostData);
	            	actionLogger.logServiceUse(
	            			ComponentName.EVALUATION_LIST_DIALOG, 
	            			"socialActivityId", String.valueOf(socialActivityId),
	            			"context", context);
	            }
			});
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void prepareAddToGoalForSocialActivity(SocialActivity socialActivity, String context) {
		if (socialActivity != null) {
			newPostData = PostUtil.convertSocialActivityToNewPostData(socialActivity);
		}
		
		this.context = context;
		
		// init goals
		initializeGoals(null);
	}
	
	public void addLinkToChosenGoal() {
		GoalDataCache chosenGoalData = findChosenGoal();
		
		if (chosenGoalData != null) {
			GoalWallBean goalWall = PageUtil.getViewScopedBean("goalwall", GoalWallBean.class);
			goalWall.createNewPost(chosenGoalData, newPostData);
		}
	}
	
	private void reset() {
		this.competenceToAdd = null;
		this.newPostData = null;
		this.goalsOptions = null;
		this.context = null;
	}
	
	public void handleFileUpload(FileUploadEvent event) {
    	UploadedFile uploadedFile = event.getFile();
    	
		try {
			AttachmentPreview attachmentPreview = uploadManager.uploadFile(loggedUser.getUser(), uploadedFile, uploadedFile.getFileName());
			
			newPostData.setAttachmentPreview(attachmentPreview);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
    }

	/*
	 * GETTERS / SETTERS
	 */
	public Competence getCompetenceToAdd() {
		return competenceToAdd;
	}

	public List<AddToGoalData> getGoalsData() {
		return goalsData;
	}

	public long getChosenGoal() {
		return chosenGoal;
	}

	public void setChosenGoal(long chosenGoal) {
		this.chosenGoal = chosenGoal;
	}

	public NewPostData getNewPostData() {
		return newPostData;
	}

	public void setNewPostData(NewPostData newPostData) {
		this.newPostData = newPostData;
	}

	public List<SelectItem> getGoalsOptions() {
		return goalsOptions;
	}

	public void setGoalsOptions(List<SelectItem> goalsOptions) {
		this.goalsOptions = goalsOptions;
	}

	public String getUpdateAfterAddToGoal() {
		return updateAfterAddToGoal;
	}

	public void setUpdateAfterAddToGoal(String updateAfterAddToGoal) {
		this.updateAfterAddToGoal = updateAfterAddToGoal;
	}
	
}
