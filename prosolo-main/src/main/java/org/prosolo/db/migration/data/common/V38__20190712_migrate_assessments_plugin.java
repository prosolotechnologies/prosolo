package org.prosolo.db.migration.data.common;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Migrate assessments plugin name and new columns values.
 *
 * @author stefanvuckovic
 * @date 2019-07-12
 * @since 1.3.2
 *
 */
public class V38__20190712_migrate_assessments_plugin extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        migrateAssessmentsPlugin(context.getConnection());
    }

    private void migrateAssessmentsPlugin(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
           statement.executeUpdate("UPDATE organization_plugin op " +
                   "SET op.dtype = 'AssessmentsPlugin', " +
                   "op.type = 'ASSESSMENTS', " +
                   "op.enabled = 'T' " +
                   "WHERE op.dtype = 'AssessmentTokensPlugin'");
        }
    }

}
