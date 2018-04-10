package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private LoggedUserBean loggedUser;

	private String id;
	private long decodedId;
	private List<String> tags;
	private Set<String> selectedKeywords;
	private List<CompetenceData1> allCompetences;
	private List<ActivityData> activities;
	private List<CompetenceData1> filteredCompetences;
	private List<ActivityData> filteredActivities;
	private CredentialData credentialData;


	public void init() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			boolean userEnrolled = credentialManager.isUserEnrolled(decodedId, loggedUser.getUserId());

			if (!userEnrolled) {
				PageUtil.accessDenied();
			} else {
				credentialData = credentialManager.getTargetCredentialData(decodedId, loggedUser.getUserId(), false,false);
				selectedKeywords = new HashSet<>();
				tags = credentialManager.getTagsFromCredentialCompetencesAndActivities(decodedId);
				allCompetences = credentialManager.getCompetencesForKeywordSearch(decodedId);
				activities = credentialManager.getActivitiesForKeywordSearch(decodedId);
				filteredCompetences = new ArrayList<>();
				filteredActivities = new ArrayList<>();
				filterResults();
			}
		} else {
			PageUtil.notFound();
		}
	}

	public String getUnselectedTagsCSV() {
		Set<String> unselectedTags = new HashSet<>(tags);
		unselectedTags.removeAll(selectedKeywords);
		return AnnotationUtil.getAnnotationsAsSortedCSVForTagTitles(unselectedTags);
	}

	public void addKeyword(String t) {
		selectedKeywords.add(t);
		filterResults();
	}

	private void filterResults() {
		filterCompetences();

		if (isHasActivities())
			filterActivities();
	}

	private void filterCompetences() {
		if (!selectedKeywords.isEmpty()) {
			filteredCompetences.clear();
			for (CompetenceData1 comp : allCompetences) {
				if (selectedKeywords.stream()
						.allMatch(tag -> comp.getTags()
								.stream()
								.anyMatch(t -> t.getTitle().equals(tag)))) {
					filteredCompetences.add(comp);
				}
			}
		} else {
			filteredCompetences = new ArrayList<>(allCompetences);
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


	/*
	 * GETTERS / SETTERS
	 */

	public List<CompetenceData1> getFilteredCompetences() {
		return filteredCompetences;
	}

	public List<ActivityData> getFilteredActivities() {
		return filteredActivities;
	}

	public String getChosenKeywordsString() {
		return AnnotationUtil.getAnnotationsAsSortedCSVForTagTitles(selectedKeywords);
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

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public boolean isHasActivities() {
		return activities.size() > 0;
	}
}
