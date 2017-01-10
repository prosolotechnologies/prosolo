package org.prosolo.web.courses.credential;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.TagCountData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.util.nodes.AnnotationUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialKeywordBean")
@Component("credentialKeywordBean")
@Scope("view")
public class CredentialKeywordsBean {

	private static Logger logger = Logger.getLogger(TargetCompetence1.class);

	@Inject
	private CredentialManager credentialManager;
	@Inject
	private UrlIdEncoder idEncoder;

	private String id;
	private List<TagCountData> tags;
	private List<TagCountData> selectedKeywords;
	private TagCountData lastSelected;
	private List<CompetenceData1> competences;
	private List<ActivityData> activities;
	private String chosenKeywordsString;

	public void init() {
		chosenKeywordsString = null;
		selectedKeywords = new ArrayList<>();
		tags = credentialManager.getTagsForCredentialCompetences(idEncoder.decodeId(id));
		competences = credentialManager.getTargetCompetencesForKeywordSearch(idEncoder.decodeId(id));
		activities = credentialManager.getTargetActivityForKeywordSearch(idEncoder.decodeId(id));

		logger.info("init");
	}

	public String getTagSearchTerm() {
		return AnnotationUtil.getAnnotationsAsSortedCSVForTagCountData(selectedKeywords);
	}

	public void setTagSearchTerm(String tagSearchTerm) {
		this.chosenKeywordsString = tagSearchTerm;
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

	public List<TagCountData> getSelectedKeywords() {
		return selectedKeywords;
	}

	public void addKeyword(TagCountData t) {
		lastSelected = t;
		if (!selectedKeywords.contains(t)) {
			selectedKeywords.add(t);
		}
	}

	public List<ActivityData> getActivities() {
		List<ActivityData> filteredActivities = new ArrayList<>();
		if (!selectedKeywords.isEmpty()) {
			for (ActivityData activity : activities) {
				for (CompetenceData1 competence : filter()) {
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

	public List<CompetenceData1> filter() {
		List<CompetenceData1> filtered = new ArrayList<>();
		if (!selectedKeywords.isEmpty() || !getTagSearchTerm().equals("")) {
			for (CompetenceData1 comp : getCompetences()) {
				if (comp.getTagsString().contains(lastSelected.getTitle())) {
					filtered.add(comp);
				}
			}
		} else {
			filtered = getCompetences();
		}
		return filtered;
	}
	
	public void removeTag(){
		
	}

}
