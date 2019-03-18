package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author stefanvuckovic
 * @date 2018-12-18
 * @since 1.2.0
 */
public class V17__20190221_unisa_migrate_assessment_data_after_schema_change extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        migrateWhenCompetencyToCredentialAssessmentIsOneToOne(context.getConnection());
        migrateWhenCompetencyAssessmentIsNotConnectedToAnyCredentialAssessments(context.getConnection());
    }

    private void migrateWhenCompetencyToCredentialAssessmentIsOneToOne(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String q =
                    "UPDATE competence_assessment ca " +
                            "INNER JOIN credential_competence_assessment cca " +
                            "ON cca.competence_assessment = ca.id " +
                            "INNER JOIN credential_assessment creda " +
                            "ON creda.id = cca.credential_assessment " +
                            "INNER JOIN (SELECT ca.id as compAId FROM competence_assessment ca " +
                            "INNER JOIN credential_competence_assessment cca " +
                            "ON cca.competence_assessment = ca.id " +
                            "GROUP BY ca.id HAVING COUNT(ca.id) = 1) tmp ON tmp.compAId = ca.id " +
                            "SET ca.target_credential = creda.target_credential, " +
                            "ca.credential_assessment = creda.id";
            statement.executeUpdate(q);
        }
    }

    private void migrateWhenCompetencyAssessmentIsNotConnectedToAnyCredentialAssessments(Connection connection) throws SQLException {
        String sql = "UPDATE competence_assessment ca SET ca.target_credential = ? WHERE ca.id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 98314);
            statement.setLong(2, 950281);
            statement.addBatch();
            statement.setLong(1, 360484);
            statement.setLong(2, 754821);
            statement.addBatch();
            statement.setLong(1, 196609);
            statement.setLong(2, 754811);
            statement.addBatch();
            statement.setLong(1, 196609);
            statement.setLong(2, 754822);
            statement.addBatch();
            statement.setLong(1, 753903);
            statement.setLong(2, 756539);
            statement.addBatch();
            statement.setLong(1, 98311);
            statement.setLong(2, 950280);
            statement.addBatch();
            statement.executeBatch();
        }
    }

    private void migrateWhenCompetencyAssessmentIsConnectedToSeveralCredentialAssessments(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.addBatch("DELETE FROM credential_assessment ca where ca.target_credential = 983040");
            statement.addBatch("DELETE FROM target_credential1 WHERE id = 983040");

            statement.addBatch("DELETE FROM credential_assessment ca where ca.target_credential = 98316");
            statement.addBatch("DELETE FROM target_credential1 WHERE id = 98316");

            statement.addBatch("DELETE FROM credential_assessment ca where ca.target_credential = 131072");
            statement.addBatch("DELETE FROM target_credential1 WHERE id = 131072");

            statement.addBatch("DELETE FROM credential_assessment ca where ca.target_credential = 950273");
            statement.addBatch("DELETE FROM target_credential1 WHERE id = 950273");

            statement.addBatch("DELETE FROM credential_assessment ca where ca.target_credential = 917505");
            statement.addBatch("DELETE FROM target_credential1 WHERE id = 917505");

            statement.executeBatch();
        }
    }
}
