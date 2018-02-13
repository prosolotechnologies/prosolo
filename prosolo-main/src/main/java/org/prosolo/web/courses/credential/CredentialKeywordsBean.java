package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.search.UserTextSearch;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
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

	private String id;
	private List<String> tags;
	private Set<String> selectedKeywords;
	private List<CompetenceData1> competences;
	private List<ActivityData> activities;
	private List<CompetenceData1> filteredCompetences;
	private List<ActivityData> filteredActivities;
	private CredentialData credentialData;
	private AssessmentRequestData assessmentRequestData = new AssessmentRequestData();
	
	private long decodedId;

	public void init() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			boolean userEnrolled = credentialManager.isUserEnrolled(decodedId, loggedUser.getUserId());

			if (!userEnrolled) {
				PageUtil.accessDenied();
			} else {
				credentialData = credentialManager.getTargetCredentialData(decodedId, loggedUser.getUserId(), false,false);
				selectedKeywords = new HashSet<>();
				filteredCompetences = new ArrayList<>();
				tags = credentialManager.getTagsFromCredentialCompetencesAndActivities(decodedId);
				competences = credentialManager.getCompetencesForKeywordSearch(decodedId);
				activities = credentialManager.getActivitiesForKeywordSearch(decodedId);
				filteredActivities = new ArrayList<>();
			}
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

	public CredentialData getCredentialData() {
		return credentialData;
	}

	public AssessmentManager getAssessmentManager() {
		return assessmentManager;
	}

	public void setAssessmentManager(AssessmentManager assessmentManager) {
		this.assessmentManager = assessmentManager;
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

	public ThreadPoolTaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
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


}
