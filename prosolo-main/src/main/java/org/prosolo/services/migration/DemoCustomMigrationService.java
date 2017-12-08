package org.prosolo.services.migration;

import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventQueue;

import java.util.List;

/**
 * @author nikolamilikic
 * @date 2017-08-18
 * @since 1.0.0
 */
public interface DemoCustomMigrationService {

    void migrateDataFrom06To11();

    EventQueue migrateCredentials();
}
