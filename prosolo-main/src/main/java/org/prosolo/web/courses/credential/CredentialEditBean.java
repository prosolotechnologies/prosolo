package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.search.CompetenceTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.data.LearningResourceAssessmentSettings;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.LearningResourceLearningStage;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.ApplicationBean;
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
public class CredentialEditBean extends CompoundLearningResourceAssessmentSettingsBean implements Serializable {

	private static final long serialVersionUID = 3430513767875001534L;

	private static Logger logger = Logger.getLogger(CredentialEditBean.class);

	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CompetenceTextSearch compTextSearch;
	@Inject private Activity1Manager activityManager;
	@Inject private LoggingService loggingService;
	@Inject private CredentialUserPrivilegeBean visibilityBean;
	@Inject private UnitManager unitManager;
	@Inject private ApplicationBean applicationBean;
	@Inject private OrganizationManager organizationManager;

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

	private List<Long> unitIds;

	private List<CredentialCategoryData> categories;

	private String context;

	private LearningStageData nextStageToBeCreated;
	//this is the last stage for which credential is created;
	private LearningResourceLearningStage lastCreatedStage;

	private BlindAssessmentMode[] blindAssessmentModes;

	public void init() {
		initializeValues();

		try {
			blindAssessmentModes = BlindAssessmentMode.values();
			if (id == null) {
				credentialData = new CredentialData(false);
				//if it is new resource, it can only be original credential, delivery can never be created from this page
				credentialData.setType(CredentialType.Original);
				credentialData.addLearningStages(organizationManager.getOrganizationLearningStagesForLearningResource(
						loggedUser.getOrganizationId()));
				credentialData.setAssessmentTypes(getAssessmentTypes());
			} else {
				decodedId = idEncoder.decodeId(id);
				setContext();
				logger.info("Editing credential with id " + decodedId);

				loadCredentialData(decodedId);
			}

			loadAssessmentData();
			loadCredentialCategories();
		} catch(Exception e) {
			logger.error("Error", e);
			credentialData = new CredentialData(false);
			PageUtil.fireErrorMessage("Error loading the page");
		}
	}

	private void loadCredentialCategories() {
		categories = organizationManager.getOrganizationCredentialCategoriesData(loggedUser.getOrganizationId());
	}

	@Override
	public boolean isLimitedEdit() {
		return isDelivery();
	}

	@Override
	public LearningResourceAssessmentSettings getAssessmentSettings() {
		return credentialData.getAssessmentSettings();
	}

	@Override
	public List<Long> getAllUnitsResourceIsConnectedTo() {
		if (decodedId > 0) {
			return unitIds;
		} else {
			//if new credential is being created, we return units where credential creator is added as manager
			return unitManager.getUserUnitIdsInRole(loggedUser.getUserId(), SystemRoleNames.MANAGER);
		}
	}

