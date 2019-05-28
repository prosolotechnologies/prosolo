package org.prosolo.services.datainit.impl;

import org.hibernate.jdbc.Work;
import org.prosolo.app.Settings;
import org.prosolo.app.bc.InitData;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.config.observation.ObservationConfigLoaderService;
import org.prosolo.config.security.SecurityService;
import org.prosolo.services.admin.ResourceSettingsManager;
import org.prosolo.services.datainit.DataInitManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-05-20
 * @since 1.3.2
 */
@Service("org.prosolo.services.datainit.DataInitManager")
public class DataInitManagerImpl extends AbstractManagerImpl implements DataInitManager {

    @Inject private SecurityService securityService;
    @Inject private ObservationConfigLoaderService observationConfigLoaderService;
    @Inject private ResourceSettingsManager resourceSettingsManager;
    @Inject private DataInitManager self;

    @Override
    public void reinitializeDBData(InitData initData) {
        try {
            self.truncateTables();
            securityService.initializeRolesAndCapabilities();
            observationConfigLoaderService.initializeObservationConfig();
            resourceSettingsManager.createResourceSettings(
                    Settings.getInstance().config.admin.selectedUsersCanDoEvaluation,
                    Settings.getInstance().config.admin.userCanCreateCompetence,
                    Settings.getInstance().config.admin.individualCompetencesCanNotBeEvaluated);
            initData.getDataInitializer().initRepository();
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error reinitializing the database data");
        }
    }

    @Override
    @Transactional
    public void truncateTables() {
        List<String> tables = new ArrayList<>();
        persistence.currentManager().doWork(connection -> {
            DatabaseMetaData md = connection.getMetaData();
            try (ResultSet rs = md.getTables(CommonSettings.getInstance().config.mysqlConfig.database, null, "%", null)) {
                while (rs.next()) {
                    tables.add(rs.getString(3));
                }
            }
        });
        persistence.currentManager()
                .createSQLQuery("SET FOREIGN_KEY_CHECKS = 0;")
                .executeUpdate();
        List<String> tablesNotToReset = List.of("flyway_schema_history", "innodb_lock_monitor", "hibernate_sequences");
        tables.forEach(tableName -> {
            if (tablesNotToReset.stream().noneMatch(tableNotToReset -> tableNotToReset.equals(tableName))) {
                persistence.currentManager().createSQLQuery("TRUNCATE TABLE " + tableName).executeUpdate();
            }
        });
        persistence.currentManager().createSQLQuery("SET FOREIGN_KEY_CHECKS = 1;").executeUpdate();
    }
}
