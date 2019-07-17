package org.prosolo.common.domainmodel.organization.settings;

/**
 * Factory class that creates a default instances of of organization plugins.
 *
 * @author Nikola Milikic
 * @date 2019-05-30
 * @since 1.3.2
 */
public class OrganizationPluginFactory {

    public static OrganizationPlugin getOrganizationPlugin(OrganizationPluginType type) {
        switch (type) {
            case LEARNING_STAGES:
                return LearningStagesPlugin.builder()
                        .type(OrganizationPluginType.LEARNING_STAGES)
                        .enabled(true)
                        .build();
            case EVIDENCE_REPOSITORY:
                return EvidenceRepositoryPlugin.builder()
                        .type(OrganizationPluginType.EVIDENCE_REPOSITORY)
                        .enabled(true)
                        .keywordsEnabled(true)
                        .fileEvidenceEnabled(true)
                        .urlEvidenceEnabled(true)
                        .textEvidenceEnabled(true)
                        .build();
            case CREDENTIAL_CATEGORIES:
                return CredentialCategoriesPlugin.builder()
                        .type(OrganizationPluginType.CREDENTIAL_CATEGORIES)
                        .enabled(true)
                        .build();
            case ASSESSMENTS:
                return AssessmentsPlugin.builder()
                        .type(OrganizationPluginType.ASSESSMENTS)
                        .enabled(true)
                        .assessmentTokensEnabled(true)
                        .privateDiscussionEnabled(true)
                        .build();
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
