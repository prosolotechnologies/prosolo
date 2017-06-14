package org.prosolo.web.courses.activity;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.ScoreCalculation;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.EventException;
import org.prosolo.services.htmlparser.HTMLParser;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ResourceLinkData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageSection;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "activityEditBean")
@Component("activityEditBean")
@Scope("view")
public class ActivityEditBean implements Serializable {

	private static final long serialVersionUID = 7678126570859694510L;

	private static Logger logger = Logger.getLogger(ActivityEditBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Competence1Manager compManager;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UploadManager uploadManager;
	@Inject private HTMLParser htmlParser;
	@Inject private CredentialManager credManager;
	@Inject private ContextJsonParserService contextParser;

	private String id;
	private String compId;
	private String credId;
	private long decodedId;
	private long decodedCompId;
	private long decodedCredId;
	
	private ActivityData activityData;
	private ResourceAccessData access;
	private String competenceName;
	private ResourceLinkData resLinkToAdd;
	private String credentialTitle;
	
	private ActivityType[] activityTypes;
	
	private ActivityResultType[] resultTypes;
	
	private String context;
	
	private boolean manageSection;
	
	public void init() {
		manageSection = PageSection.MANAGE.equals(PageUtil.getSectionForView());
		initializeValues();
		decodedCredId = idEncoder.decodeId(credId);
		try {
			if(compId == null) {
				try {
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
				} catch (IOException ioe) {
					ioe.printStackTrace();
					logger.error(ioe);
				}
			} else {
				decodedCompId = idEncoder.decodeId(compId);
				if(id == null) {
					activityData = new ActivityData(false);
				} else {
					decodedId = idEncoder.decodeId(id);
					logger.info("Editing activity with id " + decodedId);
					loadActivityData(decodedCredId, decodedCompId, decodedId);
				}
				setContext();
				activityData.setCompetenceId(decodedCompId);
				loadCompAndCredTitle();
			}
		} catch(Exception e) {
			logger.error(e);
			activityData = new ActivityData(false);
			PageUtil.fireErrorMessage(e.getMessage());
		}
		
	}
	
	private void unpackResult(RestrictedAccessResult<ActivityData> res) {
		activityData = res.getResource();
		access = res.getAccess();
	}
	
	public boolean isLimitedEdit() {
		//if competence with this activity was once published, only limited edit is allowed
		return activityData.isOncePublished();
	}
	
	private void setContext() {
		if(decodedCredId > 0) {
			context = "name:CREDENTIAL|id:" + decodedCredId;
		}
		if(decodedCompId > 0) {
			context = contextParser.addSubContext(context, "name:COMPETENCE|id:" + decodedCompId);
		}
		if(decodedId > 0) {
			context = contextParser.addSubContext(context, "name:ACTIVITY|id:" + decodedId);
		}
	}
	
	private void initializeValues() {
		activityTypes = ActivityType.values();
		resultTypes = ActivityResultType.values();
	}

	private void loadCompAndCredTitle() {
		competenceName = compManager.getCompetenceTitle(activityData.getCompetenceId());
		activityData.setCompetenceName(competenceName);
		
		if(credId != null) {
			credentialTitle = credManager.getCredentialTitle(idEncoder.decodeId(credId));
		}
	}

	private void loadActivityData(long credId, long compId, long actId) {
		try {
			AccessMode mode = manageSection ? AccessMode.MANAGER : AccessMode.USER;
			ResourceAccessRequirements req = ResourceAccessRequirements.of(mode)
					.addPrivilege(UserGroupPrivilege.Edit);
			RestrictedAccessResult<ActivityData> res = activityManager.getActivityData(credId, compId, actId, 
					loggedUser.getUserId(), true, req);
			unpackResult(res);
			
			if(!access.isCanAccess()) {
				try {
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/accessDenied.xhtml");
				} catch (IOException e) {
					logger.error(e);
				}
			} else {
				logger.info("Loaded activity data for activity with id "+ id);
			}
		} catch(ResourceNotFoundException rnfe) {
			logger.error(rnfe);
			activityData = new ActivityData(false);
			PageUtil.fireErrorMessage("Activity data can not be found");
		}
	}
	
	public ScoreCalculation[] getScoreCalculationTypes() {
		return ScoreCalculation.values();
	}
	
	public boolean isLinkListEmpty() {
		List<ResourceLinkData> links = activityData.getLinks();
		if(links == null || links.isEmpty()) {
			return true;
		}
		for(ResourceLinkData rl : links) {
			if(rl.getStatus() != ObjectStatus.REMOVED) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isFileListEmpty() {
		List<ResourceLinkData> files = activityData.getFiles();
		if(files == null || files.isEmpty()) {
			return true;
		}
		for(ResourceLinkData rl : files) {
			if(rl.getStatus() != ObjectStatus.REMOVED) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isCaptionListEmpty() {
		List<ResourceLinkData> captions = activityData.getCaptions();
		if(captions == null || captions.isEmpty()) {
			return true;
		}
		for(ResourceLinkData rl : captions) {
			if(rl.getStatus() != ObjectStatus.REMOVED) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isActivityTypeSelected(ActivityType type) {
		return type == activityData.getActivityType();
	}
	
	public void updateType(ActivityType type) {
//		ActivityType oldType = activityData.getActivityType();
//		if(oldType == ActivityType.SLIDESHARE || oldType == ActivityType.VIDEO) {
//			 activityData.setLink(null);
//		}
		activityData.setActivityType(type);
	}
	
	public void removeFile(ResourceLinkData link) {
		link.statusRemoveTransition();
		if(link.getStatus() != ObjectStatus.REMOVED) {
			activityData.getFiles().remove(link);
		}
	}
	
	public void removeLink(ResourceLinkData link) {
		link.statusRemoveTransition();
		if(link.getStatus() != ObjectStatus.REMOVED) {
			activityData.getLinks().remove(link);
		}
	}
	
	public void removeCaption(ResourceLinkData caption) {
		caption.statusRemoveTransition();
		if(caption.getStatus() != ObjectStatus.REMOVED) {
			activityData.getCaptions().remove(caption);
		}
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		
		try {
			String fileName = uploadedFile.getFileName();
			String fullPath = uploadManager.storeFile(uploadedFile, fileName);
			
			resLinkToAdd.setUrl(fullPath);
			resLinkToAdd.setFetchedTitle(fileName);
			//activityData.getFiles().add(rl);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
	}
	
	public void addUploadedFile() {
		if(resLinkToAdd.getUrl() == null || resLinkToAdd.getUrl().isEmpty() 
				|| resLinkToAdd.getLinkName() == null || resLinkToAdd.getLinkName().isEmpty()) {
			FacesContext.getCurrentInstance().validationFailed();
			resLinkToAdd.setUrlInvalid(resLinkToAdd.getUrl() == null || 
					resLinkToAdd.getUrl().isEmpty());
			resLinkToAdd.setLinkNameInvalid(resLinkToAdd.getLinkName() == null || 
					resLinkToAdd.getLinkName().isEmpty());
		} else {
			activityData.getFiles().add(resLinkToAdd);
			resLinkToAdd = null;
		}
	}
	
	public void addLink() {
		resLinkToAdd.setUrl(StringUtil.encodeUrl(resLinkToAdd.getUrl()));
		activityData.getLinks().add(resLinkToAdd);
		resLinkToAdd = null;
	}
	
	public void addUploadedCaption() {
		if(resLinkToAdd.getUrl() == null || resLinkToAdd.getUrl().isEmpty() 
				|| resLinkToAdd.getLinkName() == null || resLinkToAdd.getLinkName().isEmpty()
				|| !resLinkToAdd.getFetchedTitle().endsWith(".srt")) {
			FacesContext.getCurrentInstance().validationFailed();
			resLinkToAdd.setUrlInvalid(resLinkToAdd.getUrl() == null || 
					resLinkToAdd.getUrl().isEmpty() || !resLinkToAdd.getFetchedTitle().endsWith(".srt"));
			resLinkToAdd.setLinkNameInvalid(resLinkToAdd.getLinkName() == null || 
					resLinkToAdd.getLinkName().isEmpty());
		} else {
			activityData.getCaptions().add(resLinkToAdd);
			resLinkToAdd = null;
		}
	}
	
	public void fetchLinkTitle() {
		try {
			Map<String, String> params = FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap();
			String link =  params.get("link");
			String linkTitle = params.get("title");
			
			if (linkTitle == null || linkTitle.isEmpty()) {
				String encodedLink = StringUtil.encodeUrl(link);
				String pageTitle = htmlParser.getPageTitle(encodedLink);
				resLinkToAdd.setLinkName(pageTitle);
			} else {
				resLinkToAdd.setLinkName(linkTitle);
			}
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public void prepareAddingResourceLink() {
		resLinkToAdd = new ResourceLinkData();
		resLinkToAdd.setStatus(ObjectStatus.CREATED);
	}
	
	public boolean isCreateUseCase() {
		return activityData != null ? activityData.getActivityId() == 0 : false;
	}
	
	/*
	 * ACTIONS
	 */
	
//	public void preview() {
//		saveActivityData(true);
//	}
	
	public void save() {
		boolean isNew = activityData.getActivityId() == 0;
		boolean saved = saveActivityData(!isNew);
		
		if (saved && isNew) {
			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				/*
				 * this will not work if there are multiple levels of directories in current view path
				 * example: /credentials/create-credential will return /credentials as a section but this
				 * may not be what we really want.
				 */
				StringBuilder url = new StringBuilder(extContext.getRequestContextPath() + PageUtil.getSectionForView().getPrefix() +
						"/competences/" + compId + "/edit?tab=activities");
				if (credId != null && !credId.isEmpty()) {
					url.append("&credId=" + credId);
				}
				extContext.redirect(url.toString());
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	public boolean saveActivityData(boolean reloadData) {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			String learningContext = context;
			
			if (lContext != null && !lContext.isEmpty()) {
				learningContext = contextParser.addSubContext(context, lContext);
			}
			
			LearningContextData lcd = new LearningContextData(page, learningContext, service);
			if (activityData.getActivityId() > 0) {
				if (activityData.hasObjectChanged()) {
					activityManager.updateActivity(activityData, loggedUser.getUserId(), lcd);
				}
			} else {
				Activity1 act = activityManager.saveNewActivity(activityData, loggedUser.getUserId(), lcd);
				decodedId = act.getId();
				id = idEncoder.encodeId(decodedId);
				activityData.startObservingChanges();
				
				setContext();
			}
			
			if(reloadData && activityData.hasObjectChanged()) {
				//reload data
				loadActivityData(decodedCredId, decodedCompId, decodedId);
				activityData.setCompetenceName(competenceName);
			}
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			return true;
		} catch(EventException ee) {
			logger.error(ee);
			return true;
		} catch(DbConnectionException|IllegalDataStateException|StaleDataException e) {
			logger.error(e);
			e.printStackTrace();
			/*
			 * to be able to check in oncomplete event if action executed successfully or not.
			 */
			FacesContext.getCurrentInstance().validationFailed();
			PageUtil.fireErrorMessage(e.getMessage());
			return false;
		}
	}
	
	public void delete() {
		try {
			if(activityData.getActivityId() > 0) {
				activityManager.deleteActivity(decodedId, loggedUser.getUserId());
				//activityData = new ActivityData(false);
				//PageUtil.fireSuccessfulInfoMessage("Changes are saved");
				ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
				try {
					/*
					 * this will not work if there are multiple levels of directories in current view path
					 * example: /credentials/create-credential will return /credentials as a section but this
					 * may not be what we really want.
					 */
					StringBuilder url = new StringBuilder(extContext.getRequestContextPath() + PageUtil.getSectionForView().getPrefix() +
							"/competences/" + compId + "/edit?tab=activities");
					if (credId != null && !credId.isEmpty()) {
						url.append("&credId=" + credId);
					}
					extContext.redirect(url.toString());
				} catch (IOException e) {
					logger.error(e);
				}
			} else {
				PageUtil.fireErrorMessage("Activity is not saved so it can't be deleted");
			}
		} catch(DbConnectionException|IllegalDataStateException e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	 
	public String getPageHeaderTitle() {
		return activityData.getActivityId() > 0 ? activityData.getTitle() : "New Activity";
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

	public String getCompId() {
		return compId;
	}

	public void setCompId(String compId) {
		this.compId = compId;
	}

	public ActivityData getActivityData() {
		return activityData;
	}

	public void setActivityData(ActivityData activityData) {
		this.activityData = activityData;
	}

	public ActivityType[] getActivityTypes() {
		return activityTypes;
	}

	public void setActivityTypes(ActivityType[] activityTypes) {
		this.activityTypes = activityTypes;
	}

	public ResourceLinkData getResLinkToAdd() {
		return resLinkToAdd;
	}

	public void setResLinkToAdd(ResourceLinkData resLinkToAdd) {
		this.resLinkToAdd = resLinkToAdd;
	}

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public ActivityResultType[] getResultTypes() {
		return resultTypes;
	}

	public void setResultTypes(ActivityResultType[] resultTypes) {
		this.resultTypes = resultTypes;
	}

}
