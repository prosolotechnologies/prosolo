package org.prosolo.services.indexing;


/**
 * @author Bojan Trifkovic
 * @date 2017-08-25
 * @since 1.0.0
 */
public interface RubricsESService extends AbstractBaseEntityESService {

    void saveRubric(long orgId, long rubricId);

    void deleteRubric(long orgId, long rubricId);
}
