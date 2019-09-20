package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Statement;

/**
 * Migrate object type and id of assessment grade notifications.
 *
 * @author stefanvuckovic
 * @date 2019-08-12
 * @since 1.3.2
 *
 */
public class V40__20190812_migrate_grade_notification extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            String q =
                    "UPDATE notification1 n " +
                    "INNER JOIN competence_assessment ca on ca.id = n.object_id " +
                    "SET n.object_type = 'Competence', n.object_id = ca.competence " +
                    "WHERE n.type = 'GradeAdded' " +
                    "AND n.object_type = 'CompetenceAssessment'";
            statement.executeUpdate(q);

            q = "UPDATE notification1 n " +
                    "INNER JOIN credential_assessment ca on ca.id = n.object_id " +
                    "INNER JOIN target_credential1 tc on ca.target_credential = tc.id " +
                    "SET n.object_type = 'Credential', n.object_id = tc.credential " +
                    "WHERE n.type = 'GradeAdded' " +
                    "AND n.object_type = 'CredentialAssessment'";
            statement.executeUpdate(q);
        }
    }


}
