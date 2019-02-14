package org.prosolo.services.indexing;


import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.elasticsearch.AbstractESIndexer;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-25
 * @since 1.0.0
 */
public interface RubricsESService extends AbstractESIndexer {

    void saveRubric(long orgId, Rubric rubric);

    void deleteRubric(long orgId, long rubricId);
}
