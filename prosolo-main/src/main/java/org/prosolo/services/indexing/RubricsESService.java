package org.prosolo.services.indexing;

import org.prosolo.common.domainmodel.rubric.Rubric;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-25
 * @since 1.0.0
 */
public interface RubricsESService extends AbstractBaseEntityESService{

    void saveRubric(long rubricId);
}
