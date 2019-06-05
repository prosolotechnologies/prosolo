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
                // TODO: Ugly hack for now since IntelliJ Lombok plugin does not support @SuperBuilder annotation (see
                //  https://github.com/mplushnikov/lombok-intellij-plugin/issues/513). Once it is supported, configure the
                //  instance in a one liner with the builder.
                LearningStagesPlugin plugin = LearningStagesPlugin.builder().build();
                plugin.setType(OrganizationPluginType.LEARNING_STAGES);
                plugin.setEnabled(true);
                return plugin;
            case EVIDENCE_REPOSITORY:
                // TODO: Ugly hack for now since IntelliJ Lombok plugin does not support @SuperBuilder annotation (see
                //  https://github.com/mplushnikov/lombok-intellij-plugin/issues/513). Once it is supported, configure the
                //  instance in a one liner with the builder.
                EvidenceRepositoryPlugin plugin1 = EvidenceRepositoryPlugin.builder()
                        .keywordsEnabled(true)
                        .fileEvidenceEnabled(true)
                        .urlEvidenceEnabled(true)
                        .textEvidenceEnabled(true)
                        .build();
                plugin1.setType(OrganizationPluginType.EVIDENCE_REPOSITORY);
                plugin1.setEnabled(true);
                return plugin1;
            case CREDENTIAL_CATEGORIES:
                // TODO: Ugly hack for now since IntelliJ Lombok plugin does not support @SuperBuilder annotation (see
                //  https://github.com/mplushnikov/lombok-intellij-plugin/issues/513). Once it is supported, configure the
                //  instance in a one liner with the builder.
                CredentialCategoriesPlugin plugin2 = CredentialCategoriesPlugin.builder().build();
                plugin2.setType(OrganizationPluginType.CREDENTIAL_CATEGORIES);
                plugin2.setEnabled(true);
                return plugin2;
            case ASSESSMENT_TOKENS:
                // TODO: Ugly hack for now since IntelliJ Lombok plugin does not support @SuperBuilder annotation (see
                //  https://github.com/mplushnikov/lombok-intellij-plugin/issues/513). Once it is supported, configure the
                //  instance in a one liner with the builder.
                AssessmentTokensPlugin plugin3 = AssessmentTokensPlugin.builder().build();
                plugin3.setType(OrganizationPluginType.ASSESSMENT_TOKENS);
                plugin3.setEnabled(true);
                return plugin3;
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
