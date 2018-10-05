package org.prosolo.services.migration;

import org.prosolo.services.event.EventQueue;

/**
 * @author nikolamilikic
 * @date 2017-08-18
 * @since 1.0.0
 */
public interface UTACustomMigrationService {

    void migrateCredentialsFrom06To07();

    void deleteUsers(long newCreatorId);

    EventQueue migrateCredentials();
}
