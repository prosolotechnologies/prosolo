package org.prosolo.services.indexing;/**
 * Created by zoran on 28/09/16.
 */

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.activities.events.EventType;


import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * zoran 28/09/16
 */
class LogsFilter{
    private List<Long> credentials=new ArrayList();
    private List<Long> competences=new ArrayList();
    private List<Long> activities=new ArrayList();
    private List<Long> students=new ArrayList();
    private List<EventType> eventTypes=new ArrayList();
    private Long afterDate;
    private Long beforeDate;
    private int from=0;
    private int size=10;

    public int getFrom() {     return from;    }
    public void setFrom(int from) {        this.from = from;    }
    public int getSize() {        return size;    }
    public void setSize(int size) {        this.size = size;    }
    public List<Long> getCredentials() {return credentials; }
    public void setCredentials(List<Long> credentials) {    this.credentials = credentials;    }
    public void addCredential(Long credential1){this.credentials.add(credential1);}
    public List<Long> getCompetences() {        return competences;    }
    public void setCompetences(List<Long> competences) {        this.competences = competences;    }
    public void addCompetence(Long competence1){this.competences.add(competence1);}
    public List<Long> getActivities() {        return activities;    }
    public void setActivities(List<Long> activities) {        this.activities = activities;    }
    public void addActivity(Long activity){this.activities.add(activity);}
    public List<Long> getStudents() {        return students;    }
    public void setStudents(List<Long> students) {        this.students = students;    }
    public void addStudent(Long student){this.students.add(student);}
    public List<EventType> getEventTypes() {        return eventTypes;    }
    public void setEventTypes(List<EventType> eventTypes) {        this.eventTypes = eventTypes;    }
    public void addEventType(EventType eventType){this.eventTypes.add(eventType);}
    public Long getAfterDate() {return afterDate;    }
    public void setAfterDate(Long afterDate) {        this.afterDate = afterDate;    }
    public Long getBeforeDate() {return beforeDate;    }
    public void setBeforeDate(Long beforeDate) { this.beforeDate = beforeDate;    }
  }
public class LoggingESServiceTest {
    private static Client client;
    private static List<Long> actors;
    private static List<Long> courses;
    private static LogsFilter f1;
    @BeforeClass
    public static void initializeClient(){
        String indexName = ESIndexNames.INDEX_LOGS;
        String indexType = ESIndexTypes.LOG;
        client = ElasticSearchFactory.getClient();

        System.out.println("ES CLIENT INITIALIZED");
        actors=new ArrayList();
       // actors.add(24l);
       // actors.add(12l);
        actors.add(6l);
        actors.add(20l);
        actors.add(24l);

        courses=new ArrayList();
        courses.add(1l);
        courses.add(5l);

        f1=new LogsFilter();
        //f1.addCredential(1l);//1, 6,5,3,7
        f1.addCredential(3l);

         f1.addCompetence(3l);
        //f1.addCompetence(6l);

      //  f1.addActivity(17l);
       // f1.addActivity(18l);

        //actors 13, 24, 6
      //  f1.addStudent(13l);
      //  f1.addStudent(24l);

        //eventType: ChangeProgress, Typed_Response_Posted, Completion context, NAVIGATE
      //  f1.addEventType(EventType.ChangeProgress);
      //  f1.addEventType(EventType.Typed_Response_Posted);

        f1.setAfterDate(17083l);
        f1.setBeforeDate(17085l);

    }

