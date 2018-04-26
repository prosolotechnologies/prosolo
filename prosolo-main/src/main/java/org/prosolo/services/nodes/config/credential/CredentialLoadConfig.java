package org.prosolo.services.nodes.config.credential;

import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;

/**
 * @author stefanvuckovic
 * @date 2018-04-24
 * @since 1.2.0
 */
public class CredentialLoadConfig {

    private final boolean loadAssessmentConfig;
    private final boolean loadCompetences;
    private final boolean loadCreator;
    private final boolean loadStudent;
    private final boolean loadCategory;
    private final boolean loadTags;
    private final boolean loadInstructor;
    private final boolean loadAssessmentCount;
    private final CompetenceLoadConfig competenceLoadConfig;

    private CredentialLoadConfig(boolean loadAssessmentConfig, boolean loadCompetences, boolean loadCreator, boolean loadStudent, boolean loadCategory,
                                boolean loadTags, boolean loadInstructor, boolean loadAssessmentCount, CompetenceLoadConfig competenceLoadConfig) {
        this.loadAssessmentConfig = loadAssessmentConfig;
        this.loadCompetences = loadCompetences;
        this.loadCreator = loadCreator;
        this.loadStudent = loadStudent;
        this.loadCategory = loadCategory;
        this.loadTags = loadTags;
        this.loadInstructor = loadInstructor;
        this.loadAssessmentCount = loadAssessmentCount;
        this.competenceLoadConfig = competenceLoadConfig;
    }

    public static CredentialLoadConfig of(boolean loadAssessmentConfig, boolean loadCompetences, boolean loadCreator, boolean loadStudent, boolean loadCategory,
                                          boolean loadTags, boolean loadAssessmentCount, boolean loadInstructor, CompetenceLoadConfig competenceLoadConfig) {
        return new CredentialLoadConfig(loadAssessmentConfig, loadCompetences, loadCreator, loadStudent, loadCategory, loadTags, loadInstructor, loadAssessmentCount, competenceLoadConfig);
    }

    public boolean isLoadAssessmentConfig() {
        return loadAssessmentConfig;
    }

    public boolean isLoadCompetences() {
        return loadCompetences;
    }

    public boolean isLoadCreator() {
        return loadCreator;
    }

    public boolean isLoadCategory() {
        return loadCategory;
    }

    public boolean isLoadTags() {
        return loadTags;
    }

    public boolean isLoadInstructor() {
        return loadInstructor;
    }

    public boolean isLoadAssessmentCount() {
        return loadAssessmentCount;
    }

    public CompetenceLoadConfig getCompetenceLoadConfig() {
        return competenceLoadConfig;
    }

    public boolean isLoadStudent() {
        return loadStudent;
    }
}
