package org.prosolo.services.migration;

import org.prosolo.services.event.EventData;

import java.util.List;

/**
 * @author nikolamilikic
 * @date 2017-08-18
 * @since 0.7
 */
public interface UTACustomMigrationService {

    void migrateCredentialsFrom06To07();

    void deleteUsers(long newCreatorId);

    List<EventData> migrateCredentials();
}
