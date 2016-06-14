package org.prosolo.web.courses.competence;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.services.common.exception.CompetenceEmptyException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
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

	private String id;
	private String credId;
	private long decodedId;
	private long decodedCredId;
	
	private boolean addToCredential;
	
	private CompetenceData1 competenceData;
	private List<ActivityData> activitiesToRemove;
	private List<ActivityData> activitySearchResults;
	private String activitySearchTerm;
	private List<Long> activitiesToExcludeFromSearch;
	private int currentNumberOfActivities;
	private int activityForRemovalIndex;
	
	private PublishedStatus[] compStatusArray;
	
	private String credTitle;
	
	public void init() {
		try {
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
				loadCompetenceData(decodedId);
			}
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
		} catch(Exception e) {
			logger.error(e);
			competenceData = new CompetenceData1(false);
			PageUtil.fireErrorMessage("Error while loading competence data");
		}
	}
	
	private void loadCompetenceData(long id) {
		String section = PageUtil.getSectionForView();
		if("/manage".equals(section)) {
			competenceData = compManager
					.getCurrentVersionOfCompetenceForManager(id, false, true);
		} else {
			competenceData = compManager.getCompetenceDataForEdit(id, 
					loggedUser.getUser().getId(), true);
		}
		
		if(competenceData == null) {
			competenceData = new CompetenceData1(false);
			PageUtil.fireErrorMessage("Competence data can not be found");
		}
		List<ActivityData> activities = competenceData.getActivities();
		for(ActivityData bad : activities) {
			activitiesToExcludeFromSearch.add(bad.getActivityId());
		}
		currentNumberOfActivities = activities.size();
		
		logger.info("Loaded competence data for competence with id "+ id);
	}

	private void initializeValues() {
		activitiesToRemove = new ArrayList<>();
		activitiesToExcludeFromSearch = new ArrayList<>();
		compStatusArray = PublishedStatus.values();
	}

	public boolean hasMoreActivities(int index) {
		return competenceData.getActivities().size() != index + 1;
	}
	
	/*
	 * ACTIONS
	 */
	
	public void preview() {
		saveCompetenceData(true, true);
	}
	
	public void saveAndNavigateToCreateActivity() {
		boolean saved = saveCompetenceData(true, false);
		if(saved) {
			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				StringBuilder builder = new StringBuilder();
				/*
				 * this will not work if there are multiple levels of directories in current view path
				 * example: /credentials/create-credential will return /credentials as a section but this
				 * may not be what we really want.
				 */
				String section = PageUtil.getSectionForView();
				logger.info("SECTION " + section);
				builder.append(extContext.getRequestContextPath() + section + "/competences/" + id + "/newActivity");

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
		boolean saved = saveCompetenceData(false, !addToCredential);
		if(saved && addToCredential) {
			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				/*
				 * this will not work if there are multiple levels of directories in current view path
				 * example: /credentials/create-credential will return /credentials as a section but this
				 * may not be what we really want.
				 */
				String section = PageUtil.getSectionForView();
				logger.info("SECTION " + section);
				extContext.redirect(extContext.getRequestContextPath() + section +
						"/credentials/" + credId +"/edit?compAdded=true");
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	public boolean saveCompetenceData(boolean saveAsDraft, boolean reloadData) {
		try {
			if(competenceData.getCompetenceId() > 0) {
				competenceData.getActivities().addAll(activitiesToRemove);
				if(competenceData.hasObjectChanged()) {
					if(saveAsDraft) {
						competenceData.setStatus(PublishedStatus.DRAFT);
					}
					compManager.updateCompetence(decodedId, competenceData, 
							loggedUser.getUser());
				}
			} else {
				if(saveAsDraft) {
					competenceData.setStatus(PublishedStatus.DRAFT);
				}
				long credentialId = addToCredential ? decodedCredId : 0;
				//competenceData.setDuration(4);
				Competence1 comp = compManager.saveNewCompetence(competenceData, 
						loggedUser.getUser(), credentialId);
				competenceData.setCompetenceId(comp.getId());
				decodedId = competenceData.getCompetenceId();
				id = idEncoder.encodeId(decodedId);
			}
			if(reloadData && competenceData.hasObjectChanged()) {
				initializeValues();
				loadCompetenceData(decodedId);
			}
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			return true;
		} catch(DbConnectionException | CompetenceEmptyException e) {
			logger.error(e);
			//e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
			return false;
		}
	}
	
	public void delete() {
		try {
			if(competenceData.getCompetenceId() > 0) {
				/*
				 * passing decodedId because we need to pass id of
				 * original competence and not id of a draft version
				 */
				compManager.deleteCompetence(decodedId, competenceData, loggedUser.getUser());
				competenceData = new CompetenceData1(false);
				PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			} else {
				PageUtil.fireErrorMessage("Competence is not saved so it can't be deleted");
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
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
		
		//when activity order changes update status to draft
		competenceData.setStatus(PublishedStatus.DRAFT);
	}
	
	public void removeActivity() {
		removeActivity(activityForRemovalIndex);
		
		//when activity is removed update status to draft
		competenceData.setStatus(PublishedStatus.DRAFT);
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
		return competenceData.getCompetenceId() > 0 ? "Edit Competence" : "New Competence";
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
