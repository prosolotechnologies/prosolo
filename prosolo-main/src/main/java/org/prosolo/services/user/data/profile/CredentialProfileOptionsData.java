package org.prosolo.services.user.data.profile;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-11-20
 * @since 1.2.0
 */
public class CredentialProfileOptionsData implements Serializable {

    private static final long serialVersionUID = 7664743496622047694L;

    private final long targetCredentialId;
    private final String title;
    private final List<CompetenceProfileOptionsData> competences;
    private final List<AssessmentByTypeProfileOptionsData> assessments;

    public CredentialProfileOptionsData(long targetCredentialId, String title, List<CompetenceProfileOptionsData> competences, List<AssessmentByTypeProfileOptionsData> assessments) {
        this.targetCredentialId = targetCredentialId;
        this.title = title;
        this.competences = competences;
        this.assessments = assessments;
    }

    public long getTargetCredentialId() {
        return targetCredentialId;
    }

    public String getTitle() {
        return title;
    }

    public List<CompetenceProfileOptionsData> getCompetences() {
        return competences;
    }

    public List<AssessmentByTypeProfileOptionsData> getAssessments() {
        return assessments;
    }

    public static CredentialTargetCredentialIdStep builder() {
        return new Builder();
    }

    public static class Builder implements CredentialTargetCredentialIdStep, CredentialTitleStep, CredentialAssessmentsDataStep, CompetencesDataStep, Build {

        private long targetCredentialId;
        private String title;
        private List<CompetenceProfileOptionsData> competences;
        private List<AssessmentByTypeProfileOptionsData> assessments;

        private Builder() {}

        @Override
        public CredentialTitleStep setTargetCredentialId(long targetCredentialId) {
            this.targetCredentialId = targetCredentialId;
            return this;
        }

        @Override
        public CredentialAssessmentsDataStep setTitle(String title) {
            this.title = title;
            return this;
        }

        @Override
        public CompetencesDataStep setAssessments(List<AssessmentByTypeProfileOptionsData> assessments) {
            this.assessments = assessments;
            return this;
        }

        @Override
        public Build setCompetences(List<CompetenceProfileOptionsData> competences) {
            this.competences = competences;
            return this;
        }

        @Override
        public CredentialProfileOptionsData build() {
            return new CredentialProfileOptionsData(targetCredentialId, title, competences, assessments);
        }
    }

    public interface CredentialTargetCredentialIdStep {
        CredentialTitleStep setTargetCredentialId(long targetCredentialId);
    }

    public interface CredentialTitleStep {
        CredentialAssessmentsDataStep setTitle(String title);
    }

    public interface CredentialAssessmentsDataStep {
        CompetencesDataStep setAssessments(List<AssessmentByTypeProfileOptionsData> assessments);
    }

    public interface CompetencesDataStep {
        Build setCompetences(List<CompetenceProfileOptionsData> competences);
    }

    public interface Build {
        CredentialProfileOptionsData build();
    }
}
