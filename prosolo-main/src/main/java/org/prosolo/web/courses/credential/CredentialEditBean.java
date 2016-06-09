package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.common.exception.CompetenceEmptyException;
import org.prosolo.services.common.exception.CredentialEmptyException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.search.data.SortingOption;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialEditBean")
@Component("credentialEditBean")
@Scope("view")
public class CredentialEditBean implements Serializable {

	private static final long serialVersionUID = 3430513767875001534L;

	private static Logger logger = Logger.getLogger(CredentialEditBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private TextSearch textSearch;
	@Inject private Activity1Manager activityManager;

	private String id;
	private long decodedId;
	
	private CredentialData credentialData;
	private List<CompetenceData1> compsToRemove;
	private List<CompetenceData1> compSearchResults;
	private String compSearchTerm;
	private List<Long> compsToExcludeFromSearch;
	private int currentNumberOfComps;
	private int competenceForRemovalIndex;
	
	private PublishedStatus[] courseStatusArray;
	private Role role;
	
	public void init() {
		initializeValues();
		if(id == null) {
			credentialData = new CredentialData(false);
		} else {
			try {
				decodedId = idEncoder.decodeId(id);
				logger.info("Editing credential with id " + decodedId);

				loadCredentialData(decodedId);
			} catch(Exception e) {
				logger.error(e);
				credentialData = new CredentialData(false);
				PageUtil.fireErrorMessage(e.getMessage());
			}
		}
	}
	
	private void loadCredentialData(long id) {
		String section = PageUtil.getSectionForView();
		if("/manage".equals(section)) {
			role = Role.Manager;
			credentialData = credentialManager.getCurrentVersionOfCredentialForManager(id, false, true);
		} else {
			role = Role.User;
			credentialData = credentialManager.getCredentialDataForEdit(id, 
					loggedUser.getUser().getId(), true);
		}
		
		if(credentialData == null) {
			credentialData = new CredentialData(false);
			PageUtil.fireErrorMessage("Credential data can not be found");
		}
		List<CompetenceData1> comps = credentialData.getCompetences();
		for(CompetenceData1 cd : comps) {
			compsToExcludeFromSearch.add(cd.getCompetenceId());
		}
		currentNumberOfComps = comps.size();
		
		logger.info("Loaded credential data for credential with id "+ id);
	}
	
	public void loadCompetenceActivitiesIfNotLoaded(CompetenceData1 cd) {
		if(!cd.isActivitiesInitialized()) {
			List<ActivityData> activities = new ArrayList<>();
			activities = activityManager.getCompetenceActivitiesData(cd.getCompetenceId());
			cd.setActivities(activities);
			cd.setActivitiesInitialized(true);
		}
	}

	private void initializeValues() {
		compsToRemove = new ArrayList<>();
		compsToExcludeFromSearch = new ArrayList<>();
		courseStatusArray = PublishedStatus.values();
	}

	public boolean hasMoreCompetences(int index) {
		return credentialData.getCompetences().size() != index + 1;
	}
	
	public boolean isCompetenceCreator(CompetenceData1 comp) {
		return comp.getCreator() == null ? false : 
			comp.getCreator().getId() == loggedUser.getUser().getId();
	}
	
	/*
	 * ACTIONS
	 */
	
	public void saveAndNavigateToCreateCompetence() {
		boolean saved = saveCredentialData(true, false);
		if(saved) {
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
						"/competences/new?credId=" + id);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	public void preview() {
		saveCredentialData(true, true);
	}
	
	public void save() {
		saveCredentialData(false, true);
	}
	
	public boolean saveCredentialData(boolean saveAsDraft, boolean reloadData) {
		try {
			if(credentialData.getId() > 0) {
				credentialData.getCompetences().addAll(compsToRemove);
				if(credentialData.hasObjectChanged()) {
					if(saveAsDraft) {
						credentialData.setStatus(PublishedStatus.DRAFT);
					}
					credentialManager.updateCredential(decodedId, credentialData, 
							loggedUser.getUser(), role);
				}
			} else {
				if(saveAsDraft) {
					credentialData.setStatus(PublishedStatus.DRAFT);
				}
				Credential1 cred = credentialManager.saveNewCredential(credentialData, 
						loggedUser.getUser());
				credentialData.setId(cred.getId());
				decodedId = credentialData.getId();
				id = idEncoder.encodeId(decodedId);
			}
			if(reloadData && credentialData.hasObjectChanged()) {
				initializeValues();
				loadCredentialData(decodedId);
			}
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			return true;
		} catch(DbConnectionException | CredentialEmptyException | CompetenceEmptyException e) {
			logger.error(e);
			//e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
			return false;
		}
	}
	
	public void delete() {
		try {
			if(credentialData.getId() > 0) {
				/*
				 * decoded id is passed because we want to pass id of original version
				 * and not draft
				 */
				credentialManager.deleteCredential(decodedId, credentialData, loggedUser.getUser());
				credentialData = new CredentialData(false);
				PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			} else {
				PageUtil.fireErrorMessage("Credential is not saved so it can't be deleted");
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void searchCompetences() {
		compSearchResults = new ArrayList<>();
		if(compSearchTerm != null && !compSearchTerm.isEmpty()) {
			int size = compsToExcludeFromSearch.size();
			long [] toExclude = new long[size];
			for(int i = 0; i < size; i++) {
				toExclude[i] = compsToExcludeFromSearch.get(i);
			}
			TextSearchResponse1<CompetenceData1> searchResponse = textSearch.searchCompetences1(
					compSearchTerm,
					0, 
					Integer.MAX_VALUE,
					false,
					toExclude,
					null,
					SortingOption.ASC);
			
			List<CompetenceData1> comps = searchResponse.getFoundNodes();
			if(comps != null) {
				compSearchResults = comps;
			}
		} 
	}
	
	public void addComp(CompetenceData1 compData) {
		List<CompetenceData1> competences = credentialData.getCompetences();
		CompetenceData1 removedComp = getCompetenceIfPreviouslyRemoved(compData);
		CompetenceData1 compToEdit = removedComp != null ? removedComp : compData;
		compToEdit.setOrder(competences.size() + 1);
		competences.add(compToEdit);
		credentialData.setDuration(compToEdit.getDuration() + compToEdit.getDuration());
		if(removedComp != null) {
			removedComp.statusBackFromRemovedTransition();
		} else {
			compData.setObjectStatus(ObjectStatus.CREATED);
			compData.startObservingChanges();
		}
		compsToExcludeFromSearch.add(compToEdit.getCompetenceId());
		currentNumberOfComps ++;
		compSearchResults = new ArrayList<>();
		//change status because competence is added
		credentialData.setStatus(PublishedStatus.DRAFT);
	}
	
	private CompetenceData1 getCompetenceIfPreviouslyRemoved(CompetenceData1 compData) {
		Iterator<CompetenceData1> iter = compsToRemove.iterator();
		while(iter.hasNext()) {
			CompetenceData1 cd = iter.next();
			if(cd.getCompetenceId() == compData.getCompetenceId()) {
				iter.remove();
				return cd;
			}
		}
		return null;
	}

	public void moveDown(int index) {
		moveComp(index, index + 1);
	}
	
	public void moveUp(int index) {
		moveComp(index - 1, index);
	}
	
	public void moveComp(int i, int k) {
		List<CompetenceData1> competences = credentialData.getCompetences();
		CompetenceData1 cd1 = competences.get(i);
		cd1.setOrder(cd1.getOrder() + 1);
		cd1.statusChangeTransitionBasedOnOrderChange();
		CompetenceData1 cd2 = competences.get(k);
		cd2.setOrder(cd2.getOrder() - 1);
		cd2.statusChangeTransitionBasedOnOrderChange();
		Collections.swap(competences, i, k);
		
		//set status to draft because order changed
		credentialData.setStatus(PublishedStatus.DRAFT);
	}
	
	public void removeComp() {
		removeComp(competenceForRemovalIndex);
		
		//set status to draft because competence is removed
		credentialData.setStatus(PublishedStatus.DRAFT);
	}
	
	public void removeComp(int index) {
		CompetenceData1 cd = credentialData.getCompetences().remove(index);
		credentialData.setDuration(credentialData.getDuration() - cd.getDuration());
		cd.statusRemoveTransition();
		if(cd.getObjectStatus() == ObjectStatus.REMOVED) {
			compsToRemove.add(cd);
		}
		currentNumberOfComps--;
		long compId = cd.getCompetenceId();
		removeIdFromExcludeList(compId);
		shiftOrderOfCompetencesUp(index);
	}
	
	private void shiftOrderOfCompetencesUp(int index) {
		List<CompetenceData1> competences = credentialData.getCompetences();
		for(int i = index; i < currentNumberOfComps; i++) {
			CompetenceData1 comp = competences.get(i);
			comp.setOrder(comp.getOrder() - 1);
			comp.statusChangeTransitionBasedOnOrderChange();
		}
	}

	private void removeIdFromExcludeList(long compId) {
		Iterator<Long> iterator = compsToExcludeFromSearch.iterator();
		while(iterator.hasNext()) {
			long id = iterator.next();
			if(id == compId) {
				iterator.remove();
				return;
			}
		}
	}
	
//	public void listener(AjaxBehaviorEvent event) {
//	     System.out.println("listener");
//	     System.out.println(credentialData.isMandatoryFlow());
//	}
	 
	public String getPageHeaderTitle() {
		return credentialData.getId() > 0 ? "Edit Credential" : "New Credential";
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public PublishedStatus[] getCourseStatusArray() {
		return courseStatusArray;
	}

	public void setCourseStatusArray(PublishedStatus[] courseStatusArray) {
		this.courseStatusArray = courseStatusArray;
	}

	public CredentialData getCredentialData() {
		return credentialData;
	}

	public void setCredentialData(CredentialData credentialData) {
		this.credentialData = credentialData;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<CompetenceData1> getCompSearchResults() {
		return compSearchResults;
	}

	public void setCompSearchResults(List<CompetenceData1> compSearchResults) {
		this.compSearchResults = compSearchResults;
	}

	public String getCompSearchTerm() {
		return compSearchTerm;
	}

	public void setCompSearchTerm(String compSearchTerm) {
		this.compSearchTerm = compSearchTerm;
	}

	public int getCurrentNumberOfComps() {
		return currentNumberOfComps;
	}

	public void setCurrentNumberOfComps(int currentNumberOfComps) {
		this.currentNumberOfComps = currentNumberOfComps;
	}
	
	public int getCompetenceForRemovalIndex() {
		return competenceForRemovalIndex;
	}

	public void setCompetenceForRemovalIndex(int competenceForRemovalIndex) {
		this.competenceForRemovalIndex = competenceForRemovalIndex;
	}
}
