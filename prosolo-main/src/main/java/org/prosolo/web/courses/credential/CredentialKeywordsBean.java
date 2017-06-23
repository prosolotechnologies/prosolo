package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.assessments.AssessmentRequestData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ManagedBean(name = "credentialKeywordBean")
@Component("credentialKeywordBean")
@Scope("view")
public class CredentialKeywordsBean {

	private static Logger logger = Logger.getLogger(TargetCompetence1.class);

	@Inject
	private CredentialManager credentialManager;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private AssessmentManager assessmentManager;

	@Autowired
	private LoggedUserBean loggedUser;
	@Inject private UserTextSearch userTextSearch;
	@Autowired
	@Qualifier("taskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	@Autowired
	private EventFactory eventFactory;

	private String id;
	private List<TagCountData> tags;
	private List<TagCountData> selectedKeywords;
	private TagCountData lastSelected;
	private List<CompetenceData1> competences;
	private List<ActivityData> activities;
	private String chosenKeywordsString;
	private List<CompetenceData1> filteredCompetences;
	private CredentialData credentialData;
	private long numberOfUsersLearningCred;
	private AssessmentRequestData assessmentRequestData = new AssessmentRequestData();

	// used for search in the Ask for Assessment modal
	private List<UserData> peersForAssessment;
	private String peerSearchTerm;
	private List<Long> peersToExcludeFromSearch;
	private boolean noRandomAssessor = false;
	
	private long decodedId;

	public void init() {
		decodedId = idEncoder.decodeId(id);
		credentialData = credentialManager.getTargetCredentialData(decodedId,
				loggedUser.getSessionData().getUserId(), false);
		if (credentialData != null) {
			selectedKeywords = new ArrayList<>();
			filteredCompetences = new ArrayList<>();
			tags = credentialManager.getTagsForCredentialCompetences(decodedId);
			competences = credentialManager.getCompetencesForKeywordSearch(credentialData.getTargetCredId());
			activities = credentialManager.getActivitiesForKeywordSearch(credentialData.getTargetCredId());
			filterCompetences();

			logger.info("init credential keywords");
		} else {
			PageUtil.notFound();
		}

	}

	public List<CompetenceData1> getFilteredCompetences() {
		return filteredCompetences;
	}

	public String getChosenKeywordsString() {
		return AnnotationUtil.getAnnotationsAsSortedCSVForTagCountData(selectedKeywords);
	}

	public void setChosenKeywordsString(String chosenKeywordsString) {
		this.chosenKeywordsString = chosenKeywordsString;
	}

	public List<CompetenceData1> getCompetences() {
		return competences;
	}

	public List<TagCountData> getTags() {
		return tags;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public CredentialData getcredentialData() {
		return credentialData;
	}

	public void setcredentialData(CredentialData credentialData) {
		this.credentialData = credentialData;
	}

	public long getNumberOfUsersLearningCred() {
		return numberOfUsersLearningCred;
	}

	public void setNumberOfUsersLearningCred(long numberOfUsersLearningCred) {
		this.numberOfUsersLearningCred = numberOfUsersLearningCred;
	}

	public AssessmentManager getAssessmentManager() {
		return assessmentManager;
	}

	public void setAssessmentManager(AssessmentManager assessmentManager) {
		this.assessmentManager = assessmentManager;
	}

	public String getPeerSearchTerm() {
		return peerSearchTerm;
	}

	public void setPeerSearchTerm(String peerSearchTerm) {
		this.peerSearchTerm = peerSearchTerm;
	}

	public List<TagCountData> getSelectedKeywords() {
		return selectedKeywords;
	}

	public AssessmentRequestData getAssessmentRequestData() {
		return assessmentRequestData;
	}

	public void setAssessmentRequestData(AssessmentRequestData assessmentRequestData) {
		this.assessmentRequestData = assessmentRequestData;
	}

	public List<UserData> getPeersForAssessment() {
		return peersForAssessment;
	}

	public void setPeersForAssessment(List<UserData> peersForAssessment) {
		this.peersForAssessment = peersForAssessment;
	}

	public List<Long> getPeersToExcludeFromSearch() {
		return peersToExcludeFromSearch;
	}

	public void setPeersToExcludeFromSearch(List<Long> peersToExcludeFromSearch) {
		this.peersToExcludeFromSearch = peersToExcludeFromSearch;
	}

	public boolean isNoRandomAssessor() {
		return noRandomAssessor;
	}

	public void setNoRandomAssessor(boolean noRandomAssessor) {
		this.noRandomAssessor = noRandomAssessor;
	}

	public ThreadPoolTaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public EventFactory getEventFactory() {
		return eventFactory;
	}

	public void setEventFactory(EventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}
	
	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public void addKeyword(TagCountData t) {
		lastSelected = t;
		selectedKeywords.add(t);
		filterCompetences();
	}

	public List<ActivityData> getActivities() {
		List<ActivityData> filteredActivities = new ArrayList<>();
		if (!selectedKeywords.isEmpty()) {
			for (ActivityData activity : activities) {
				for (CompetenceData1 competence : filteredCompetences) {
					if (activity.getCompetenceId() == competence.getCompetenceId()) {
						filteredActivities.add(activity);
						break;
					}
				}
			}

		} else {
			filteredActivities = activities;
		}
		return filteredActivities;
	}

	public void setActivities(List<ActivityData> activities) {
		this.activities = activities;
	}

	public void filterCompetences() {
		if (!selectedKeywords.isEmpty() || !getChosenKeywordsString().equals("")) {
			filteredCompetences.clear();
			for (CompetenceData1 comp : getCompetences()) {
				for (TagCountData tag : selectedKeywords) {
					if (comp.getTagsString().contains(tag.getTitle())) {
						filteredCompetences.add(comp);
						break;
					}
				}
			}
		} else {
			filteredCompetences = new ArrayList<>(competences);
		}
	}

	public void removeTag() {
		String tagToRemove = PageUtil.getGetParameter("tag");
		for (int i = 0; i < selectedKeywords.size(); i++) {
			if (selectedKeywords.get(i).getTitle().equals(tagToRemove)) {
				selectedKeywords.remove(i);
				filterCompetences();
				break;
			}
		}
	}

	public boolean userHasAssessmentForCredential() {
		Long assessmentCount = assessmentManager.countAssessmentsForUserAndCredential(loggedUser.getUserId(),
				idEncoder.decodeId(id));
		if (assessmentCount > 0) {
			logger.debug("We found " + assessmentCount + " assessments for user " + loggedUser.getUserId()
					+ "for credential" + idEncoder.decodeId(id));
			return true;
		}
		return false;
	}

	public String getAssessmentIdForUser() {
		return idEncoder.encodeId(
				assessmentManager.getAssessmentIdForUser(loggedUser.getUserId(), credentialData.getTargetCredId()));
	}

	public void searchCredentialPeers() {
		if (peerSearchTerm == null && peerSearchTerm.isEmpty()) {
			peersForAssessment = null;
		} else {
			try {
				if (peersToExcludeFromSearch == null) {
					peersToExcludeFromSearch = credentialManager
							.getAssessorIdsForUserAndCredential(credentialData.getId(), loggedUser.getUserId());
					peersToExcludeFromSearch.add(loggedUser.getUserId());
				}

				TextSearchResponse1<UserData> result = userTextSearch.searchPeersWithoutAssessmentRequest(
						peerSearchTerm, 3, idEncoder.decodeId(id), peersToExcludeFromSearch);
				peersForAssessment = result.getFoundNodes();
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	public void chooseRandomPeerForAssessor() {
		resetAskForAssessmentModal();

		UserData randomPeer = credentialManager.chooseRandomPeer(credentialData.getId(), loggedUser.getUserId());

		if (randomPeer != null) {
			assessmentRequestData.setAssessorId(randomPeer.getId());
			assessmentRequestData.setAssessorFullName(randomPeer.getFullName());
			assessmentRequestData.setAssessorAvatarUrl(randomPeer.getAvatarUrl());
			noRandomAssessor = false;
		} else {
			noRandomAssessor = true;
			;
		}
	}

	public void resetAskForAssessmentModal() {
		noRandomAssessor = false;
		assessmentRequestData = new AssessmentRequestData();
		peersForAssessment = null;
		peerSearchTerm = null;
	}
	
	public void setAssessor(UserData assessorData) {
		assessmentRequestData.setAssessorId(assessorData.getId());
		assessmentRequestData.setAssessorFullName(assessorData.getFullName());
		assessmentRequestData.setAssessorAvatarUrl(assessorData.getAvatarUrl());

		noRandomAssessor = false;
	}

	public void submitAssessment() {
		try {
			// at this point, assessor should be set either from credential data or
			// user-submitted peer id
			if (assessmentRequestData.isAssessorSet()) {
				populateAssessmentRequestFields();
				assessmentRequestData.setMessageText(assessmentRequestData.getMessageText().replace("\r", ""));
				assessmentRequestData.setMessageText(assessmentRequestData.getMessageText().replace("\n", "<br/>"));
				LearningContextData lcd = new LearningContextData();
				lcd.setPage(PageUtil.getPostParameter("page"));
				lcd.setLearningContext(PageUtil.getPostParameter("learningContext"));
				lcd.setService(PageUtil.getPostParameter("service"));
				long assessmentId = assessmentManager.requestAssessment(assessmentRequestData, lcd);
				String page = PageUtil.getPostParameter("page");
				String lContext = PageUtil.getPostParameter("learningContext");
				String service = PageUtil.getPostParameter("service");
				notifyAssessmentRequestedAsync(assessmentId, assessmentRequestData.getAssessorId(), page, lContext,
						service);

				PageUtil.fireSuccessfulInfoMessage("Assessment request sent");

				if (peersToExcludeFromSearch != null) {
					peersToExcludeFromSearch.add(assessmentRequestData.getAssessorId());
				}
			} else {
				logger.error("Student " + loggedUser.getFullName() + " tried to submit assessment request for credential : "
						+ credentialData.getId() + ", but credential has no assessor/instructor set!");
				PageUtil.fireErrorMessage("No assessor set");
			}
			resetAskForAssessmentModal();
		} catch (EventException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while sending assessment request");
		}
	}
	private void populateAssessmentRequestFields() {
		assessmentRequestData.setCredentialTitle(credentialData.getTitle());
		assessmentRequestData.setStudentId(loggedUser.getUserId());
		assessmentRequestData.setCredentialId(credentialData.getId());
		assessmentRequestData.setTargetCredentialId(credentialData.getTargetCredId());
	}
	private void notifyAssessmentRequestedAsync(final long assessmentId, long assessorId, String page, String lContext,
			String service) {
		taskExecutor.execute(() -> {
			User assessor = new User();
			assessor.setId(assessorId);
			CredentialAssessment assessment = new CredentialAssessment();
			assessment.setId(assessmentId);
			Map<String, String> parameters = new HashMap<>();
			parameters.put("credentialId", idEncoder.decodeId(id) + "");
			try {
				eventFactory.generateEvent(EventType.AssessmentRequested, loggedUser.getUserId(), assessment, assessor,
						page, lContext, service, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});

	}

}
