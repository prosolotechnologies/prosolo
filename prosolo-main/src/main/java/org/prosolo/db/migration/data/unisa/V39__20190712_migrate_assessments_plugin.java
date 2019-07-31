package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
public class V39__20190712_migrate_assessments_plugin extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        migrateOrganizationPluginData(context.getConnection());
    }

    private void migrateOrganizationPluginData(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE organization_plugin op " +
                "SET op.assessment_tokens_enabled = ?, " +
                "op.private_discussion_enabled = ? " +
                "WHERE op.dtype = 'AssessmentsPlugin' and op.organization = ?")) {
            ps.setInt(1, 1);
            ps.setInt(2, 0);
            ps.setLong(3, 1);
            ps.addBatch();

            ps.setInt(1, 0);
            ps.setInt(2, 1);
            ps.setLong(3, 32768);
            ps.addBatch();

            ps.executeBatch();
        }
    }

}
