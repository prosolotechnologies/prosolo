package org.prosolo.web.competences;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.htmlparser.HTMLParser;
import org.prosolo.services.media.util.SlideShareUtils;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.ExternalActivityResourceData;
import org.prosolo.services.nodes.data.activity.ResourceActivityResourceData;
import org.prosolo.services.nodes.data.activity.ResourceData;
import org.prosolo.services.nodes.data.activity.ResourceType;
import org.prosolo.services.nodes.data.activity.UploadAssignmentResourceData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;
import org.prosolo.services.nodes.data.activity.mapper.ActivityMapperFactory;
import org.prosolo.services.nodes.data.activity.mapper.activityData.ActivityDataMapper;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.competences.validator.YoutubeLinkValidator;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.images.ImageSize;
import org.prosolo.web.util.images.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "competenceActivitiesBean")
@Component("competenceActivitiesBean")
@Scope("view")
public class CompetenceActivitiesBean implements Serializable {

	private static final long serialVersionUID = -6145577969728042249L;

	private static Logger logger = Logger.getLogger(CompetenceActivitiesBean.class);

	@Autowired private CompetenceManager competenceManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private TextSearch textSearch;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Inject private HTMLParser htmlParser;
	@Inject private ActivityManager activityManager;
	@Inject private UploadManager uploadManager;
	
	private String id;
	private String title;
	private long decodedId;
	
	private List<ActivityData> activities;
	
	private ActivityData activityToEdit;
	private UploadAssignmentResourceData uploadResData;
	private ExternalActivityResourceData externalResData;
	private ResourceActivityResourceData resourceResData;
	
	private ActivityData activityToEditBackup;
	
	private ResourceType[] resTypes;
	
	private String actSearchTerm;
	private List<ActivityData> searchResults;
	private List<Long> activitiesToExclude;
	
	private int currentNumberOfActivities;
	
	private String learningContext;
	
	private static String VIDEO_INPUT_COMP_ID = "formAddActivity:textFieldYoutubeLink";
	private static String SLIDESHARE_INPUT_COMP_ID = "formAddActivity:textFieldSlideshareLink";
	private static String URL_INPUT_COMP_ID = "formAddActivity:textFieldPlainUrl";
	
	public CompetenceActivitiesBean() {
		
	}
	
