package org.prosolo.web.courses.competence;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.CompetenceEmptyException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageSection;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "competenceEditBean")
@Component("competenceEditBean")
@Scope("view")
public class CompetenceEditBean implements Serializable {

	private static final long serialVersionUID = -2951979198939808173L;

	private static Logger logger = Logger.getLogger(CompetenceEditBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialManager credManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private ContextJsonParserService contextParser;
	@Inject private CompetenceVisibilityBean visibilityBean;

	private String id;
	private String credId;
	private long decodedId;
	private long decodedCredId;
	
	private boolean addToCredential;
	
	private CompetenceData1 competenceData;
	private ResourceAccessData access;
	private List<ActivityData> activitiesToRemove;
	private List<ActivityData> activitySearchResults;
	private String activitySearchTerm;
	private List<Long> activitiesToExcludeFromSearch;
	private int currentNumberOfActivities;
	private int activityForRemovalIndex;
	
	private PublishedStatus[] compStatusArray;
	
	private String credTitle;
	
	private String context;
	
	private boolean manageSection;
	
	public void init() {
		try {
			manageSection = PageSection.MANAGE.equals(PageUtil.getSectionForView());
			initializeValues();
			decodedCredId = idEncoder.decodeId(credId);
			if(id == null) {
				competenceData = new CompetenceData1(false);
				if(decodedCredId > 0) {
					addToCredential = true;
				}
			} else {
				decodedId = idEncoder.decodeId(id);
				logger.info("Editing competence with id " + decodedId);
				loadCompetenceData(decodedCredId, decodedId);
			}
			setContext();
			if(decodedCredId > 0) {
				Optional<CredentialData> res = competenceData.getCredentialsWithIncludedCompetence()
						.stream().filter(cd -> cd.getId() == decodedCredId).findFirst();
				if(res.isPresent()) {
					credTitle = res.get().getTitle();
				} else {
					credTitle = credManager.getCredentialTitle(decodedCredId);
					CredentialData cd = new CredentialData(false);
					cd.setId(decodedCredId);
					cd.setTitle(credTitle);
					competenceData.getCredentialsWithIncludedCompetence().add(cd);
				}
			}
			initializeStatuses();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			competenceData = new CompetenceData1(false);
			PageUtil.fireErrorMessage("Error while loading competence data");
		}
	}
	
	private void unpackResult(RestrictedAccessResult<CompetenceData1> res) {
		competenceData = res.getResource();
		access = res.getAccess();
	}
	
	//competence is draft when it has never been published or when date of first publish is null
	public boolean isDraft() {
		return competenceData.getDatePublished() == null;
	}
	
	/**
	 * if this method returns true only limited edits are allowed
	 * 
	 * @return
	 */
	public boolean isLimitedEdit() {
		//if competence was once published 'big' changes are not allowed
		return competenceData.getDatePublished() != null;
	}
	
	public void initVisibilityManageData() {
		visibilityBean.init(decodedId, competenceData.getCreator(), manageSection);
	}
	
	private void setContext() {
		if(decodedCredId > 0) {
			context = "name:CREDENTIAL|id:" + decodedCredId;
		}
		if(decodedId > 0) {
			context = contextParser.addSubContext(context, "name:COMPETENCE|id:" + decodedId);
		}
	}
	
	private void loadCompetenceData(long credId, long id) {
		try {
			AccessMode mode = manageSection ? AccessMode.MANAGER : AccessMode.USER;
			RestrictedAccessResult<CompetenceData1> res = compManager.getCompetenceForEdit(credId, id, 
					loggedUser.getUserId(), mode);
			unpackResult(res);
			if(!access.isCanAccess()) {
				PageUtil.accessDenied();
			} else {
				List<ActivityData> activities = competenceData.getActivities();
				for(ActivityData bad : activities) {
					activitiesToExcludeFromSearch.add(bad.getActivityId());
				}
				currentNumberOfActivities = activities.size();
				
				logger.info("Loaded competence data for competence with id "+ id);
			}
		} catch(ResourceNotFoundException rnfe) {
			competenceData = new CompetenceData1(false);
			PageUtil.fireErrorMessage("Competency can not be found");
		}
	}

	private void initializeValues() {
		activitiesToRemove = new ArrayList<>();
		activitiesToExcludeFromSearch = new ArrayList<>();
	}
	
	private void initializeStatuses() {
		compStatusArray = Arrays.stream(PublishedStatus.values()).filter(
				s -> shouldIncludeStatus(s)).toArray(PublishedStatus[]::new);
	}
	
	private boolean shouldIncludeStatus(PublishedStatus status) {
		//draft status should not be included when competence is published or unpublished
		if(status == PublishedStatus.DRAFT && (competenceData.getStatus() == PublishedStatus.PUBLISHED
				|| competenceData.getStatus() == PublishedStatus.UNPUBLISHED)) {
			return false;
		}
		//unpublished status should not be included when competence is draft
		if(status == PublishedStatus.UNPUBLISHED && competenceData.getStatus() == PublishedStatus.DRAFT) {
			return false;
		}
		return true;
	}

	public boolean hasMoreActivities(int index) {
		return competenceData.getActivities().size() != index + 1;
	}
	
	/*
	 * ACTIONS
	 */
	
//	public void preview() {
//		saveCompetenceData(true);
//	}
	
	public void saveAndNavigateToCreateActivity() {
		boolean saved = saveCompetenceData(false);
		if(saved) {
			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				StringBuilder builder = new StringBuilder();
				/*
				 * this will not work if there are multiple levels of directories in current view path
				 * example: /credentials/create-credential will return /credentials as a section but this
				 * may not be what we really want.
				 */
				builder.append(extContext.getRequestContextPath() + PageUtil.getSectionForView().getPrefix() 
						+ "/competences/" + id + "/newActivity");
				
				if(credId != null && !credId.isEmpty()) {
					builder.append("?credId=" + credId);
				}
				extContext.redirect(builder.toString());
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	public void save() {
		boolean saved = saveCompetenceData(!addToCredential);
		if(saved && addToCredential) {
			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				/*
				 * this will not work if there are multiple levels of directories in current view path
				 * example: /credentials/create-credential will return /credentials as a section but this
				 * may not be what we really want.
				 */
				extContext.redirect(extContext.getRequestContextPath() + PageUtil.getSectionForView().getPrefix() +
						"/credentials/" + credId +"/edit?compAdded=true");
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	public boolean saveCompetenceData(boolean reloadData) {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			String learningContext = context;
			if(lContext != null && !lContext.isEmpty()) {
				learningContext = contextParser.addSubContext(context, lContext);
			}
			LearningContextData lcd = new LearningContextData(page, learningContext, service);
			if(competenceData.getCompetenceId() > 0) {
				competenceData.getActivities().addAll(activitiesToRemove);
				if(competenceData.hasObjectChanged()) {
					compManager.updateCompetence(competenceData, 
							loggedUser.getUserId(), lcd);
				}
			} else {
				long credentialId = addToCredential ? decodedCredId : 0;
				//competenceData.setDuration(4);
				Competence1 comp = compManager.saveNewCompetence(competenceData, 
						loggedUser.getUserId(), credentialId, lcd);
				competenceData.setCompetenceId(comp.getId());
				decodedId = competenceData.getCompetenceId();
				id = idEncoder.encodeId(decodedId);
				competenceData.setVersion(comp.getVersion());
				competenceData.startObservingChanges();
				setContext();
			}
			if(reloadData && competenceData.hasObjectChanged()) {
				initializeValues();
				loadCompetenceData(decodedCredId, decodedId);
				initializeStatuses();
			}
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			return true;
		} catch(StaleDataException sde) {
			logger.error(sde);
			PageUtil.fireErrorMessage("Update failed because competency is edited in the meantime. Please review changed competency and try again.");
			//reload data
			reloadCompetence();
			return false;
		} catch(IllegalDataStateException idse) {
			logger.error(idse);
			PageUtil.fireErrorMessage(idse.getMessage());
			if (competenceData.getCompetenceId() > 0) {
		        //reload data
		        reloadCompetence();
			}
			return false;
		} catch(DbConnectionException e) {
			logger.error(e);
			//e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
			return false;
		}
	}
	
//	public void delete() {
//		try {
//			if(competenceData.getCompetenceId() > 0) {
//				/*
//				 * passing decodedId because we need to pass id of
//				 * original competence and not id of a draft version
//				 */
//				compManager.deleteCompetence(competenceData, loggedUser.getUserId());
//				competenceData = new CompetenceData1(false);
//				PageUtil.fireSuccessfulInfoMessage("Changes are saved");
//			} else {
//				PageUtil.fireErrorMessage("Competence is not saved so it can't be deleted");
//			}
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			PageUtil.fireErrorMessage(e.getMessage());
//		}
//	}
	
	public void archive() {
		LearningContextData ctx = PageUtil.extractLearningContextData();
		try {
			compManager.archiveCompetence(decodedId, loggedUser.getUserId(), ctx);
			competenceData.setArchived(true);
			PageUtil.fireSuccessfulInfoMessage("Competency archived successfully");
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to archive competency");
		}
	}
	
	public void restore() {
		LearningContextData ctx = PageUtil.extractLearningContextData();
		try {
			compManager.restoreArchivedCompetence(decodedId, loggedUser.getUserId(), ctx);
			competenceData.setArchived(false);
			PageUtil.fireSuccessfulInfoMessage("Competency restored successfully");
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to restore competency");
		}
	}
	
	public void duplicate() {
		LearningContextData ctx = PageUtil.extractLearningContextData();
		try {
			long compId = compManager.duplicateCompetence(decodedId, 
					loggedUser.getUserId(), ctx);
			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				extContext.redirect(extContext.getRequestContextPath() + "/manage/competences/" 
						+ idEncoder.encodeId(compId) + "/edit");
			} catch (IOException e) {
				logger.error(e);
			}
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to duplicate competence");
		} catch(EventException ee) {
			logger.error(ee);
		}
	}
	
	private void reloadCompetence() {
		try {
			AccessMode mode = manageSection ? AccessMode.MANAGER : AccessMode.USER;
			RestrictedAccessResult<CompetenceData1> res = compManager.getCompetenceForEdit(decodedCredId, decodedId, 
					loggedUser.getUserId(), mode);
			unpackResult(res);
		} catch(DbConnectionException e) {
			logger.error(e);
		}
	}
	
//	public void addActivity(BasicActivityData actData) {
//		List<BasicActivityData> activities = competenceData.getActivities();
//		BasicActivityData removedAct = getActivityIfPreviouslyRemoved(actData);
//		BasicActivityData actToEdit = removedAct != null ? removedAct : actData;
//		actToEdit.setOrder(activities.size() + 1);
//		activities.add(actToEdit);
//		if(removedAct != null) {
//			removedAct.statusBackFromRemovedTransition();
//		} else {
//			actData.startObservingChanges();
//		}
//		activitiesToExcludeFromSearch.add(actToEdit.getActivityId());
//		currentNumberOfActvities ++;
//		activitySearchResults = new ArrayList<>();
//	}
	
//	private BasicActivityData getActivityIfPreviouslyRemoved(BasicActivityData actData) {
//		Iterator<BasicActivityData> iter = activitiesToRemove.iterator();
//		while(iter.hasNext()) {
//			BasicActivityData bad = iter.next();
//			if(bad.getId() == actData.getId()) {
//				iter.remove();
//				return bad;
//			}
//		}
//		return null;
//	}
	
	public void moveDown(int index) {
		moveActivity(index, index + 1);
	}
	
	public void moveUp(int index) {
		moveActivity(index - 1, index);
	}
	
	public void moveActivity(int i, int k) {
		List<ActivityData> activities = competenceData.getActivities();
		ActivityData bad1 = activities.get(i);
		bad1.setOrder(bad1.getOrder() + 1);
		bad1.statusChangeTransitionBasedOnOrderChange();
		ActivityData bad2 = activities.get(k);
		bad2.setOrder(bad2.getOrder() - 1);
		bad2.statusChangeTransitionBasedOnOrderChange();
		Collections.swap(activities, i, k);
	}
	
	public void removeActivity() {
		removeActivity(activityForRemovalIndex);
	}
	
	public void removeActivity(int index) {
		ActivityData bad = competenceData.getActivities().remove(index);
		bad.statusRemoveTransition();
		if(bad.getObjectStatus() == ObjectStatus.REMOVED) {
			activitiesToRemove.add(bad);
		}
		currentNumberOfActivities--;
		long actId = bad.getActivityId();
		removeIdFromExcludeList(actId);
		shiftOrderUpFromIndex(index);
	}
	
	private void shiftOrderUpFromIndex(int index) {
		List<ActivityData> activities = competenceData.getActivities();
		for(int i = index; i < currentNumberOfActivities; i++) {
			ActivityData bad = activities.get(i);
			bad.setOrder(bad.getOrder() - 1);
			bad.statusChangeTransitionBasedOnOrderChange();
		}
	}

	private void removeIdFromExcludeList(long compId) {
		Iterator<Long> iterator = activitiesToExcludeFromSearch.iterator();
		while(iterator.hasNext()) {
			long id = iterator.next();
			if(id == compId) {
				iterator.remove();
				return;
			}
		}
	}
	 
	public String getPageHeaderTitle() {
		return competenceData.getCompetenceId() > 0 ? competenceData.getTitle() : "New Competence";
	}
	
	public boolean isCreateUseCase() {
		return competenceData.getCompetenceId() == 0;
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

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public CompetenceData1 getCompetenceData() {
		return competenceData;
	}

	public void setCompetenceData(CompetenceData1 competenceData) {
		this.competenceData = competenceData;
	}

	public List<ActivityData> getActivitySearchResults() {
		return activitySearchResults;
	}

	public void setActivitySearchResults(List<ActivityData> activitySearchResults) {
		this.activitySearchResults = activitySearchResults;
	}

	public String getActivitySearchTerm() {
		return activitySearchTerm;
	}

	public void setActivitySearchTerm(String activitySearchTerm) {
		this.activitySearchTerm = activitySearchTerm;
	}

	public int getCurrentNumberOfActivities() {
		return currentNumberOfActivities;
	}

	public void setCurrentNumberOfActivities(int currentNumberOfActivities) {
		this.currentNumberOfActivities = currentNumberOfActivities;
	}

	public PublishedStatus[] getCompStatusArray() {
		return compStatusArray;
	}

	public void setCompStatusArray(PublishedStatus[] compStatusArray) {
		this.compStatusArray = compStatusArray;
	}

	public int getActivityForRemovalIndex() {
		return activityForRemovalIndex;
	}

	public void setActivityForRemovalIndex(int activityForRemovalIndex) {
		this.activityForRemovalIndex = activityForRemovalIndex;
	}

	public String getCredTitle() {
		return credTitle;
	}

	public void setCredTitle(String credTitle) {
		this.credTitle = credTitle;
	}

}
