package org.prosolo.bigdata.es;/**
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
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.user.User;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * zoran 31/07/16
 */
public class DataSearchImpl  extends AbstractESIndexer implements DataSearch{
    private static Logger logger = Logger
            .getLogger(DataSearch.class.getName());
    public void searchCredentialMembers (
            String searchTerm,  int page, int limit, long credId) {


            int start = 0;
            start = setStart(page, limit);

            Client client = ElasticSearchConnector.getClient();
        try{
            this.addMapping(client, ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
        }catch(IndexingServiceNotAvailable isna){
            logger.error(isna);
        }


            BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
            if(searchTerm != null && !searchTerm.isEmpty()) {
                QueryBuilder qb = QueryBuilders
                        .queryStringQuery(searchTerm.toLowerCase() + "*").useDisMax(true)
                        .defaultOperator(QueryStringQueryBuilder.Operator.AND)
                        .field("name").field("lastname");

                bQueryBuilder.must(qb);
            }



            BoolQueryBuilder nestedFB = QueryBuilders.boolQuery();
            nestedFB.must(QueryBuilders.termQuery("credentials.id", credId));
           /* if(instructorId != -1) {
                nestedFB.must(QueryBuilders.termQuery("credentials.instructorId", instructorId));
            }*/
            NestedQueryBuilder nestedFilter1 = QueryBuilders.nestedQuery("credentials",
                    nestedFB);
//					.innerHit(new QueryInnerHitBuilder());
            FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder,
                    nestedFilter1);
            //bQueryBuilder.must(termQuery("credentials.id", credId));


                String[] includes = {"id", "name", "lastname", "avatar", "position"};
                SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
                        .setTypes(ESIndexTypes.USER)
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setQuery(filteredQueryBuilder)
                        .addAggregation(AggregationBuilders.nested("nestedAgg").path("credentials")
                                .subAggregation(AggregationBuilders.filter("filtered")
                                        .filter(QueryBuilders.termQuery("credentials.id", credId))
                                        ))
                        .setFetchSource(includes, null);

				/*
				 * set instructor assign filter as a post filter so it does not influence
				 * aggregation results
				 */

                    nestedFilter1.innerHit(new QueryInnerHitBuilder());


                searchRequestBuilder.setFrom(start).setSize(limit);


                //System.out.println(searchRequestBuilder.toString());
                SearchResponse sResponse = searchRequestBuilder.execute().actionGet();





    }

    private int setStart(int page, int limit){
        int start = 0;
        if (page >= 0 && limit > 0) {
            start = page * limit;
        }
        return start;
    }
}
