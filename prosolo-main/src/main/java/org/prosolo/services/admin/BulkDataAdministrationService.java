package org.prosolo.services.admin;

import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

/**
 * @author nikolamilikic
 * @date 2017-08-18
 * @since 1.0.0
 */
public interface BulkDataAdministrationService {
    void deleteAndInitElasticSearchIndexes() throws IndexingServiceNotAvailable;

    void deleteAndReindexLearningContent(long orgId) throws IndexingServiceNotAvailable;

    void deleteAndReindexRubrics(long orgId) throws IndexingServiceNotAvailable;

    void deleteAndReindexUsersAndGroups(long orgId) throws IndexingServiceNotAvailable;

    void indexDBData();
}