	@Override
	public boolean isPointBasedResource() {
		return isPointBasedResource(credentialData.getAssessmentSettings().getGradingMode(), credentialData.getAssessmentSettings().getRubricId(), credentialData.getAssessmentSettings().getRubricType());
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
			access = credentialManager.getResourceAccessData(id, loggedUser.getUserId(),
					ResourceAccessRequirements.of(AccessMode.MANAGER).addPrivilege(UserGroupPrivilege.Edit));

			if (!access.isCanAccess()) {
				PageUtil.accessDenied();
			} else {
				credentialData = credentialManager.getCredentialDataForEdit(id);
				List<CompetenceData1> comps = credentialData.getCompetences();
				for(CompetenceData1 cd : comps) {
					compsToExcludeFromSearch.add(cd.getCompetenceId());
				}
				currentNumberOfComps = comps.size();

				//load units credential is connected to
				unitIds = unitManager.getAllUnitIdsCredentialIsConnectedTo(decodedId);
				
				logger.info("Loaded credential data for credential with id "+ id);
			}
		} catch(ResourceNotFoundException rnfe) {
			credentialData = new CredentialData(false);
			PageUtil.fireErrorMessage("Credential can not be found");
		}
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
		unitIds = new ArrayList<>();
		nextStageToBeCreated = null;
		lastCreatedStage = null;
	}

	public boolean hasMoreCompetences(int index) {
		return credentialData.getCompetences().size() != index + 1;
	}
	
	public boolean isCompetenceCreator(CompetenceData1 comp) {
		return comp.getCreator() == null ? false : 
			comp.getCreator().getId() == loggedUser.getUserId();
	}

	public boolean isLearningInStagesEnabled() {
		return applicationBean.getConfig().application.pluginConfig.learningInStagesPlugin.enabled;
	}

	public boolean isLearningStageActive(LearningResourceLearningStage ls) {
		return ls.getLearningStage().getId() == credentialData.getLearningStage().getId();
	}

	public LearningStageData getFirstStage() {
		if (!credentialData.getLearningStages().isEmpty()) {
			return credentialData.getLearningStages().get(0).getLearningStage();
		}
		return null;
	}

	public LearningStageData getNextStageToBeCreated() {
		if (nextStageToBeCreated == null) {
			Optional<LearningResourceLearningStage> lStage = credentialData.getLearningStages()
					.stream()
					.filter(ls -> ls.isCanBeCreated())
					.findFirst();

			if (lStage.isPresent()) {
				nextStageToBeCreated = lStage.get().getLearningStage();
			}
		}
		return nextStageToBeCreated;
	}

	public LearningResourceLearningStage getLastCreatedStage() {
		if (lastCreatedStage == null) {
			ListIterator<LearningResourceLearningStage> it = credentialData.getLearningStages().listIterator(credentialData.getLearningStages().size());
			while (it.hasPrevious()) {
				LearningResourceLearningStage ls = it.previous();
				if (ls.getLearningResourceId() > 0) {
					lastCreatedStage = ls;
					break;
				}
			}
		}
		return lastCreatedStage;
	}

	/**
	 * Returns true if disabling learning in stages would influence other credentials too, so
	 * that those credentials would also have learning in stages disabled.
	 * @return
	 */
	public boolean areOtherCredentialsInfluencedByUpdate() {
		return credentialData.getIdData().getId() > 0 && credentialData.isLearningStageEnabledChanged()
				&& !credentialData.isLearningStageEnabled() && otherStagesDefined();
	}

	private boolean otherStagesDefined() {
		return credentialData.getLearningStages()
				.stream()
				.anyMatch(ls -> ls.getLearningResourceId() > 0 && ls.getLearningResourceId() != credentialData.getIdData().getId());
	}

	/*
	 * ACTIONS
	 */

	public void enableLearningStagesChecked() {
		if (credentialData.isLearningStageEnabled()) {
			//first stage should be set for new credentials and those which did not have stages enabled before
			LearningStageData ls = credentialData.getIdData().getId() == 0 || credentialData.isLearningStageEnabledChanged()
					? credentialData.getLearningStages().get(0).getLearningStage()
					: credentialData.getLearningStageBeforeUpdate();
			credentialData.setLearningStage(ls);
		} else {
			credentialData.setLearningStage(null);
		}
	}

	public void createNextStageCredentialBasic() {
		createNextStageCredential(false);
	}

	public void createNextStageCredentialFull() {
		createNextStageCredential(true);
	}

	private void createNextStageCredential(boolean full) {
		try {
			long id = credentialManager.createCredentialInLearningStage(
					getLastCreatedStage().getLearningResourceId(),
					getNextStageToBeCreated().getId(),
					full,
					loggedUser.getUserContext());

			PageUtil.redirect("/manage/credentials/" + idEncoder.encodeId(id) + "/edit");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error creating the credential for the " + getNextStageToBeCreated().getTitle() + " stage");
		}
	}
	
	public void saveAndNavigateToCreateCompetence() {
		boolean saved = saveCredentialData(false);
		if (saved) {
			PageUtil.redirect("/manage/competences/new?credId=" + id);
		}
	}

	public void save() {
		boolean isCreateUseCase = credentialData.getIdData().getId() == 0;
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
			if (credentialData.getIdData().getId() > 0) {
				credentialData.getCompetences().addAll(compsToRemove);
				if(credentialData.hasObjectChanged()) {
					credentialManager.updateCredential(credentialData, loggedUser.getUserContext());
				}
			} else {
				Credential1 cred = credentialManager.saveNewCredential(credentialData, loggedUser.getUserContext());
				credentialData.getIdData().setId(cred.getId());
				decodedId = credentialData.getIdData().getId();
				id = idEncoder.encodeId(decodedId);
				credentialData.setVersion(cred.getVersion());
				credentialData.startObservingChanges();
				setContext();
			}
			if (reloadData && credentialData.hasObjectChanged()) {
				reloadCredential();
			}
			PageUtil.fireSuccessfulInfoMessage("Changes have been saved");
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
			if (credentialData.getIdData().getId() > 0) {
				reloadCredential();
			}
			return false;
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
			return false;
		}
	}
	
	private void reloadCredential() {
		initializeValues();
		loadCredentialData(decodedId);
	}
	
	
	public void archive() {
		try {
			credentialManager.archiveCredential(credentialData.getIdData().getId(), loggedUser.getUserContext());
			credentialData.setArchived(true);
			PageUtil.fireSuccessfulInfoMessageAcrossPages("The " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " has been archived");
			PageUtil.redirect("/manage/library/credentials");
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error archiving the " + ResourceBundleUtil.getMessage("label.credential").toLowerCase());
		}
	}
	
	public void restore() {
		try {
			credentialManager.restoreArchivedCredential(credentialData.getIdData().getId(), loggedUser.getUserContext());
			credentialData.setArchived(false);
			PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " has been restored");
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error restoring the " + ResourceBundleUtil.getMessage("label.credential").toLowerCase());
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
			if(credentialData.getIdData().getId() > 0 && isDelivery()) {
				credentialManager.deleteDelivery(credentialData.getIdData().getId(), loggedUser.getUserContext());
				credentialData = new CredentialData(false);

				String growlMessage = "The " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase() + " has been deleted";
				PageUtil.fireSuccessfulInfoMessageAcrossPages(growlMessage);
				PageUtil.redirect("/manage/library");
			}
		} catch (StaleDataException sde) {
			logger.error(sde);
			PageUtil.fireErrorMessage("Delete failed because "+ResourceBundleUtil.getMessage("label.credential").toLowerCase()+" has been edited in the meantime. Please review those changes and try again");
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
			PageUtil.fireErrorMessage("There are students that started learning this "+ResourceBundleUtil.getMessage("label.credential").toLowerCase()+" so it cannot be deleted");
		}
	}
	
