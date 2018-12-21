package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;

import java.sql.*;

/**
 * @author stefanvuckovic
 * @date 2018-12-18
 * @since 1.2.0
 */
public class V8__20181218_unisa_migrate_blind_assessment_mode extends BaseJavaMigration {


    @Override
    public void migrate(Context context) throws Exception {
        migrateCompetenceAssessmentConfigBlindAssessmentMode(context.getConnection());
        migrateCredentialAssessmentsBlindAssessmentMode(context.getConnection());
        migrateCompetenceAssessmentsBlindAssessmentMode(context.getConnection());
    }

    private void migrateCompetenceAssessmentConfigBlindAssessmentMode(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                    ResultSet rs1 = getAllCompetenceIds(statement);
                    PreparedStatement ps = connection.prepareStatement(getTheMostRestrictiveBlindAssessmentModeQuery());
                    PreparedStatement psUpdate = connection.prepareStatement(getCompetenceAssessmentConfigUpdateQuery())
            ) {
                while (rs1.next()) {
                    long competenceId = rs1.getLong(1);
                    ps.setLong(1, competenceId);
                    try (ResultSet rs2 = ps.executeQuery()) {
                        rs2.next();
                        String assessmentMode = rs2.getString(1);
                        if (assessmentMode != null && BlindAssessmentMode.valueOf(assessmentMode) != BlindAssessmentMode.OFF) {
                            //update only if mode differs from default value
                            psUpdate.setString(1, assessmentMode);
                            psUpdate.setLong(2, competenceId);
                            psUpdate.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    private void migrateCredentialAssessmentsBlindAssessmentMode(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
                String q =
                        "UPDATE credential_assessment ca " +
                        "INNER JOIN target_credential1 tc ON ca.target_credential = tc.id " +
                        "INNER JOIN credential1 c ON tc.credential = c.id " +
                        "INNER JOIN credential_assessment_config conf ON c.id = conf.credential " +
                                "AND conf.assessment_type = 'PEER_ASSESSMENT' " +
                        "SET ca.blind_assessment_mode = conf.blind_assessment_mode " +
                        "WHERE ca.type = 'PEER_ASSESSMENT'";
                statement.executeUpdate(q);
        }
    }

    private void migrateCompetenceAssessmentsBlindAssessmentMode(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String q =
                    "UPDATE competence_assessment ca " +
                    "INNER JOIN competence1 c ON ca.competence = c.id " +
                    "INNER JOIN competence_assessment_config conf ON c.id = conf.competence " +
                                "AND conf.assessment_type = 'PEER_ASSESSMENT' " +
                    "SET ca.blind_assessment_mode = conf.blind_assessment_mode " +
                    "WHERE ca.type = 'PEER_ASSESSMENT'";
            statement.executeUpdate(q);
        }
    }

    private String getCompetenceAssessmentConfigUpdateQuery() {
        String query =
                "UPDATE competence_assessment_config conf " +
                "SET conf.blind_assessment_mode = ? " +
                "WHERE conf.competence = ? " +
                "AND conf.assessment_type = 'PEER_ASSESSMENT'";
        return query;
    }

    private ResultSet getAllCompetenceIds(Statement s) throws SQLException {
        return s.executeQuery("SELECT c.id FROM competence1 c");
    }

    private String getTheMostRestrictiveBlindAssessmentModeQuery() {
        return "select assessment2_.blind_assessment_mode as col_0_0_ from credential_competence1 credential0_ inner join credential1 credential1_ on credential0_.credential=credential1_.id inner join credential_assessment_config assessment2_ on credential1_.id=assessment2_.credential and (assessment2_.assessment_type='PEER_ASSESSMENT') where credential0_.competence=? order by case when assessment2_.blind_assessment_mode='DOUBLE_BLIND' then 1 when assessment2_.blind_assessment_mode='BLIND' then 2 else 3 end limit 1";
    }
}
