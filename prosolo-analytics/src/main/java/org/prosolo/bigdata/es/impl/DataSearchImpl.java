package org.prosolo.bigdata.es.impl;/**
 * Created by zoran on 31/07/16.
 */

import org.apache.log4j.Logger;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.es.DataSearch;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.elasticsearch.impl.AbstractESIndexerImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * zoran 31/07/16
 */
public class DataSearchImpl  extends AbstractESIndexerImpl implements DataSearch {
    private static Logger logger = Logger
            .getLogger(DataSearch.class.getName());
    @Override
    //TODO es migration - test this query
    public List<Long> findCredentialMembers(long credId,int page, int limit) {
            List<Long> members=new ArrayList<Long>();
            try {
                int start = 0;
                start = setStart(page, limit);

                BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();

                BoolQueryBuilder nestedFB = QueryBuilders.boolQuery();
                nestedFB.must(QueryBuilders.termQuery("credentials.id", credId));
                NestedQueryBuilder nestedFilter1 = QueryBuilders.nestedQuery("credentials", nestedFB, ScoreMode.None);
                nestedFilter1.innerHit(new InnerHitBuilder());
                bQueryBuilder.filter(nestedFilter1);

                String[] includes = {"id"};
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder
                        .query(bQueryBuilder)
                        .aggregation(AggregationBuilders.nested("nestedAgg", "credentials")
                                .subAggregation(AggregationBuilders.filter("filtered", QueryBuilders.termQuery("credentials.id", credId))))
                        .fetchSource(includes, null)
                        .from(start)
                        .size(limit);
                //                SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
                //                        .setTypes(ESIndexTypes.USER)
                //                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                //                        .setQuery(bQueryBuilder)
                //                        .addAggregation(AggregationBuilders.nested("nestedAgg", "credentials")
                //                                .subAggregation(AggregationBuilders.filter("filtered", QueryBuilders.termQuery("credentials.id", credId))))
                //                        .setFetchSource(includes, null);


                //searchRequestBuilder.setFrom(start).setSize(limit);


                //   System.out.println(searchRequestBuilder.toString());
                SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
                if (sResponse != null) {
                    for (SearchHit hit : sResponse.getHits()) {
                        members.add(Long.valueOf(hit.getSource().get("id").toString()));
                    }
                }
            } catch (Exception e) {
                logger.error("Error", e);
            }
            return members;
    }

    private int setStart(int page, int limit){
        int start = 0;
        if (page >= 0 && limit > 0) {
            start = page * limit;
        }
        return start;
    }
}
