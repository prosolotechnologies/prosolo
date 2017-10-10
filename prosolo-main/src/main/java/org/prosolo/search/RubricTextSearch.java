package org.prosolo.search;

import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.RubricData;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-30
 * @since 1.0.0
 */
public interface RubricTextSearch extends AbstractManager {

    PaginatedResult<RubricData> searchRubrics(long orgId, String searchString,int page, int limit);
}
