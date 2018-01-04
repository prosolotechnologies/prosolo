package org.prosolo.search.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * @author stefanvuckovic
 * @date 2017-12-07
 * @since 1.2.0
 */
@Service("org.prosolo.search.LearningEvidenceTextSearch")
public class LearningEvidenceTextSearchImpl extends AbstractManagerImpl implements org.prosolo.search.LearningEvidenceTextSearch {

    private static final long serialVersionUID = 1088454252639176410L;

    private static Logger logger = Logger.getLogger(LearningEvidenceTextSearchImpl.class);

    @Inject private ESIndexer esIndexer;
    @Inject private LearningEvidenceManager learningEvidenceManager;

    private int setStart(int page, int limit){
        int start = 0;
        if (page >= 0 && limit > 0) {
            start = page * limit;
        }
        return start;
    }

    @Override
    public PaginatedResult<LearningEvidenceData> searchLearningEvidences(long orgId, long userId, List<Long> evidencesToExclude,
                                                      String searchTerm, int page, int limit) {

        PaginatedResult<LearningEvidenceData> response = new PaginatedResult<>();

        try {
            int start = setStart(page, limit);

            String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_EVIDENCE, orgId);
            Client client = ElasticSearchFactory.getClient();
            esIndexer.addMapping(client, indexName, ESIndexTypes.EVIDENCE);

            BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
            if(searchTerm != null && !searchTerm.isEmpty()) {
                QueryBuilder qb = QueryBuilders
                        .queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
                        .defaultOperator(QueryStringQueryBuilder.Operator.AND)
                        .field("name");
                bQueryBuilder.filter(qb);
            }

            bQueryBuilder.filter(termQuery("userId", userId));

            for (Long evidenceId : evidencesToExclude) {
                bQueryBuilder.mustNot(termQuery("id", evidenceId));
            }

            SearchRequestBuilder srb = client.prepareSearch(indexName)
                    .setTypes(ESIndexTypes.EVIDENCE)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery(bQueryBuilder)
                    .setFrom(start).setSize(limit)
                    .addSort("name", SortOrder.ASC);

            SearchResponse sResponse = srb.execute().actionGet();

            if (sResponse != null) {
                response.setHitsNumber(sResponse.getHits().getTotalHits());

                for (SearchHit hit : sResponse.getHits()) {
                    Long id = ((Integer) hit.getSource().get("id")).longValue();

                    try {
                        response.addFoundNode(learningEvidenceManager.getLearningEvidence(id));
                    } catch (DbConnectionException e) {
                        logger.error(e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return response;
    }
}
