package org.prosolo.core.db.migration;

import org.flywaydb.core.Flyway;

/**
 * @author stefanvuckovic
 * @date 2018-12-19
 * @since 1.2.0
 */
public class DefaultFlywayMigrationStrategy implements FlywayMigrationStrategy {

    private Flyway flyway;
    private boolean formatDb;

    public DefaultFlywayMigrationStrategy(Flyway flyway, boolean formatDb) {
        this.flyway = flyway;
        this.formatDb = formatDb;
    }

    @Override
    public void migrate() {
        if (formatDb) {
            flyway.clean();
        }
        flyway.migrate();
    }
}
