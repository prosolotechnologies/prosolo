package org.prosolo.bigdata.es.impl;/**
 * Created by zoran on 31/07/16.
 */

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.support.QueryInnerHitBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.bigdata.es.AbstractESIndexer;
import org.prosolo.bigdata.es.DataSearch;
import org.prosolo.bigdata.es.ElasticSearchConnector;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.user.User;

import java.util.*;

/**
 * zoran 31/07/16
 */
public class DataSearchImpl  extends AbstractESIndexer implements DataSearch {
    private static Logger logger = Logger
            .getLogger(DataSearch.class.getName());
    @Override
    public List<Long> findCredentialMembers(long credId,int page, int limit) {
            List<Long> members=new ArrayList<Long>();
            int start = 0;
            start = setStart(page, limit);

            Client client = ElasticSearchConnector.getClient();
            BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();

            BoolQueryBuilder nestedFB = QueryBuilders.boolQuery();
            nestedFB.must(QueryBuilders.termQuery("credentials.id", credId));
             NestedQueryBuilder nestedFilter1 = QueryBuilders.nestedQuery("credentials",
                    nestedFB);
            FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder,
                    nestedFilter1);

                String[] includes = {"id"};
                SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
                        .setTypes(ESIndexTypes.USER)
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setQuery(filteredQueryBuilder)
                        .addAggregation(AggregationBuilders.nested("nestedAgg").path("credentials")
                                .subAggregation(AggregationBuilders.filter("filtered")
                                        .filter(QueryBuilders.termQuery("credentials.id", credId))
                                        ))
                        .setFetchSource(includes, null);
                    nestedFilter1.innerHit(new QueryInnerHitBuilder());


                searchRequestBuilder.setFrom(start).setSize(limit);


             //   System.out.println(searchRequestBuilder.toString());
                SearchResponse sResponse = searchRequestBuilder.execute().actionGet();
        if (sResponse != null) {
            for (SearchHit hit : sResponse.getHits()) {
                members.add(Long.valueOf(hit.getSource().get("id").toString()));
            }
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
