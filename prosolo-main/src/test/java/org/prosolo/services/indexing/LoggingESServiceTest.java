package org.prosolo.services.indexing;/**
 * Created by zoran on 28/09/16.
 */

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;

import java.util.ArrayList;
import java.util.List;

/**
 * zoran 28/09/16
 */
public class LoggingESServiceTest {
    private static Client client;
    @BeforeClass
    public static void initializeClient(){
        String indexName = ESIndexNames.INDEX_LOGS;
        String indexType = ESIndexTypes.LOG;
        client = ElasticSearchFactory.getClient();

        System.out.println("ES CLIENT INITIALIZED");
    }

    @Test
    public void testLogsFiltering(){

        List<Long> actors=new ArrayList();
        actors.add(24l);
        actors.add(12l);
        List<Long> courses=new ArrayList();
        courses.add(1l);
        courses.add(5l);
        BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder courseQueryBuilder = QueryBuilders.boolQuery();
        for (Long cId:courses) {
            courseQueryBuilder.should(QueryBuilders.matchQuery("courseId",cId));
        }
        courseQueryBuilder.minimumNumberShouldMatch(1);
        bQueryBuilder.should(courseQueryBuilder);
        BoolQueryBuilder userQueryBuilder=QueryBuilders.boolQuery();

        for (Long aId:actors) {
            userQueryBuilder.should(QueryBuilders.matchQuery("actorId",aId));
        }
        userQueryBuilder.minimumNumberShouldMatch(1);
        bQueryBuilder.should(userQueryBuilder);
        bQueryBuilder.minimumNumberShouldMatch(2);
        System.out.println("QUERY:"+ bQueryBuilder.toString());
        SearchResponse sResponse=client.prepareSearch(ESIndexNames.INDEX_LOGS)
                .setTypes(ESIndexTypes.LOG)
                .setQuery( bQueryBuilder)
                        .setFrom(0)
                        .setSize(100)
                        .execute().actionGet();
        System.out.println("EXECUTED");
        if(sResponse != null) {
            System.out.println("HAS RESPONSE");

            SearchHits searchHits = sResponse.getHits();
            if(searchHits != null) {
                for (SearchHit hit : sResponse.getHits()) {
                    String actorId=hit.getSource().get("actorId").toString();
                    String courseId=hit.getSource().get("courseId").toString();
                    System.out.println("HIT:actor:"+actorId+" course:"+courseId);
                }
            }
        }

    }
}
