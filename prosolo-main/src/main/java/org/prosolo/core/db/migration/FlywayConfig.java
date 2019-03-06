package org.prosolo.core.db.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.prosolo.app.Settings;
import org.prosolo.common.config.AppConfig;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.core.db.DataSourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-12-19
 * @since 1.2.0
 */
@Configuration
public class FlywayConfig {

    @Inject
    private DataSourceConfig dataSourceConfig;

    @Bean(initMethod = "migrate")
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        boolean formatDb = Settings.getInstance().config.init.formatDB;
        List<String> migrations = new ArrayList<>();
        migrations.add("classpath:db/migration/schema");
        migrations.add("classpath:org/prosolo/db/migration/data/common");
        if (CommonSettings.getInstance().config.appConfig.deployment != AppConfig.Deployment.LOCAL) {
            migrations.add("classpath:org/prosolo/db/migration/data/" + CommonSettings.getInstance().config.appConfig.deployment.name().toLowerCase());
        }
        FluentConfiguration flywayConf = Flyway
                .configure()
                .dataSource(dataSourceConfig.dataSource())
                .locations(migrations.toArray(new String[0]));
        if (!formatDb) {
            /*
            only if formatDB is false baseline is used, if databse is empty (formatDB is true)
            there is no need to use baseline
             */
            /*
            all migrations up to baseline version will be ignored and all
            migrations after this version will be executed
             */
            flywayConf
                    /*
                    current solution until flyway is introduced to release.
                    this tells flyway that we are currently on version 1 so it
                    will not try to execute init script which creates tables; it
                    will only apply migrations.
                     */
                    .baselineVersion("1")
                    /*
                    baseline is used when introducing Flyway to existing database
                    if Flyway metadata table is already created, baseline settings don't
                    have any effect
                     */
                    .baselineOnMigrate(true)
                    .baselineDescription("Current state");
        }
        return new DefaultFlywayMigrationStrategy(flywayConf.load());
    }

}
