package org.prosolo.search.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.search.RubricTextSearch;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-30
 * @since 1.0.0
 */

@Service("org.prosolo.search.RubricTextSearch")
public class RubricTextSearchImpl extends AbstractManagerImpl implements RubricTextSearch {

    private static Logger logger = Logger.getLogger(RubricTextSearchImpl.class);

    @Inject
    private RubricManager rubricManager;
    //@Inject
    //private ESIndexer esIndexer;

    private int setStart(int page, int limit) {
        int start = 0;
        if (page >= 0 && limit > 0) {
            start = page * limit;
        }
        return start;
    }

    @Override
    public PaginatedResult<RubricData> searchRubrics(long orgId, String searchString,
                                                     int page, int limit) {

        PaginatedResult<RubricData> response = new PaginatedResult<>();

        try {
            SearchResponse sResponse = getRubricsSearchResponse(orgId, searchString, page, limit);

            if(sResponse != null){
                response.setHitsNumber(sResponse.getHits().getTotalHits());

                for(SearchHit sh : sResponse.getHits()){
                    logger.info("ID: " + sh.getSourceAsMap().get("id"));
                    long id = Long.parseLong(sh.getSourceAsMap().get("id").toString());
                    RubricData rubricData =rubricManager.getOrganizationRubric(id);
                    response.addFoundNode(rubricData);
                }
            }

        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }

        return response;
    }

    private SearchResponse getRubricsSearchResponse(long orgId, String searchString,
                                                       int page, int limit) throws IOException {
        int start = 0;
        int size = 1000;
        if (limit > 0) {
            start = setStart(page, limit);
            size = limit;
        }

        QueryBuilder queryBuilder = QueryBuilders
                .queryStringQuery(searchString.toLowerCase() + "*")
                .field("name");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(queryBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(boolQueryBuilder)
                .from(start)
                .size(size)
                .sort(new FieldSortBuilder("name.sort").order(SortOrder.ASC));

        //System.out.println(searchRequestBuilder.toString());
        return ElasticSearchConnector.getClient().search(searchSourceBuilder, ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_RUBRIC_NAME, orgId), ESIndexTypes.RUBRIC);
    }
}
