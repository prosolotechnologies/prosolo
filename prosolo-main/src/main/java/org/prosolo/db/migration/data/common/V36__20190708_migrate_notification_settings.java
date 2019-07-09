package org.prosolo.db.migration.data.common;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Statement;

/**
 * @author stefanvuckovic
 * @date 2019-07-08
 * @since 1.3.2
 */
public class V36__20190708_migrate_notification_settings extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            statement.addBatch("SET @id = (SELECT MAX(id) FROM notification_settings)");
            statement.addBatch("INSERT INTO notification_settings (id, subscribed_email, type, user) " +
                    "SELECT @id := @id + 1, 'T', 'ASSESSMENT_TOKENS_NUMBER_UPDATED', ns.user " +
                    "FROM notification_settings ns " +
                    "GROUP BY ns.user");
            statement.executeBatch();
        }
    }

}