	public void init() {
		decodedId = idEncoder.decodeId(id);
		resTypes = ResourceType.values();
		activityToEdit = new ActivityData();
		
		if (decodedId > 0) {
			logger.info("Searching activities for competence with id: " + decodedId);
			try {
				if(title == null || "".equals(title)) {
					title = competenceManager.getCompetenceTitle(decodedId);
				}
				initializeActivities();
				logger.info("Loaded competence activities for competence with id "+ 
						decodedId);
			} catch(DbConnectionException e) {
				logger.error(e);
				PageUtil.fireErrorMessage(e.getMessage());
			}
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	private void initializeActivities() {
		activities = competenceManager.getCompetenceActivities(decodedId);
		activitiesToExclude = new ArrayList<>();
		for(ActivityData ad : activities) {
			activitiesToExclude.add(ad.getActivityId());
		}
		currentNumberOfActivities = activities.size();
	}

	public void searchActivities() {
		searchResults = new ArrayList<>();
		if(actSearchTerm != null && !actSearchTerm.isEmpty()) {
			int size = activitiesToExclude.size();
			long [] toExclude = new long[size];
			for(int i = 0; i < size; i++) {
				toExclude[i] = activitiesToExclude.get(i);
			}
			TextSearchResponse searchResponse = textSearch.searchActivities(
					actSearchTerm, 
					0, 
					Integer.MAX_VALUE, 
					false, 
					toExclude);
			
			@SuppressWarnings("unchecked")
			List<Activity> acts = (List<Activity>) searchResponse.getFoundNodes();
			if(acts != null) {
				for(Activity a : acts) {
					ActivityDataMapper mapper = ActivityMapperFactory.getActivityDataMapper(a);
					if(mapper != null) {
						ActivityData ad = mapper.mapToActivityData();
						searchResults.add(ad);
					}
				}
			}
		} 
	}
	
	public void addActivityFromSearch(ActivityData act) {
		try {
			String learningContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			String page = PageUtil.getPostParameter("page");
			LearningContextData context = new LearningContextData(page, learningContext, service);
			saveNewCompActivity(act, context);
			PageUtil.fireSuccessfulInfoMessage("Activity added");
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
		searchResults = new ArrayList<>();
	}
	
	private CompetenceActivity saveNewCompActivity(ActivityData act, LearningContextData context) {
		currentNumberOfActivities++;
		act.setOrder(currentNumberOfActivities);
		CompetenceActivity cAct = competenceManager.saveCompetenceActivity(decodedId, act, context);
		act.setCompetenceActivityId(cAct.getId());
		if(act.getActivityId() == 0) {
			act.setActivityId(cAct.getActivity().getId());
		}
		activities.add(act);
		activitiesToExclude.add(act.getActivityId());
		return cAct;
	}

	public void deleteCompActivity() {
		try {
			String service = PageUtil.getPostParameter("service");
			String page = PageUtil.getPostParameter("page");
			LearningContextData context = new LearningContextData(page, learningContext, service);
			int index = activities.indexOf(activityToEdit);
			List<ActivityData> changedActivities = shiftOrderOfActivitiesUp(index);
			competenceManager.deleteCompetenceActivity(activityToEdit, 
					changedActivities, loggedUserBean.getUser(), context);
			activities.remove(activityToEdit);
			currentNumberOfActivities--;
			activitiesToExclude.remove(new Long(activityToEdit.getActivityId()));
			PageUtil.fireSuccessfulInfoMessage("Activity deleted");
		} catch(DbConnectionException e) {
			logger.error(e);
			initializeActivities();
			PageUtil.fireErrorMessage(e.getMessage());
		}
		activityToEdit = null;
	}
	
	public void initResourceData() {
		activityToEdit.createResourceDataBasedOnResourceType();
		this.uploadResData = null;
		this.externalResData = null;
		this.resourceResData = null;
		
		ResourceData resData = activityToEdit.getResourceData();
		if(resData != null) {
			switch(resData.getActivityType()) {
				case ASSIGNMENT_UPLOAD:
					this.uploadResData = (UploadAssignmentResourceData) resData;
					return;
				case EXTERNAL_TOOL:
					this.externalResData = (ExternalActivityResourceData) resData;
					return;
				case RESOURCE:
					this.resourceResData = (ResourceActivityResourceData) resData;
					return;
			}
		}
	}
	
	public void setActivityForEdit() {
		setActivityForEdit(new ActivityData());
		activityToEdit.setMakerId(loggedUserBean.getUser().getId());
	}

	public void setActivityForEdit(ActivityData activityData) {
		this.activityToEdit = activityData;
		this.learningContext = PageUtil.getPostParameter("learningContext");
	}
	
	public CompetenceActivity saveActivity() {
		CompetenceActivity cAct = null;
		try {
			String service = PageUtil.getPostParameter("service");
			String page = PageUtil.getPostParameter("page");
			LearningContextData context = new LearningContextData(page, learningContext, service);
			cAct = saveNewCompActivity(activityToEdit, context);
			PageUtil.fireSuccessfulInfoMessage("Competence activity saved");
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
		activityToEdit = null;
		return cAct;
	}
	
	public void moveDown(int index) {
		moveActivity(index, index + 1);
	}
	
	public void moveUp(int index) {
		moveActivity(index - 1, index);
	}
	
	public void moveActivity(int i, int k) {
		ActivityData ad1 = activities.get(i);
		ad1.setOrder(ad1.getOrder() + 1);
		ActivityData ad2 = activities.get(k);
		ad2.setOrder(ad2.getOrder() - 1);
		try {
			List<ActivityData> changed = new ArrayList<>();
			changed.add(ad1);
			changed.add(ad2);
			competenceManager.updateOrderOfCompetenceActivities(changed);
			Collections.swap(activities, i, k);
		} catch(DbConnectionException e) {
			logger.error(e);
			ad1.setOrder(ad1.getOrder() - 1);
			ad2.setOrder(ad2.getOrder() + 1);
			PageUtil.fireErrorMessage(e.getMessage());
		}
		
	}
	
	private List<ActivityData> shiftOrderOfActivitiesUp(int index) {
		List<ActivityData> changedActivities = new ArrayList<>();
		for(int i = index + 1; i < currentNumberOfActivities; i++) {
			ActivityData ad = activities.get(i);
			ad.setOrder(ad.getOrder() - 1);
			changedActivities.add(ad);
		}
		return changedActivities;
	}
	
	public void addPlainUrl() {
		AttachmentPreview ap = resourceResData.getAttachmentPreview();
		UrlValidator urlValidator = new UrlValidator();
		boolean valid = urlValidator.isValid(ap.getLink());
		if(valid) {
			AttachmentPreview attachmentPreview = htmlParser.extractAttachmentPreview(StringUtil.cleanHtml(ap.getLink()));
			if(attachmentPreview != null) {
				resourceResData.setAttachmentPreview(attachmentPreview);
			} else {
				markUrlError(ap);
			}
		} else {
			markUrlError(ap);
		}
	}
	
	private void markUrlError(AttachmentPreview ap) {
		resetAttachmentPreview(ap);
		PageUtil.fireErrorMessage(URL_INPUT_COMP_ID, 
				"Url not valid");
	}

	private void resetAttachmentPreview(AttachmentPreview ap) {
		ap.setInitialized(false);
		ap.setInvalidLink(true);
		ap.setTitle(null);
		ap.setDescription(null);
		ap.setImages(new ArrayList<>());
	}
	
	public void addSlideshareLink() {
		AttachmentPreview ap = resourceResData.getAttachmentPreview();
		String embedLink = SlideShareUtils.convertSlideShareURLToEmbededUrl(ap.getLink());
		if(embedLink != null) {
			ap.setEmbedingLink(embedLink);
			ap.setInitialized(true);
		} else {
			ap.setEmbedingLink(null);
			ap.setInitialized(false);
			PageUtil.fireErrorMessage(SLIDESHARE_INPUT_COMP_ID, 
					"Url not valid");
		}
	}
	
	public void addYoutubeLink() {
		AttachmentPreview ap = resourceResData.getAttachmentPreview();
		YoutubeLinkValidator validator = new YoutubeLinkValidator(null);
		try {
			String id = (String) validator.performValidation(ap.getLink(), null);
			if(id != null) {
				ap.setId(id);
				ap.setInitialized(true);
			} else {
				ap.setId(null);
				ap.setInitialized(false);
				PageUtil.fireErrorMessage(VIDEO_INPUT_COMP_ID, 
						"Url not valid");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		
	}
	
	public void saveCompActivity() {
		switch(activityToEdit.getResourceType()) {
			case VIDEO:
			case SLIDESHARE:
				saveActivityAsync();
				return;
			case URL:
				saveResourceActivity();
				return;
			case ASSIGNMENT:
			case EXTERNAL_ACTIVITY:
			case FILE:
			case NONE:
				saveActivity();
				return;
		}
	}
	
	public CompetenceActivity saveResourceActivity() {
		try {
			AttachmentPreview ap = resourceResData.getAttachmentPreview();
			if(!ap.isInitialized()) {
				String compId = getCompId();
				if(compId != null) {
					PageUtil.fireErrorMessage(compId, 
							"Url not valid");
				}
				FacesContext context = FacesContext.getCurrentInstance();
				context.validationFailed();
			} else {
				CompetenceActivity cAct = saveActivity();
				return cAct;
			}
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
		return null;
	}
	
	private String getCompId() {
		switch(activityToEdit.getResourceType()) {
			case VIDEO:
				return VIDEO_INPUT_COMP_ID;
			case SLIDESHARE:
				return SLIDESHARE_INPUT_COMP_ID;
			case URL:
				return URL_INPUT_COMP_ID;
			default:
				return null;
		}
	}

	public void saveActivityAsync() {
			CompetenceActivity cAct = saveResourceActivity();
			if(cAct != null) {
				final RichContent richContent = ((ResourceActivity) cAct.getActivity()).getRichContent();
				
				taskExecutor.execute(new Runnable() {
					@Override
					public void run() {
						AttachmentPreview attachmentPreview = htmlParser.extractAttachmentPreview(StringUtil.cleanHtml(richContent.getLink()));
						try {
							activityManager.updateRichContent(richContent.getId(), attachmentPreview.getTitle(), 
									attachmentPreview.getDescription());
						} catch(DbConnectionException e) {
							logger.error(e);
						}
					}
				});
			}
	}
	
	public void getNextImage(){
		AttachmentPreview ap = resourceResData.getAttachmentPreview();
		int currentIndex = ap.getSelectedImageIndex();
		int size = ap.getImages().size();
		
		if (currentIndex == -1) {
			// do nothing
		} else if (currentIndex == size-1) {
			currentIndex = 0;
		} else {
			currentIndex++;
		}
		
		ap.setSelectedImageIndex(currentIndex);
		
		if (currentIndex >= 0)
			ap.setImage(ap.getImages().get(currentIndex));
	}
	
	public void getPrevImage(){
		AttachmentPreview ap = resourceResData.getAttachmentPreview();
		int currentIndex = ap.getSelectedImageIndex();
		int size = ap.getImages().size();
		
		if (currentIndex == -1) {
			// do nothing
		} else if (currentIndex == 0) {
			currentIndex = size - 1;
		} else {
			currentIndex--;
		}
		
		ap.setSelectedImageIndex(currentIndex);
		
		if (currentIndex >= 0)
			ap.setImage(ap.getImages().get(currentIndex));
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		
		try {
			AttachmentPreview attachmentPreview = uploadManager.uploadFile(loggedUserBean.getUser(), 
					uploadedFile, uploadedFile.getFileName());
			attachmentPreview.setFileIcon(ImageUtil.getFileTypeIcon(attachmentPreview.getLink(), 
					ImageSize.size0x100));
			attachmentPreview.setTitle(attachmentPreview.getUploadTitle());
			resourceResData.setAttachmentPreview(attachmentPreview);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
	}

	/*
	 * GETTERS / SETTERS
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ActivityData> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityData> activities) {
		this.activities = activities;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ActivityData getActivityToEdit() {
		return activityToEdit;
	}

	public void setActivityToEdit(ActivityData activityToEdit) {
		this.activityToEdit = activityToEdit;
	}

	public ActivityData getActivityToEditBackup() {
		return activityToEditBackup;
	}

	public void setActivityToEditBackup(ActivityData activityToEditBackup) {
		this.activityToEditBackup = activityToEditBackup;
	}

	public ResourceType[] getResTypes() {
		return resTypes;
	}

	public void setResTypes(ResourceType[] resTypes) {
		this.resTypes = resTypes;
	}

	public UploadAssignmentResourceData getUploadResData() {
		return uploadResData;
	}

	public void setUploadResData(UploadAssignmentResourceData uploadResData) {
		this.uploadResData = uploadResData;
	}

	public ExternalActivityResourceData getExternalResData() {
		return externalResData;
	}

	public void setExternalResData(ExternalActivityResourceData externalResData) {
		this.externalResData = externalResData;
	}

	public ResourceActivityResourceData getResourceResData() {
		return resourceResData;
	}

	public void setResourceResData(ResourceActivityResourceData resourceResData) {
		this.resourceResData = resourceResData;
	}

	public String getActSearchTerm() {
		return actSearchTerm;
	}

	public void setActSearchTerm(String actSearchTerm) {
		this.actSearchTerm = actSearchTerm;
	}

	public List<ActivityData> getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(List<ActivityData> searchResults) {
		this.searchResults = searchResults;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public int getCurrentNumberOfActivities() {
		return currentNumberOfActivities;
	}

	public void setCurrentNumberOfActivities(int currentNumberOfActivities) {
		this.currentNumberOfActivities = currentNumberOfActivities;
	}

	public String getLearningContext() {
		return learningContext;
	}

	public void setLearningContext(String learningContext) {
		this.learningContext = learningContext;
	}

}
