package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.search.CompetenceTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.event.EventException;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.search.data.SortingOption;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

@ManagedBean(name = "credentialEditBean")
@Component("credentialEditBean")
@Scope("view")
public class CredentialEditBean implements Serializable {

	private static final long serialVersionUID = 3430513767875001534L;

	private static Logger logger = Logger.getLogger(CredentialEditBean.class);

	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CompetenceTextSearch compTextSearch;
	@Inject private Activity1Manager activityManager;
	@Inject private LoggingService loggingService;
	@Inject private CredentialUserPrivilegeBean visibilityBean;

	private String id;
	private long decodedId;

	private CredentialData credentialData;
	private ResourceAccessData access;
	private List<CompetenceData1> compsToRemove;
	private List<CompetenceData1> compSearchResults;
	private String compSearchTerm;
	private List<Long> compsToExcludeFromSearch;
	private int currentNumberOfComps;
	private int competenceForRemovalIndex;

	private String context;

	public void init() {
		initializeValues();

		if (id == null) {
			credentialData = new CredentialData(false);
			//if it is new resource, it can only be original credential, delivery can never be created from this page
			credentialData.setType(CredentialType.Original);
		} else {
			try {
				decodedId = idEncoder.decodeId(id);
				setContext();
				logger.info("Editing credential with id " + decodedId);

				loadCredentialData(decodedId);
			} catch(Exception e) {
				logger.error(e);
				e.printStackTrace();
				credentialData = new CredentialData(false);
				PageUtil.fireErrorMessage("Error while trying to load credential data");
			}
		}
	}

	public boolean hasDeliveryStarted() {
		return credentialData.getDeliveryStartTime() >= 0 &&
				getNumberOfMillisecondsBetweenNowAndDeliveryStart() <= 0;
	}
	
	public boolean hasDeliveryEnded() {
		return credentialData.getDeliveryEndTime() >= 0 &&
				getNumberOfMillisecondsBetweenNowAndDeliveryEnd() <= 0;
	}
	
	public long getNumberOfMillisecondsBetweenNowAndDeliveryStart() {
		return credentialData.getDeliveryStartTime() >= 0
				? getDateDiffInMilliseconds(credentialData.getDeliveryStartTime(), new Date().getTime())
				: 0;
	}
	
	public long getNumberOfMillisecondsBetweenNowAndDeliveryEnd() {
		return credentialData.getDeliveryEndTime() >= 0
				? getDateDiffInMilliseconds(credentialData.getDeliveryEndTime(), new Date().getTime())
				: 0;
	}
	
	private long getDateDiffInMilliseconds(long millis1, long millis2) {
		/*
		 * if difference is bigger than one day return one day in millis to avoid bigger number than
		 * js timeout allows
		 */
		long oneDayMillis = 24 * 60 * 60 * 1000;
		long diff = millis1 - millis2;
		return diff < oneDayMillis ? diff : oneDayMillis;
	}
	
	public String getResourceTypeString() {
		return credentialData.getType() == CredentialType.Original ? "Credential" : "Delivery";
	}
	
	public boolean isOriginal() {
		return credentialData.getType() == CredentialType.Original;
	}
	
	public boolean isDelivery() {
		return credentialData.getType() == CredentialType.Delivery;
	}
	
	private void setContext() {
		if(decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedId;
		}
	}

	private void loadCredentialData(long id) {
		try {
			RestrictedAccessResult<CredentialData> res = credentialManager.getCredentialForEdit(id, 
					loggedUser.getUserId());
			unpackResult(res);
			
			if(!access.isCanAccess()) {
				PageUtil.accessDenied();
			} else {
				List<CompetenceData1> comps = credentialData.getCompetences();
				for(CompetenceData1 cd : comps) {
					compsToExcludeFromSearch.add(cd.getCompetenceId());
				}
				currentNumberOfComps = comps.size();
				
				logger.info("Loaded credential data for credential with id "+ id);
			}
		} catch(ResourceNotFoundException rnfe) {
			credentialData = new CredentialData(false);
			PageUtil.fireErrorMessage("Credential can not be found");
		}
	}
	
	private void unpackResult(RestrictedAccessResult<CredentialData> res) {
		credentialData = res.getResource();
		access = res.getAccess();
	}
	
