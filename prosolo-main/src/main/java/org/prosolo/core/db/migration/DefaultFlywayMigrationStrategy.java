package org.prosolo.core.db.migration;

import org.flywaydb.core.Flyway;
import org.prosolo.app.Settings;

/**
 * @author stefanvuckovic
 * @date 2018-12-19
 * @since 1.2.0
 */
public class DefaultFlywayMigrationStrategy implements FlywayMigrationStrategy {

    private Flyway flyway;

    public DefaultFlywayMigrationStrategy(Flyway flyway) {
        this.flyway = flyway;
    }

    @Override
    public void migrate() {
        if (Settings.getInstance().config.init.formatDB) {
            flyway.clean();
        }
        if (Settings.getInstance().config.init.databaseMigration.repair) {
            flyway.repair();
        }
        flyway.migrate();
    }
}
