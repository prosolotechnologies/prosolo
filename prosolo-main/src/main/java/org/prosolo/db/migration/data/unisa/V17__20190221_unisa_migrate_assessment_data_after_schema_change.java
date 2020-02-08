package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.*;

/**
 * @author stefanvuckovic
 * @date 2018-12-18
 * @since 1.2.0
 */
public class V17__20190221_unisa_migrate_assessment_data_after_schema_change extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM (SELECT tc.user from target_credential1 tc inner join credential1 c on tc.credential = c.id inner join user u on u.id =tc.user group by tc.user, c.delivery_of HAVING count(tc.user) > 1) t");
            rs.next();
            int res = rs.getInt(1);
            if (res != 6) {
                throw new Exception("Data in the database changed and it should be revisited. Expected: 6 records, actual: " + res);
            }
        }
        migrateWhenCompetencyAssessmentIsConnectedToSeveralCredentialAssessments(context.getConnection());
        migrateWhenCompetencyToCredentialAssessmentIsOneToOne(context.getConnection());
        migrateWhenCompetencyAssessmentIsNotConnectedToAnyCredentialAssessments(context.getConnection());
        migrateOtherCases(context.getConnection());
    }

    private void migrateOtherCases(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.addBatch("DELETE p FROM competence_assessment_discussion_participant p WHERE p.assessment = 1048616;");
            statement.addBatch("DELETE cca FROM competence_criterion_assessment cca WHERE cca.assessment = 1048616;");
            statement.addBatch("DELETE ca FROM competence_assessment ca WHERE ca.competence = 1 AND ca.student = 786450;");
            statement.addBatch("DELETE tc FROM target_competence1 tc WHERE tc.id = 1114127;");

            statement.executeBatch();
        }
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
            statement.setLong(1, 98311);
            statement.setLong(2, 1114112);
            statement.addBatch();
            statement.setLong(1, 1081362);
            statement.setLong(2, 1114261);
            statement.addBatch();
            statement.setLong(1, 1081361);
            statement.setLong(2, 1114262);
            statement.addBatch();
            statement.executeBatch();
        }
    }

    private void migrateWhenCompetencyAssessmentIsConnectedToSeveralCredentialAssessments(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.addBatch("DELETE FROM `social_activity1` WHERE id = 229579");
            statement.addBatch("DELETE FROM `social_activity1` WHERE actor = 163844");
            statement.addBatch("DELETE ce FROM competence_evidence ce INNER JOIN learning_evidence le on ce.evidence = le.id WHERE le.user = 163844");
            statement.addBatch("DELETE cca FROM credential_assessment ca INNER JOIN credential_competence_assessment cca on cca.credential_assessment = ca.id where ca.student = 163844");

            statement.addBatch("DELETE msg FROM competence_assessment ca INNER JOIN competence_assessment_message msg on msg.assessment = ca.id where ca.student = 163844");
            statement.addBatch("DELETE participant FROM competence_assessment ca INNER JOIN competence_assessment_discussion_participant participant ON participant.assessment = ca.id WHERE ca.student = 163844");
            statement.addBatch("DELETE cca FROM competence_assessment ca INNER JOIN competence_criterion_assessment cca ON cca.assessment = ca.id WHERE ca.student = 163844");
            statement.addBatch("DELETE ca FROM competence_assessment ca WHERE ca.student = 163844");

            statement.addBatch("DELETE msg FROM credential_assessment ca INNER JOIN credential_assessment_message msg on msg.assessment = ca.id where ca.student = 163844");
            statement.addBatch("DELETE participant FROM credential_assessment ca INNER JOIN credential_assessment_discussion_participant participant on participant.assessment = ca.id where ca.student = 163844");
            statement.addBatch("DELETE cca FROM credential_assessment ca INNER JOIN credential_criterion_assessment cca on cca.assessment = ca.id where ca.student = 163844");
            statement.addBatch("DELETE ca FROM credential_assessment ca where ca.student = 163844");

            statement.addBatch("DELETE tc FROM target_competence1 tc WHERE tc.user = 163844");
            statement.addBatch("DELETE FROM target_credential1 WHERE user = 163844");

            try (
                    ResultSet rs = statement.executeQuery(
                            "SELECT cca.competence_assessment FROM credential_competence_assessment cca INNER JOIN credential_assessment credA on credA.id = cca.credential_assessment AND credA.target_credential IN (1015847, 1015820) AND credA.type = 'INSTRUCTOR_ASSESSMENT'");
            ) {
                statement.addBatch("DELETE msg FROM credential_assessment ca INNER JOIN credential_assessment_message msg on msg.assessment = ca.id where ca.target_credential IN (1015847, 1015820)");
                statement.addBatch("DELETE participant FROM credential_assessment ca INNER JOIN credential_assessment_discussion_participant participant on participant.assessment = ca.id where ca.target_credential IN (1015847, 1015820)");
                statement.addBatch("DELETE cca FROM credential_assessment ca INNER JOIN credential_competence_assessment cca on cca.credential_assessment = ca.id where ca.target_credential IN (1015847, 1015820)");
                statement.addBatch("DELETE cca FROM credential_assessment ca INNER JOIN credential_criterion_assessment cca on cca.assessment = ca.id where ca.target_credential IN (1015847, 1015820)");
                statement.addBatch("DELETE ca FROM credential_assessment ca where ca.target_credential IN (1015847, 1015820)");

                statement.addBatch("DELETE FROM target_credential1 WHERE id IN (1015847, 1015820)");
                statement.addBatch("DELETE FROM `social_activity1` WHERE dtype = \"CredentialCompleteSocialActivity\" AND credential_object = 32778 AND actor = 229386");
                statement.addBatch("DELETE FROM `social_activity1` WHERE dtype = \"CredentialCompleteSocialActivity\" AND credential_object = 32787 AND actor = 262150");

                while (rs.next()) {
                    long compAssessmentId = rs.getLong(1);
                    statement.addBatch("DELETE msg FROM competence_assessment_message msg where msg.assessment = " + compAssessmentId);
                    statement.addBatch("DELETE participant FROM competence_assessment_discussion_participant participant WHERE participant.assessment = " + compAssessmentId);
                    statement.addBatch("DELETE cca FROM competence_criterion_assessment cca WHERE cca.assessment = " + compAssessmentId);
                    statement.addBatch("DELETE ca FROM competence_assessment ca WHERE ca.id = " + compAssessmentId);
                }
            }

            statement.executeBatch();
        }
    }
}
