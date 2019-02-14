package org.prosolo.core.db.migration;

/**
 * @author stefanvuckovic
 * @date 2018-12-19
 * @since 1.2.0
 */
public interface FlywayMigrationStrategy {

    void migrate();
}
