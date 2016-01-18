package org.prosolo.web.dialogs;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.event.EventException;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.goals.competences.CompWallBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.search.data.ActivityData;
import org.prosolo.web.useractions.PostActionBean;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "addToCompetenceDialog")
@Component("addToCompetenceDialog")
@Scope("view")
public class AddToCompetenceDialog implements Serializable {

	private static final long serialVersionUID = -8047212713456919507L;
	
	private static Logger logger = Logger.getLogger(AddToCompetenceDialog.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private LearningGoalManager goalManager;
	@Autowired private LearnBean goalsBean;
	@Autowired private PostActionBean postAction;
	@Autowired private UploadManager uploadManager;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private long selectedCompetence;
	private List<CompetenceDataCache> competencesData = new ArrayList<CompetenceDataCache>();
	private List<SelectItem> competenceItems = new LinkedList<SelectItem>();
	private String toUpdateAfterAdding;
	
	private NewPostData newPostData = new NewPostData();
	private ActivityData activity;
	private AttachmentPreview attachmentPreview;
	private boolean noActiveCompetences = true;
	private String context;
	
	/*
	 * ACTIONS
	 */

	public void initializeCompetences() {
		List<GoalDataCache> userGoals = goalsBean.getGoals();
		this.competenceItems = new ArrayList<SelectItem>();
		this.competencesData.clear();
		
		competenceItems.add(new SelectItem(-1, "----------", null, false));
		this.selectedCompetence = -1;
		
		String targetCompIdToSelectString = PageUtil.getPostParameter("targetCompIdToSelect");
		if (targetCompIdToSelectString != null && targetCompIdToSelectString.length() > 0 && !targetCompIdToSelectString.equals("null")) {
			try {
				selectedCompetence = Long.parseLong(targetCompIdToSelectString);
			} catch (NumberFormatException nfe) {
				logger.warn("Parameter targetCompIdToSelect does not have value that can be "
						+ "converted to long. Value passed is: '"+targetCompIdToSelectString+"'");
			}
		}
		
		if (userGoals != null && !userGoals.isEmpty()) {
			for (GoalDataCache goalCache : userGoals) {
				SelectItemGroup group1 = new SelectItemGroup(goalCache.getData().getTitle());
				
				SelectItem[] comps = new SelectItem[goalCache.getCompetences().size()];
				
				for (int i = 0; i < goalCache.getCompetences().size(); i++) {
					CompetenceDataCache compData = goalCache.getCompetences().get(i);
					
					boolean disabled = false;
					
					if (activity != null) {
						try {
							disabled = compData.containsActivity(activity.getActivity());
						} catch (ResourceCouldNotBeLoadedException e) {
							logger.error(e);
						}
					}
					
					if (disabled && selectedCompetence == compData.getData().getId()) {
						selectedCompetence = -1;
					}
					
					comps[i] = new SelectItem(
							compData.getData().getId(), 
							compData.getData().getTitle(),
							null,
							disabled);
					
					this.competencesData.add(compData);
					noActiveCompetences = false;
				}
				
				group1.setSelectItems(comps);
				this.competenceItems.add(group1);
			}
		}
	}

	public void prepareAddToCompetenceForActivity(final String link, final String title, final String context) {
		reset();
		
		if (title != null && link != null) {
			newPostData = new NewPostData();
			newPostData.setTitle(title);
			newPostData.setText(title + " " + link);
			newPostData.setLink(link);
			
			this.context = context;
			
        	postAction.fetchLinkContents(newPostData);
			
			// init goals
			initializeCompetences();
			
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	actionLogger.logServiceUse(
            			ComponentName.ADD_TO_COMPETENCE_DIALOG, 
            			"context", context,
            			"link", link,
            			"title", title);
	            }
			});
		}
	}
	
	public void prepareAddToCompetenceForActivity(final Activity activity, final String context) {
		reset();
		
		if (activity != null) {
			this.activity = new ActivityData(goalManager.merge(activity));
			this.context = context;
			
			// init goals
			initializeCompetences();
			
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	actionLogger.logServiceUse(
            			ComponentName.ADD_TO_COMPETENCE_DIALOG, 
            			"context", context,
            			"activityId", String.valueOf(activity.getId()));
	            }
			});
		}
	}
	
	public void addToCompetence() {
		// blank is selected
		if (selectedCompetence == -1) {
			PageUtil.fireErrorMessage("addToCompetenceDialogGrowl", "You must choose one competence.");
			return;
		}
		
		CompetenceDataCache compData = findChosenCompetence();
		
		if (activity != null) {
			try {
				TargetActivity newActivity = goalManager.addActivityToTargetCompetence(loggedUser.getUser(),
						compData.getData().getId(), 
						activity.getId(),
						context);
				
				logger.debug("Activity \""+activity.getTitle()+"\" ("+newActivity.getId()+
						") connected to the target competence \""+
						compData.getData().getId()+"\" of the user "+ loggedUser.getUser() );
				
				PageUtil.fireSuccessfulInfoMessage("Activity "+activity.getTitle()+ " is added!");
			} catch (EventException e) {
				logger.error(e);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		} else if (compData != null) {
			CompWallBean compWallBean = PageUtil.getViewScopedBean("compwall", CompWallBean.class);
			
			if (compWallBean == null) {
				long targetCompId = compData.getData().getId();
				String title = newPostData.getTitle();
				
				// clean html tags
				String description = StringUtil.cleanHtml(newPostData.getText());
				
				try {
					@SuppressWarnings("unused")
					TargetActivity newActivity = goalManager.createActivityAndAddToTargetCompetence(
							loggedUser.getUser(),
							title, 
							description,
							newPostData.getAttachmentPreview(),
							newPostData.getVisibility(),
							targetCompId,
							false,
							context);
					
					PageUtil.fireSuccessfulInfoMessage("Created new activity "+title+".");
				} catch (EventException e) {
					logger.error(e);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			} else if (newPostData != null) {
				compWallBean.createNewActivity(newPostData, compData, context);
			}
		}
	}
	
	private void reset() {
		this.selectedCompetence = -1;
		this.newPostData = null;
		this.activity = null;
		this.competenceItems = null;
		this.toUpdateAfterAdding = "";
		this.context = null;
	}
	
	private CompetenceDataCache findChosenCompetence() {
		for (CompetenceDataCache compData : competencesData) {
			if (compData.getData().getId() == selectedCompetence) {
				return compData;
			}
		}
		return null;
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
	public NewPostData getNewPostData() {
		return newPostData;
	}

	public void setNewPostData(NewPostData newPostData) {
		this.newPostData = newPostData;
	}
	
	public long getSelectedCompetence() {
		return selectedCompetence;
	}

	public void setSelectedCompetence(long selectedCompetence) {
		this.selectedCompetence = selectedCompetence;
	}

	public List<SelectItem> getCompetenceItems() {
		return competenceItems;
	}

	public ActivityData getActivity() {
		return activity;
	}

	public AttachmentPreview getAttachmentPreview() {
		return attachmentPreview;
	}

	public String getToUpdateAfterAdding() {
		return toUpdateAfterAdding;
	}

	public void setToUpdateAfterAdding(String toUpdateAfterAdding) {
		this.toUpdateAfterAdding = toUpdateAfterAdding;
	}

	public boolean isNoActiveCompetences() {
		return noActiveCompetences;
	}
	
}
