package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Migrate existing LTI tools by removing id query param from launch url.
 *
 * @author stefanvuckovic
 * @date 2019-07-27
 * @since 1.3.3
 *
 */
public class V42__20190827_migrate_lti_tools extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            statement.executeUpdate(
                    "UPDATE lti_tool lt " +
                    "SET lt.launch_url = REPLACE(lt.launch_url, CONCAT('?id=', lt.id), '')");
        }
    }

}
