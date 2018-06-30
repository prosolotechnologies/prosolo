package org.prosolo.services.nodes.config.competence;

/**
 * @author stefanvuckovic
 * @date 2018-04-24
 * @since 1.2.0
 */
public class CompetenceLoadConfig {

    private final boolean loadCreator;
    private final boolean loadTags;
    private final boolean loadActivities;
    private final boolean loadEvidence;
    private final boolean loadAssessmentCount;

    private CompetenceLoadConfig(boolean loadCreator, boolean loadTags, boolean loadActivities, boolean loadEvidence, boolean loadAssessmentCount) {
        this.loadCreator = loadCreator;
        this.loadTags = loadTags;
        this.loadActivities = loadActivities;
        this.loadEvidence = loadEvidence;
        this.loadAssessmentCount = loadAssessmentCount;
    }

    public static CompetenceLoadConfigBuilder builder() {
        return new CompetenceLoadConfigBuilder();
    }

    public boolean isLoadCreator() {
        return loadCreator;
    }

    public boolean isLoadTags() {
        return loadTags;
    }

    public boolean isLoadActivities() {
        return loadActivities;
    }

    public boolean isLoadEvidence() {
        return loadEvidence;
    }

    public boolean isLoadAssessmentCount() {
        return loadAssessmentCount;
    }

    public static class CompetenceLoadConfigBuilder {
        private boolean loadCreator;
        private boolean loadTags;
        private boolean loadActivities;
        private boolean loadEvidence;
        private boolean loadAssessmentCount;

        public CompetenceLoadConfigBuilder setLoadCreator(boolean loadCreator) {
            this.loadCreator = loadCreator;
            return this;
        }

        public CompetenceLoadConfigBuilder setLoadTags(boolean loadTags) {
            this.loadTags = loadTags;
            return this;
        }

        public CompetenceLoadConfigBuilder setLoadActivities(boolean loadActivities) {
            this.loadActivities = loadActivities;
            return this;
        }

        public CompetenceLoadConfigBuilder setLoadEvidence(boolean loadEvidence) {
            this.loadEvidence = loadEvidence;
            return this;
        }

        public CompetenceLoadConfigBuilder setLoadAssessmentCount(boolean loadAssessmentCount) {
            this.loadAssessmentCount = loadAssessmentCount;
            return this;
        }

        public CompetenceLoadConfig create() {
            return new CompetenceLoadConfig(loadCreator, loadTags, loadActivities, loadEvidence, loadAssessmentCount);
        }
    }
}