	public void loadCompetenceActivitiesIfNotLoaded(CompetenceData1 cd) {
		try {
			if(!cd.isActivitiesInitialized()) {
				List<ActivityData> activities = new ArrayList<>();
				activities = activityManager.getCompetenceActivitiesData(cd.getCompetenceId());
				cd.setActivities(activities);
				cd.setActivitiesInitialized(true);
			}
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading activities");
		}
	}

	private void initializeValues() {
		compsToRemove = new ArrayList<>();
		compsToExcludeFromSearch = new ArrayList<>();
	}

	public boolean hasMoreCompetences(int index) {
		return credentialData.getCompetences().size() != index + 1;
	}
	
	public boolean isCompetenceCreator(CompetenceData1 comp) {
		return comp.getCreator() == null ? false : 
			comp.getCreator().getId() == loggedUser.getUserId();
	}
	
	/*
	 * ACTIONS
	 */
	
	public void saveAndNavigateToCreateCompetence() {
		boolean saved = saveCredentialData(false);
		if (saved) {
			PageUtil.redirect("/manage/competences/new?credId=" + id);
		}
	}

	public void save() {
		boolean isCreateUseCase = credentialData.getId() == 0;
		boolean saved = saveCredentialData(!isCreateUseCase);

		//redirect to credential edit page if credential is saved for the first time
		if (saved && isCreateUseCase) {
			PageUtil.keepFiredMessagesAcrossPages();
			//when competence is saved for the first time redirect to edit page
			PageUtil.redirect("/manage/credentials/" + id + "/edit");
		}
	}
	
	public boolean saveCredentialData(boolean reloadData) {
		try {
			LearningContextData lcd = PageUtil.extractLearningContextData();
			
			if (credentialData.getId() > 0) {
				credentialData.getCompetences().addAll(compsToRemove);
				if(credentialData.hasObjectChanged()) {
					credentialManager.updateCredential(credentialData, loggedUser.getUserId(), lcd);
				}
			} else {
				Credential1 cred = credentialManager.saveNewCredential(credentialData, loggedUser.getUserId(), lcd);
				credentialData.setId(cred.getId());
				decodedId = credentialData.getId();
				id = idEncoder.encodeId(decodedId);
				credentialData.setVersion(cred.getVersion());
				credentialData.startObservingChanges();
				setContext();
			}
			if (reloadData && credentialData.hasObjectChanged()) {
				reloadCredential();
			}
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			return true;
		} catch (StaleDataException sde) {
			logger.error(sde);
			PageUtil.fireErrorMessage("Update failed because credential is edited in the meantime. Please review changed credential and try again.");
			//reload data
			reloadCredential();
			return false;
		} catch (IllegalDataStateException idse) {
			logger.error(idse);
			PageUtil.fireErrorMessage(idse.getMessage());
			if (credentialData.getId() > 0) {
				reloadCredential();
			}
			return false;
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
			return false;
		} catch (EventException e) {
			logger.error(e);
			return false;
		}
	}
	
	private void reloadCredential() {
		initializeValues();
		loadCredentialData(decodedId);
	}
	
	
	public void archive() {
		LearningContextData ctx = PageUtil.extractLearningContextData();
		try {
			credentialManager.archiveCredential(credentialData.getId(), loggedUser.getUserId(), ctx);
			credentialData.setArchived(true);
			PageUtil.fireSuccessfulInfoMessage("Credential archived successfully");
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to archive credential");
		}
	}
	
	public void restore() {
		LearningContextData ctx = PageUtil.extractLearningContextData();
		try {
			credentialManager.restoreArchivedCredential(credentialData.getId(), loggedUser.getUserId(), ctx);
			credentialData.setArchived(false);
			PageUtil.fireSuccessfulInfoMessage("Credential restored successfully");
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to restore credential");
		}
	}
	
//	public void cancelScheduledUpdate() {
//		String page = PageUtil.getPostParameter("page");
//		String lContext = PageUtil.getPostParameter("learningContext");
//		String service = PageUtil.getPostParameter("service");
//		String learningContext = context;
//		if(lContext != null && !lContext.isEmpty()) {
//			learningContext = contextParser.addSubContext(context, lContext);
//		}
//		Credential1 cr = new Credential1();
//		cr.setId(decodedId);
//		try {
//			eventFactory.generateEvent(EventType.CANCEL_SCHEDULED_VISIBILITY_UPDATE, 
//					loggedUser.getUserId(), cr, null, page, learningContext, 
//					service, null);
//			credentialData.setScheduledPublishDate(null);
//			credentialData.setCredentialStatus();
//		} catch(Exception e) {
//			e.printStackTrace();
//			logger.error(e);
//		}
//	}
	
