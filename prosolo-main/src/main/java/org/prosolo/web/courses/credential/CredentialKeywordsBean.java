package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.UserData;
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
import java.util.*;

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
	private List<String> tags;
	private Set<String> selectedKeywords;
	private List<CompetenceData1> competences;
	private List<ActivityData> activities;
	private List<CompetenceData1> filteredCompetences;
	private List<ActivityData> filteredActivities;
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
				loggedUser.getUserId(), false);
		if (credentialData != null) {
			selectedKeywords = new HashSet<>();
			filteredCompetences = new ArrayList<>();
			tags = credentialManager.getTagsFromCredentialCompetencesAndActivities(decodedId);
			competences = credentialManager.getCompetencesForKeywordSearch(decodedId);
			activities = credentialManager.getActivitiesForKeywordSearch(decodedId);
			filterResults();

			logger.info("init credential keywords");
		} else {
			PageUtil.notFound();
		}

	}

	public List<CompetenceData1> getFilteredCompetences() {
		return filteredCompetences;
	}

	public List<ActivityData> getFilteredActivities() {
		return filteredActivities;
	}

	public String getChosenKeywordsString() {
		return AnnotationUtil.getAnnotationsAsSortedCSVForTagTitles(selectedKeywords);
	}

	public String getUnselectedTagsCSV() {
		Set<String> unselectedTags = new HashSet<>(tags);
		unselectedTags.removeAll(selectedKeywords);
		return AnnotationUtil.getAnnotationsAsSortedCSVForTagTitles(unselectedTags);
	}

	public List<CompetenceData1> getCompetences() {
		return competences;
	}

	public List<String> getTags() {
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

	public Set<String> getSelectedKeywords() {
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

	public void addKeyword(String t) {
		selectedKeywords.add(t);
		filterResults();
	}

	public List<ActivityData> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityData> activities) {
		this.activities = activities;
	}

	private void filterResults() {
		filterCompetences();
		filterActivities();
	}

	private void filterCompetences() {
		if (!selectedKeywords.isEmpty()) {
			filteredCompetences.clear();
			for (CompetenceData1 comp : getCompetences()) {
				if (selectedKeywords.stream()
						.allMatch(tag -> comp.getTags()
										 .stream()
										 .anyMatch(t -> t.getTitle().equals(tag)))) {
					filteredCompetences.add(comp);
				}
			}
		} else {
			filteredCompetences = new ArrayList<>(competences);
		}
	}

	private void filterActivities() {
		if (!selectedKeywords.isEmpty()) {
			filteredActivities.clear();
			for (ActivityData act : activities) {
				if (selectedKeywords.stream()
						.allMatch(tag -> act.getTags()
								         .stream()
								         .anyMatch(t -> t.getTitle().equals(tag)))) {
					filteredActivities.add(act);
				}
			}
		} else {
			filteredActivities = new ArrayList<>(activities);
		}
	}

	public void removeTag() {
		String tagToRemove = PageUtil.getGetParameter("tag");
		selectedKeywords.remove(tagToRemove);
		filterResults();
	}

	public void addTag() {
		String tagToAdd = PageUtil.getGetParameter("tag");
		selectedKeywords.add(tagToAdd);
		filterResults();
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

				PaginatedResult<UserData> result = userTextSearch.searchPeersWithoutAssessmentRequest(
						loggedUser.getOrganizationId(), peerSearchTerm, 3, idEncoder.decodeId(id), peersToExcludeFromSearch);
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
				long assessmentId = assessmentManager.requestAssessment(assessmentRequestData, loggedUser.getUserContext());

				notifyAssessmentRequestedAsync(assessmentId, assessmentRequestData.getAssessorId());

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
	private void notifyAssessmentRequestedAsync(final long assessmentId, long assessorId) {
		UserContextData context = loggedUser.getUserContext();
		taskExecutor.execute(() -> {
			User assessor = new User();
			assessor.setId(assessorId);
			CredentialAssessment assessment = new CredentialAssessment();
			assessment.setId(assessmentId);
			Map<String, String> parameters = new HashMap<>();
			parameters.put("credentialId", idEncoder.decodeId(id) + "");
			try {
				eventFactory.generateEvent(EventType.AssessmentRequested, context, assessment, assessor,
						null, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});

	}

}
