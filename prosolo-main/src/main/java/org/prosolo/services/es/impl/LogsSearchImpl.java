package org.prosolo.services.es.impl;/**
 * Created by zoran on 09/10/16.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.dal.pojo.LogsFilter;
import org.prosolo.bigdata.common.dal.pojo.LogsRecord;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.events.serialization.ContextDeserializer;
import org.prosolo.bigdata.common.events.serialization.LearningContextDeserializer;
import org.prosolo.bigdata.common.events.serialization.ServiceDeserializer;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.LearningContext;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.es.LogsSearch;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * zoran 09/10/16
 */
@Service("org.prosolo.services.es.LogsSearch")
public class LogsSearchImpl implements LogsSearch {

    //@Inject
    private ContextJsonParserService contextParser;

    @Inject
    public LogsSearchImpl(ContextJsonParserService contextParser){
        this.contextParser=contextParser;
    }
    @Override
    public List<LogsRecord> findLogsByFilter(LogsFilter logsFilter){
        Client client = ElasticSearchFactory.getClient();
        BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
        int bQueryMinShouldMatch=0;
        if(logsFilter.getCredentials().size()>0){
            //System.out.println("HAS CREDENTIALS:"+logsFilter.getCredentials().size());
            BoolQueryBuilder contextQueryBuilder =createContextQueryBuilder(logsFilter.getCredentials(),"Credential1",1);

            bQueryBuilder.should(contextQueryBuilder);
            bQueryMinShouldMatch++;
        }

        if(logsFilter.getCompetences().size()>0){
            //System.out.println("HAS COMPETENCES:"+logsFilter.getCompetences().size());
            BoolQueryBuilder contextQueryBuilder =createContextQueryBuilder(logsFilter.getCompetences(),"Competence1",2);
            bQueryBuilder.should(contextQueryBuilder);
            bQueryMinShouldMatch++;
        }
        if(logsFilter.getActivities().size()>0){
            //System.out.println("HAS Activities:"+logsFilter.getActivities().size());
            BoolQueryBuilder contextQueryBuilder = createContextQueryBuilder(logsFilter.getActivities(),"Activity1",3);
            bQueryBuilder.should(contextQueryBuilder);
            bQueryMinShouldMatch++;
        }
        if(logsFilter.getStudents().size()>0){
            //System.out.println("HAS STUDENTS:"+logsFilter.getStudents().size());
            BoolQueryBuilder studentsQueryBuilder=QueryBuilders.boolQuery();
            for(Long studentId: logsFilter.getStudents()){
                studentsQueryBuilder.should(QueryBuilders.matchQuery("actorId",studentId));
            }
            studentsQueryBuilder.minimumNumberShouldMatch(1);
            bQueryBuilder.should(studentsQueryBuilder);
            bQueryMinShouldMatch++;
        }
        if(logsFilter.getEventTypes().size()>0){
           // System.out.println("HAS EVENT TYPES:"+logsFilter.getEventTypes().size());
            BoolQueryBuilder eventTypesQueryBuilder=QueryBuilders.boolQuery();
            for(EventType eventType:logsFilter.getEventTypes()){
                eventTypesQueryBuilder.should(QueryBuilders.matchQuery("eventType",eventType.name()));
            }
            eventTypesQueryBuilder.minimumNumberShouldMatch(1);
            bQueryBuilder.should(eventTypesQueryBuilder);
            bQueryMinShouldMatch++;
        }
        if(logsFilter.getAfterDate()>0 && logsFilter.getBeforeDate()>0){
         //   System.out.println("HAS AFTER AND BEFORE DATE");
            RangeQueryBuilder datesQueryBuilder=QueryBuilders.rangeQuery("date")
                    .from(logsFilter.getAfterDate())
                    .to(logsFilter.getBeforeDate())
                    .includeLower(false)
                    .includeUpper(false);
            bQueryBuilder.should(datesQueryBuilder);
            bQueryMinShouldMatch++;
        }
        bQueryBuilder.minimumNumberShouldMatch( bQueryMinShouldMatch);
        SearchRequestBuilder searchRequestBuilder=client.prepareSearch(ESIndexNames.INDEX_LOGS)
                .setTypes(ESIndexTypes.LOG)
                .setQuery( bQueryBuilder)
                .setFrom(logsFilter.getFrom())
                .setSize(logsFilter.getSize())
                .addSort("timestamp", SortOrder.DESC);

        //System.out.println("QUERY:"+ searchRequestBuilder.toString());
        SearchResponse sResponse=searchRequestBuilder.execute().actionGet();

      //  System.out.println("EXECUTED");
        return buildLogsResults(sResponse);
    }
    private BoolQueryBuilder createContextQueryBuilder(List<Long> objectIds, String objectType, int contextLevel){
        BoolQueryBuilder contextQueryBuilder = QueryBuilders.boolQuery();
        String contextKey="learningContext";
        for(int i=0;i<contextLevel;i++){
            contextKey=contextKey+".context";
        }
        for(Long objId:objectIds){
            BoolQueryBuilder objQueryBuilder=QueryBuilders.boolQuery();
            objQueryBuilder.should(QueryBuilders.matchQuery(contextKey+".object_type",objectType));
            objQueryBuilder.should(QueryBuilders.matchQuery(contextKey+".id",objId));
            objQueryBuilder.minimumNumberShouldMatch(2);
            contextQueryBuilder.should(objQueryBuilder);
        }
        contextQueryBuilder.minimumNumberShouldMatch(1);
        return contextQueryBuilder;
    }

    private List<LogsRecord> buildLogsResults(SearchResponse sResponse){
        List<LogsRecord> logs=new ArrayList();
        if(sResponse != null) {
          //  System.out.println("HAS RESPONSE");

            SearchHits searchHits = sResponse.getHits();
            if(searchHits != null) {
                for (SearchHit hit : sResponse.getHits()) {
                    String actorId=hit.getSource().get("actorId").toString();
                    String courseId=hit.getSource().get("courseId").toString();
                    String eventTypeString=hit.getSource().get("eventType").toString();
                    String learningContextString=hit.getSource().get("learningContext").toString();
                    String timestamp=hit.getSource().get("timestamp").toString();
                    String sessionId=hit.getSource().get("sessionId").toString();
                    String date=hit.getSource().get("date").toString();

                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(LearningContext.class,
                            new LearningContextDeserializer());

                    gsonBuilder.registerTypeAdapter(Context.class,
                            new ContextDeserializer());
                    gsonBuilder.registerTypeAdapter(org.prosolo.common.event.context.Service.class,
                            new ServiceDeserializer());

                    Gson gson = gsonBuilder.create();
                    LearningContext learningContext = gson.fromJson(learningContextString, LearningContext.class);
                    // System.out.println(timestamp);
                   // System.out.println("HIT:actor:"+actorId+" course:"+courseId+" eventType:"+eventTypeString+" timestamp:"+timestamp+" date:"+date+" context:"+learningContextString);
                    LogsRecord log=new LogsRecord(Long.valueOf(date), Long.valueOf(timestamp), Long.valueOf(actorId), sessionId, EventType.valueOf(eventTypeString), learningContext);
                    logs.add(log);
                }
            }
        }
        return logs;
    }
}
