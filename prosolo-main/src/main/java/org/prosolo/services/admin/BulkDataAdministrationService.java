package org.prosolo.services.admin;

import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

/**
 * @author nikolamilikic
 * @date 2017-08-18
 * @since 1.0.0
 */
public interface BulkDataAdministrationService {
    void deleteAndInitElasticSearchIndexes() throws IndexingServiceNotAvailable;

    void deleteAndReindexUsers() throws IndexingServiceNotAvailable;

    void indexDBData();
}
