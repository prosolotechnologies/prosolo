package org.prosolo.services.user.data.profile;

import org.prosolo.services.common.data.LazyInitData;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
public class CredentialProfileData implements Serializable {

    private static final long serialVersionUID = 2006037751655590239L;

    private final long credentialProfileConfigId;
    private final long targetCredentialId;
    private final long credentialId;
    private final String title;
    private final String description;
    private final String duration;
    private final List<String> keywords;
    private final long dateCompleted;
    private final LazyInitData<AssessmentByTypeProfileData> assessments;
    private final LazyInitData<CompetenceProfileData> competences;
    private final CredentialCategoryData category;

    public CredentialProfileData(long credentialProfileConfigId, long targetCredentialId, long credentialId, String title, String description, String duration, List<String> keywords, long dateCompleted, LazyInitData<AssessmentByTypeProfileData> assessments, LazyInitData<CompetenceProfileData> competences, CredentialCategoryData category) {
        this.credentialProfileConfigId = credentialProfileConfigId;
        this.targetCredentialId = targetCredentialId;
        this.credentialId = credentialId;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.keywords = keywords;
        this.dateCompleted = dateCompleted;
        this.assessments = assessments;
        this.competences = competences;
        this.category = category;
    }

    public long getCredentialProfileConfigId() {
        return credentialProfileConfigId;
    }

    public long getTargetCredentialId() {
        return targetCredentialId;
    }

    public long getCredentialId() {
        return credentialId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDuration() {
        return duration;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public long getDateCompleted() {
        return dateCompleted;
    }

    public LazyInitData<AssessmentByTypeProfileData> getAssessments() {
        return assessments;
    }

    public LazyInitData<CompetenceProfileData> getCompetences() {
        return competences;
    }

    public CredentialCategoryData getCategory() {
        return category;
    }
}
