package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Migrate data from Organization class to the respective org. plugin classes.
 *
 * @author Nikola Milikic
 * @date 2019-06-04
 * @since 1.3.2
 */
public class V32__20190604_migrate_organization_plugin_data extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        migrateOrganizationPluginData(context.getConnection());
    }

    private void migrateOrganizationPluginData(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {

            // organization UniSA

            // add org. plugins
            statement.executeUpdate(
                    "INSERT INTO organization_plugin (dtype, id, enabled, type, initial_number_of_tokens_given, number_of_earned_tokens_per_assessment, number_of_spent_tokens_per_request, file_evidence_enabled, keywords_enabled, text_evidence_enabled, url_evidence_enabled, organization) " +
                    "VALUES ('LearningStagesPlugin', 1, 'T', 'LEARNING_STAGES', null, null, null, null, null, null, null, 1);");

            statement.executeUpdate(
                    "INSERT INTO organization_plugin (dtype, id, enabled, type, initial_number_of_tokens_given, number_of_earned_tokens_per_assessment, number_of_spent_tokens_per_request, file_evidence_enabled, keywords_enabled, text_evidence_enabled, url_evidence_enabled, organization) " +
                    "VALUES ('EvidenceRepositoryPlugin', 2, 'T', 'EVIDENCE_REPOSITORY', null, null, null, 'T', 'F', 'F', 'T', 1);");

            statement.executeUpdate(
                    "INSERT INTO organization_plugin (dtype, id, enabled, type, initial_number_of_tokens_given, number_of_earned_tokens_per_assessment, number_of_spent_tokens_per_request, file_evidence_enabled, keywords_enabled, text_evidence_enabled, url_evidence_enabled, organization) " +
                    "VALUES ('CredentialCategoriesPlugin', 3, 'F', 'CREDENTIAL_CATEGORIES', null, null, null, null, null, null, null, 1);");

            statement.executeUpdate(
                    "INSERT INTO organization_plugin (dtype, id, enabled, type, initial_number_of_tokens_given, number_of_earned_tokens_per_assessment, number_of_spent_tokens_per_request, file_evidence_enabled, keywords_enabled, text_evidence_enabled, url_evidence_enabled, organization) " +
                    "VALUES ('AssessmentTokensPlugin', 4, 'T', 'ASSESSMENT_TOKENS', 0, 0, 0, null, null, null, null, 1);");

            statement.executeUpdate(
                    "UPDATE learning_stage " +
                    "SET learning_stages_plugin = 1 " +
                    "WHERE organization = 1;");

            statement.executeUpdate(
                    "UPDATE credential_category " +
                    "SET credential_categories_plugin = 3 " +
                    "WHERE organization = 1;");


            // organization AstroPACE Project
            // add org. plugins
            statement.executeUpdate(
                    "INSERT INTO organization_plugin (dtype, id, enabled, type, initial_number_of_tokens_given, number_of_earned_tokens_per_assessment, number_of_spent_tokens_per_request, file_evidence_enabled, keywords_enabled, text_evidence_enabled, url_evidence_enabled, organization) " +
                            "VALUES ('LearningStagesPlugin', 5, 'T', 'LEARNING_STAGES', null, null, null, null, null, null, null, 32768);");

            statement.executeUpdate(
                    "INSERT INTO organization_plugin (dtype, id, enabled, type, initial_number_of_tokens_given, number_of_earned_tokens_per_assessment, number_of_spent_tokens_per_request, file_evidence_enabled, keywords_enabled, text_evidence_enabled, url_evidence_enabled, organization) " +
                            "VALUES ('EvidenceRepositoryPlugin', 6, 'T', 'EVIDENCE_REPOSITORY', null, null, null, 'T', 'T', 'T', 'T', 32768);");

            statement.executeUpdate(
                    "INSERT INTO organization_plugin (dtype, id, enabled, type, initial_number_of_tokens_given, number_of_earned_tokens_per_assessment, number_of_spent_tokens_per_request, file_evidence_enabled, keywords_enabled, text_evidence_enabled, url_evidence_enabled, organization) " +
                            "VALUES ('CredentialCategoriesPlugin', 7, 'F', 'CREDENTIAL_CATEGORIES', null, null, null, null, null, null, null, 32768);");

            statement.executeUpdate(
                    "INSERT INTO organization_plugin (dtype, id, enabled, type, initial_number_of_tokens_given, number_of_earned_tokens_per_assessment, number_of_spent_tokens_per_request, file_evidence_enabled, keywords_enabled, text_evidence_enabled, url_evidence_enabled, organization) " +
                            "VALUES ('AssessmentTokensPlugin', 8, 'F', 'ASSESSMENT_TOKENS', 0, 0, 0, null, null, null, null, 32768);");
        }
    }

}
