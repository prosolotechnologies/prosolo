package org.prosolo.web.courses.competence;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.services.assessment.data.LearningResourceAssessmentSettings;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialIdData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.competence.util.LearningPathDescription;
import org.prosolo.web.courses.credential.CompoundLearningResourceAssessmentSettingsBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageSection;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

@ManagedBean(name = "competenceEditBean")
@Component("competenceEditBean")
@Scope("view")
public class CompetenceEditBean extends CompoundLearningResourceAssessmentSettingsBean implements Serializable {

	private static final long serialVersionUID = -2951979198939808173L;

	private static Logger logger = Logger.getLogger(CompetenceEditBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialManager credManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UnitManager unitManager;

	private String id;
	private String credId;
	private long decodedId;
	private long decodedCredId;
	
	private CompetenceData1 competenceData;
	private ResourceAccessData access;
	private List<ActivityData> activitiesToRemove;
	private List<ActivityData> activitySearchResults;
	private String activitySearchTerm;
	private List<Long> activitiesToExcludeFromSearch;
	private int currentNumberOfActivities;
	private int activityForRemovalIndex;
	
	private PublishedStatus[] compStatusArray;

	private LearningPathDescription[] learningPaths;
	
	private CredentialIdData credentialIdData;

	private BlindAssessmentMode[] blindAssessmentModes;

	private String context;
	
	private boolean manageSection;
	
	public void init() {
		try {
			manageSection = PageSection.MANAGE.equals(PageUtil.getSectionForView());
			initializeValues();
			blindAssessmentModes = BlindAssessmentMode.values();
			decodedCredId = idEncoder.decodeId(credId);

			if (id == null) {
				competenceData = new CompetenceData1(false);
				competenceData.setAssessmentTypes(getAssessmentTypes());
			} else {
				decodedId = idEncoder.decodeId(id);
				logger.info("Editing competence with id " + decodedId);
				loadCompetenceData(decodedCredId, decodedId);
			}
			setContext();

			if (decodedCredId > 0) {
				Optional<CredentialIdData> res = competenceData.getCredentialsWithIncludedCompetence()
						.stream().filter(id -> id.getId() == decodedCredId).findFirst();
				if (res.isPresent()) {
					credentialIdData = res.get();
				} else {
					credentialIdData = credManager.getCredentialIdData(decodedCredId, null);
					//we add passed credential to parent credentials only if new competency is being created
					if (id == null) {
						CredentialIdData idData = new CredentialIdData(false);
						idData.setId(decodedCredId);
						idData.setTitle(credentialIdData.getTitle());
						competenceData.getCredentialsWithIncludedCompetence().add(idData);
					}
				}
			}
			loadAssessmentData();
			initializeStatuses();
			learningPaths = LearningPathDescription.values();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			competenceData = new CompetenceData1(false);
			PageUtil.fireErrorMessage("Error loading competence data");
		}
	}
	
	private void unpackResult(RestrictedAccessResult<CompetenceData1> res) {
		competenceData = res.getResource();
		access = res.getAccess();
	}

	@Override
	/**
	 * if this method returns true only limited edits are allowed
	 *
	 * @return
	 */
	public boolean isLimitedEdit() {
		//if competence was once published 'big' changes are not allowed
		return competenceData.getDatePublished() != null;
	}

	@Override
	public LearningResourceAssessmentSettings getAssessmentSettings() {
		return competenceData.getAssessmentSettings();
	}

	@Override
	public List<Long> getAllUnitsResourceIsConnectedTo() {
		if (decodedId > 0) {
			return unitManager.getAllUnitIdsCompetenceIsConnectedTo(competenceData.getCompetenceId());
		} else {
			//if new competence is being created, we return units where competence creator is added as manager
			return unitManager.getUserUnitIdsInRole(loggedUser.getUserId(), SystemRoleNames.MANAGER);
		}
	}

	@Override
	public boolean isPointBasedResource() {
		return isPointBasedResource(competenceData.getAssessmentSettings().getGradingMode(), competenceData.getAssessmentSettings().getRubricId(), competenceData.getAssessmentSettings().getRubricType());
	}
	
	//competence is draft when it has never been published or when date of first publish is null
	public boolean isDraft() {
		return competenceData.getDatePublished() == null;
	}
	
//	public void initVisibilityManageData() {
//		visibilityBean.init(decodedId, competenceData.getCreator(), manageSection);
//	}
	
	private void setContext() {
		if(decodedCredId > 0) {
			context = "name:CREDENTIAL|id:" + decodedCredId;
		}
		if(decodedId > 0) {
			context = ContextJsonParserService.addSubContext(context, "name:COMPETENCE|id:" + decodedId);
		}
	}
	
	private void loadCompetenceData(long credId, long id) {
		try {
			AccessMode mode = manageSection ? AccessMode.MANAGER : AccessMode.USER;
			RestrictedAccessResult<CompetenceData1> res = compManager.getCompetenceForEdit(credId, id, 
					loggedUser.getUserId(), mode);
			unpackResult(res);
			if (!access.isCanAccess()) {
				PageUtil.accessDenied();
			} else {
				List<CredentialIdData> credentialsWithCompetence = credManager
						.getCredentialsWithIncludedCompetenceBasicData(id, CredentialType.Original);
				competenceData.getCredentialsWithIncludedCompetence().addAll(credentialsWithCompetence);
				List<ActivityData> activities = competenceData.getActivities();
				for (ActivityData bad : activities) {
					activitiesToExcludeFromSearch.add(bad.getActivityId());
				}
				currentNumberOfActivities = activities.size();
				
				logger.info("Loaded competence data for competence with id "+ id);
			}
		} catch (ResourceNotFoundException rnfe) {
			PageUtil.notFound();
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
	
	public void saveAndNavigateToCreateActivity() {
		// if someone wants to edit activity, he certainly didn't mean to publish the competence at that point. Thus,
		// we will manually set field 'published 'to false
		competenceData.setPublished(false);
		boolean saved = saveCompetenceData(false);

		if (saved) {
			PageUtil.redirect("/manage/credentials/" + credId + "/competences/" + id + "/activities/new");
		}
	}
	
	public void save() {
		boolean isCreateUseCase = competenceData.getCompetenceId() == 0;
		boolean saved = saveCompetenceData(!isCreateUseCase);

		if (saved && isCreateUseCase) {
			PageUtil.keepFiredMessagesAcrossPages();

			PageUtil.redirect("/manage/credentials/" + credId + "/edit?tab=competences");
		}
	}
	
	public boolean saveCompetenceData(boolean reloadData) {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			String learningContext = context;
			if (lContext != null && !lContext.isEmpty()) {
				learningContext = ContextJsonParserService.addSubContext(context, lContext);
			}
			PageContextData lcd = new PageContextData(page, learningContext, service);
			if (competenceData.getCompetenceId() > 0) {
				competenceData.getActivities().addAll(activitiesToRemove);
				if (competenceData.hasObjectChanged()) {
					compManager.updateCompetence(competenceData, loggedUser.getUserContext(lcd));

					if (reloadData) {
						initializeValues();
						loadCompetenceData(decodedCredId, decodedId);
						initializeStatuses();
					}
				}

				PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			} else {
				long credentialId = decodedCredId;
				Competence1 comp = compManager.saveNewCompetence(competenceData, credentialId, loggedUser.getUserContext(lcd));
				competenceData.setCompetenceId(comp.getId());
				decodedId = competenceData.getCompetenceId();
				id = idEncoder.encodeId(decodedId);
				competenceData.setVersion(comp.getVersion());
				competenceData.startObservingChanges();
				setContext();
			}
			if (reloadData && competenceData.hasObjectChanged()) {
				initializeValues();
				loadCompetenceData(decodedCredId, decodedId);
				initializeStatuses();
			}

			return true;
		} catch (StaleDataException sde) {
			logger.error(sde);
			PageUtil.fireErrorMessage("Update failed because the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase() + " has been edited in the meantime. Please review those changes and try again");
			//reload data
			initializeValues();
			loadCompetenceData(decodedCredId, decodedId);
			initializeStatuses();
			return false;
		} catch (IllegalDataStateException idse) {
			logger.error(idse);
			PageUtil.fireErrorMessage(idse.getMessage());
			if (competenceData.getCompetenceId() > 0) {
		        //reload data
				initializeValues();
				loadCompetenceData(decodedCredId, decodedId);
				initializeStatuses();
			}
			return false;
		} catch (DbConnectionException e) {
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
		try {
			compManager.archiveCompetence(decodedId, loggedUser.getUserContext());
			competenceData.setArchived(true);
			PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.competence").toLowerCase() + " has been archived");
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error archiving the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
		}
	}
	
	public void restore() {
		try {
			compManager.restoreArchivedCompetence(decodedId, loggedUser.getUserContext());
			competenceData.setArchived(false);
			PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.competence").toLowerCase() + " has been restored");
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error restoring the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
		}
	}
	
	public void duplicate() {
		try {
			long compId = compManager.duplicateCompetence(decodedId, loggedUser.getUserContext());

			PageUtil.redirect("/manage/credentials/" + credId + "/competences/" + idEncoder.encodeId(compId) + "/edit");
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error duplicating the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
		}
	}
	
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
		return competenceData.getCompetenceId() > 0 ? competenceData.getTitle() : "New " + ResourceBundleUtil.getMessage("label.competence");
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
		return credentialIdData.getTitle();
	}

	public long getDecodedCredId() {
		return decodedCredId;
	}

	public LearningPathDescription[] getLearningPaths() {
		return learningPaths;
	}

	public CredentialIdData getCredentialIdData() {
		return credentialIdData;
	}

	public BlindAssessmentMode[] getBlindAssessmentModes() {
		return blindAssessmentModes;
	}
}