    @Test
    public void testLogsFilterObject(){
        LogsFilter logsFilter=f1;
        BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
        int bQueryMinShouldMatch=0;
        if(logsFilter.getCredentials().size()>0){
            System.out.println("HAS CREDENTIALS:"+logsFilter.getCredentials().size());
            BoolQueryBuilder contextQueryBuilder =createContextQueryBuilder(logsFilter.getCredentials(),"Credential1",1);

            bQueryBuilder.should(contextQueryBuilder);
            bQueryMinShouldMatch++;
        }

        if(logsFilter.getCompetences().size()>0){
            System.out.println("HAS COMPETENCES:"+logsFilter.getCompetences().size());
            BoolQueryBuilder contextQueryBuilder =createContextQueryBuilder(logsFilter.getCompetences(),"Competence1",2);
            bQueryBuilder.should(contextQueryBuilder);
            bQueryMinShouldMatch++;
        }
        if(logsFilter.getActivities().size()>0){
            System.out.println("HAS Activities:"+logsFilter.getActivities().size());
            BoolQueryBuilder contextQueryBuilder = createContextQueryBuilder(logsFilter.getActivities(),"Activity1",3);
            bQueryBuilder.should(contextQueryBuilder);
            bQueryMinShouldMatch++;
        }
        if(logsFilter.getStudents().size()>0){
            System.out.println("HAS STUDENTS:"+logsFilter.getStudents().size());
            BoolQueryBuilder studentsQueryBuilder=QueryBuilders.boolQuery();
            for(Long studentId: logsFilter.getStudents()){
                studentsQueryBuilder.should(QueryBuilders.matchQuery("actorId",studentId));
            }
            studentsQueryBuilder.minimumNumberShouldMatch(1);
            bQueryBuilder.should(studentsQueryBuilder);
            bQueryMinShouldMatch++;
        }
        if(logsFilter.getEventTypes().size()>0){
            System.out.println("HAS EVENT TYPES:"+logsFilter.getEventTypes().size());
            BoolQueryBuilder eventTypesQueryBuilder=QueryBuilders.boolQuery();
            for(EventType eventType:logsFilter.getEventTypes()){
                eventTypesQueryBuilder.should(QueryBuilders.matchQuery("eventType",eventType.name()));
            }
            eventTypesQueryBuilder.minimumNumberShouldMatch(1);
            bQueryBuilder.should(eventTypesQueryBuilder);
            bQueryMinShouldMatch++;
        }
        if(logsFilter.getAfterDate()>0 && logsFilter.getBeforeDate()>0){
            System.out.println("HAS AFTER AND BEFORE DATE");
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

        System.out.println("QUERY:"+ searchRequestBuilder.toString());
        SearchResponse sResponse=searchRequestBuilder
                .execute().actionGet();
        System.out.println("EXECUTED");
        printSearchResponse(sResponse);
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

    @Test
    public void testLogsFilteringForLongAndCourse(){

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
        printSearchResponse(sResponse);
    }



    @Test
    public void testLogsFilteringByContext(){
        BoolQueryBuilder qb=QueryBuilders.boolQuery();

        BoolQueryBuilder qbL1=QueryBuilders.boolQuery();

        QueryBuilder qb1= QueryBuilders.matchQuery("learningContext.context.object_type","Long");
        QueryBuilder qb2= QueryBuilders.matchQuery("learningContext.context.id","1");
        qbL1.should(qb1).should(qb2).minimumNumberShouldMatch(2);


        qb.should(qbL1);
        System.out.println("QUERY:"+ qb.toString());
        SearchResponse sResponse=client.prepareSearch(ESIndexNames.INDEX_LOGS)
                .setTypes(ESIndexTypes.LOG)
                .setQuery( qb)
                .setFrom(0)
                .setSize(100)
                .execute().actionGet();
        System.out.println("EXECUTED testLogsFilteringByContext");
        printSearchResponse(sResponse);
    }
    private void printSearchResponse(SearchResponse sResponse){
        if(sResponse != null) {
            System.out.println("HAS RESPONSE");

            SearchHits searchHits = sResponse.getHits();
            if(searchHits != null) {
                for (SearchHit hit : sResponse.getHits()) {
                    String actorId=hit.getSource().get("actorId").toString();
                    String courseId=hit.getSource().get("courseId").toString();
                    String eventType=hit.getSource().get("eventType").toString();
                    String learningContext=hit.getSource().get("learningContext").toString();
                    String timestamp=hit.getSource().get("timestamp").toString();
                    String date=hit.getSource().get("date").toString();
                   // System.out.println(timestamp);
                    System.out.println("HIT:actor:"+actorId+" course:"+courseId+" eventType:"+eventType+" timestamp:"+timestamp+" date:"+date+" context:"+learningContext);
                }
            }
        }
    }
}
