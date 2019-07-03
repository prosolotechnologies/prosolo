package org.prosolo.services.nodes.data.evidence;

import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.services.nodes.data.BasicObjectInfo;
import org.prosolo.services.nodes.data.CompetencyBasicObjectInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author stefanvuckovic
 * @date 2017-12-04
 * @since 1.2.0
 */
public class LearningEvidenceData implements Serializable {

    private static final long serialVersionUID = -5070671721203793789L;

    private long id;
    private long userId;
    private String userFullName;
    private long competenceEvidenceId;
    private String title;
    private String text;
    private String relationToCompetence;
    private String competenceTitle;
    private LearningEvidenceType type;
    private String url;
    private Set<String> tags;
    private String tagsString;
    private long dateCreated;
    //date when evidence is added to the current competence
    private long dateAttached;
    //competences with this evidence
    private List<CompetencyBasicObjectInfo> competences;

    public LearningEvidenceData() {
        competences = new ArrayList<>();
    }

    public String getFileName() {
        return url != null && !url.isEmpty() ? url.substring(url.lastIndexOf("/") + 1) : null;
    }

    public String getTitle() {
        return title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCompetenceEvidenceId() {
        return competenceEvidenceId;
    }

    public void setCompetenceEvidenceId(long competenceEvidenceId) {
        this.competenceEvidenceId = competenceEvidenceId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LearningEvidenceType getType() {
        return type;
    }

    public void setType(LearningEvidenceType type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getTagsString() {
        return tagsString;
    }

    public void setTagsString(String tagsString) {
        this.tagsString = tagsString;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getDateAttached() {
        return dateAttached;
    }

    public void setDateAttached(long dateAttached) {
        this.dateAttached = dateAttached;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public List<CompetencyBasicObjectInfo> getCompetences() {
        return competences;
    }

    public void addCompetence(CompetencyBasicObjectInfo comp) {
        if (comp != null) {
            competences.add(comp);
        }
    }

    public void addCompetences(List<CompetencyBasicObjectInfo> competences) {
        this.competences.addAll(competences);
    }

    public String getRelationToCompetence() {
        return relationToCompetence;
    }

    public void setRelationToCompetence(String relationToCompetence) {
        this.relationToCompetence = relationToCompetence;
    }

    public String getCompetenceTitle() {
        return competenceTitle;
    }

    public void setCompetenceTitle(String competenceTitle) {
        this.competenceTitle = competenceTitle;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }
}
