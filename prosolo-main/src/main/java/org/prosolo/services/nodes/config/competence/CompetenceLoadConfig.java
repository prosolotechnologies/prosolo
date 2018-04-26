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

    public static CompetenceLoadConfig of(boolean loadCreator, boolean loadTags, boolean loadActivities, boolean loadEvidence, boolean loadAssessmentCount) {
        return new CompetenceLoadConfig(loadCreator, loadTags, loadActivities, loadEvidence, loadAssessmentCount);
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
}
