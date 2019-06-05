package org.prosolo.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.prosolo.app.Settings;

/**
 * Base migration class that all data migration classes should inherit.
 * It executes migration if formatDb is false, otherwise queries from migration
 * are not executed. Rationale is that when database is formatted we do not need
 * to apply data migrations since data has been just inserted. So data migrations should
 * be applied only for existing data, not when database is formatted.
 *
 * @author stefanvuckovic
 * @date 2019-02-21
 * @since 1.3
 */
public abstract class BaseMigration extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        if (!Settings.getInstance().config.init.formatDB) {
            doMigrate(context);
        }
    }

    protected abstract void doMigrate(Context context) throws Exception;
}
