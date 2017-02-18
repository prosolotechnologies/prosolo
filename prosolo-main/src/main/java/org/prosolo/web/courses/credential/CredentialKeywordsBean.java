package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.TagCountData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private LoggedUserBean loggedUser;

	private String id;
	private List<TagCountData> tags;
	private List<TagCountData> selectedKeywords;
	private TagCountData lastSelected;
	private List<CompetenceData1> competences;
	private List<ActivityData> activities;
	private String chosenKeywordsString;
	private List<CompetenceData1> filteredCompetences;
	private CredentialData enrolledStudent;

	public void init() {
		enrolledStudent = credentialManager.getFullTargetCredentialOrCredentialDataForPreview(idEncoder.decodeId(id), loggedUser.getSessionData().getUserId());
		if (enrolledStudent != null) {
			selectedKeywords = new ArrayList<>();
			filteredCompetences = new ArrayList<>();
			tags = credentialManager.getTagsForCredentialCompetences(idEncoder.decodeId(id));
			competences = credentialManager.getTargetCompetencesForKeywordSearch(idEncoder.decodeId(id));
			activities = credentialManager.getTargetActivityForKeywordSearch(idEncoder.decodeId(id));
			filterCompetences();
			logger.info("init");
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				logger.error(ioe);
			}
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

	public List<TagCountData> getSelectedKeywords() {
		return selectedKeywords;
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

}
