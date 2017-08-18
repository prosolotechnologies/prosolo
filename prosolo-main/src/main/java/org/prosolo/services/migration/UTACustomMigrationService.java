package org.prosolo.services.migration;

/**
 * @author nikolamilikic
 * @date 2017-08-18
 * @since 0.7
 */
public interface UTACustomMigrationService {

    void migrateCredentialsFrom06To07();

    void deleteUsers(long newCreatorId);
}