	public void delete() {
		try {
			if(credentialData.getId() > 0 && isDelivery()) {
				credentialManager.deleteDelivery(credentialData.getId(), loggedUser.getUserId());
				credentialData = new CredentialData(false);
				try {
					String growlMessage = ResourceBundleUtil.getMessage("label.credential") + " " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase() + " deleted";
					PageUtil.fireSuccessfulInfoMessageAcrossPages(growlMessage);
				} catch (KeyNotFoundInBundleException e) {
					logger.error(e);
				}
				PageUtil.redirect("/manage/library");
			}
		} catch (StaleDataException sde) {
			logger.error(sde);
			PageUtil.fireErrorMessage("Delete failed because credential is edited in the meantime. Please review changed credential and try again.");
			//reload data
			reloadCredential();
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		} catch (DataIntegrityViolationException div) {
			//if integrity rule is violated it is due to students already started learning, so they have a reference to this delivery
			logger.error(div);
			div.printStackTrace();
			PageUtil.fireErrorMessage("There are students that started learning this credential so it cannot be deleted");
		} catch (EventException ee) {
			logger.error(ee);
		}
	}
	
//	public void duplicate() {
//
//		Credential1 cred = credentialCloneFactory.clone(credentialData.getId());
//		
//		// if we enable Duplicate button for the regular user (and not just Manager), path should be changed
//		PageUtil.redirect(CommonSettings.getInstance().config.appConfig.domain + "manage/credentials/" + idEncoder.encodeId(cred.getId()) + "/edit");
//		
//		String page = PageUtil.getPostParameter("page");
//		String lContext = PageUtil.getPostParameter("learningContext");
//		String service = PageUtil.getPostParameter("service");
//
//		taskExecutor.execute(() -> {
//			try {
//				String learningContext = context;
//				if (lContext != null && !lContext.isEmpty()) {
//					learningContext = contextParser.addSubContext(context, lContext);
//				}
//				LearningContextData lcd = new LearningContextData(page, learningContext, service);
//				
//        		HashMap<String, String> parameters = new HashMap<String, String>();
//        		parameters.put("duplicate", "true");
//        		
//        		eventFactory.generateEvent(EventType.Create, loggedUser.getUserId(), cred, null, lcd.getPage(), 
//        				lcd.getLearningContext(), lcd.getService(), parameters);
//        	} catch (EventException e) {
//        		logger.error(e);
//        	}
//		});
//	}
	
	public void searchCompetences() {
		compSearchResults = new ArrayList<>();
		if(compSearchTerm != null && !compSearchTerm.isEmpty()) {
			int size = compsToExcludeFromSearch.size();
			long [] toExclude = new long[size];
			for(int i = 0; i < size; i++) {
				toExclude[i] = compsToExcludeFromSearch.get(i);
			}
			PaginatedResult<CompetenceData1> searchResponse = compTextSearch.searchCompetencesForAddingToCredential(
					loggedUser.getUserId(), compSearchTerm, 0, 1000, false, toExclude, SortingOption.ASC);
					
			List<CompetenceData1> comps = searchResponse.getFoundNodes();
			if(comps != null) {
				compSearchResults = comps;
			}
			
			String page = FacesContext.getCurrentInstance().getViewRoot().getViewId();
			LearningContextData lcd = new LearningContextData(page, context, null);
			Map<String, String> params = new HashMap<>();
			params.put("query", compSearchTerm);
			try {
				loggingService.logServiceUse(loggedUser.getUserId(), 
						ComponentName.SEARCH_COMPETENCES, 
						params, loggedUser.getIpAddress(), lcd);
			} catch(Exception e) {
				logger.error(e);
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
	}
	
	public void removeComp() {
		removeComp(competenceForRemovalIndex);
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
		return credentialData.getId() > 0 ? credentialData.getTitle() : "New Credential";
	}
	
	public boolean isCreateUseCase() {
		return credentialData.getId() == 0;
	}
	
	/*
	 * GETTERS / SETTERS
	 */

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

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}
	
}