//	public void bcc() {
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
//				PageContextData lcd = new PageContextData(page, learningContext, service);
//				
//        		HashMap<String, String> parameters = new HashMap<String, String>();
//        		parameters.put("bcc", "true");
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
					loggedUser.getOrganizationId(), loggedUser.getUserId(), compSearchTerm, 0, 1000, false,
					unitIds, toExclude, SortingOption.ASC);
					
			List<CompetenceData1> comps = searchResponse.getFoundNodes();
			if(comps != null) {
				compSearchResults = comps;
			}
			
			String page = FacesContext.getCurrentInstance().getViewRoot().getViewId();
			PageContextData lcd = new PageContextData(page, context, null);
			Map<String, String> params = new HashMap<>();
			params.put("query", compSearchTerm);
			try {
				loggingService.logServiceUse(loggedUser.getUserContext(lcd),
						ComponentName.SEARCH_COMPETENCES, null, params, loggedUser.getIpAddress());
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
		return credentialData.getIdData().getId() > 0 ? credentialData.getIdData().getTitle() : "New " + ResourceBundleUtil.getMessage("label.credential");
	}
	
	public boolean isCreateUseCase() {
		return credentialData.getIdData().getId() == 0;
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

	public List<CredentialCategoryData> getCategories() {
		return categories;
	}

	public BlindAssessmentMode[] getBlindAssessmentModes() {
		return blindAssessmentModes;
	}
}
