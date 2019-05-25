package org.prosolo.db.migration.data.common;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Statement;

/**
 * @author stefanvuckovic
 * @date 2018-12-18
 * @since 1.2.0
 */
public class V27__20190416_migrate_notification_settings extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            statement.addBatch("SET @id = (SELECT MAX(id) FROM notification_settings)");
            statement.addBatch("INSERT INTO notification_settings (id, subscribed_email, type, user) " +
                    "SELECT @id := @id + 1, 'T', 'ASSESSOR_WITHDREW_FROM_ASSESSMENT', ns.user " +
                    "FROM notification_settings ns " +
                    "GROUP BY ns.user");
            statement.executeBatch();
        }
    }

}
