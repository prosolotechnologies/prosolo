package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Connection;
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
}
