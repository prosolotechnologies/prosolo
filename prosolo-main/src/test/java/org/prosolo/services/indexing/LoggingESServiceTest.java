package org.prosolo.services.indexing;/**
 * Created by zoran on 28/09/16.
 */

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.elasticsearch.client.ESRestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private static Logger logger = Logger.getLogger(LoggingESServiceTest.class);

    private static ESRestClient client;
    private static List<Long> actors;
    private static List<Long> courses;
    private static LogsFilter f1;
    @BeforeClass
    public static void initializeClient(){
        String indexName = ESIndexNames.INDEX_LOGS;
        String indexType = ESIndexTypes.LOG;
        client = ElasticSearchConnector.getClient();

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
        if (logsFilter.getCredentials().size()>0) {
            System.out.println("HAS CREDENTIALS:"+logsFilter.getCredentials().size());
            BoolQueryBuilder contextQueryBuilder =createContextQueryBuilder(logsFilter.getCredentials(),"Credential1",1);

            bQueryBuilder.should(contextQueryBuilder);
            bQueryMinShouldMatch++;
        }

        if (logsFilter.getCompetences().size()>0) {
            System.out.println("HAS COMPETENCES:"+logsFilter.getCompetences().size());
            BoolQueryBuilder contextQueryBuilder =createContextQueryBuilder(logsFilter.getCompetences(),"Competence1",2);
            bQueryBuilder.should(contextQueryBuilder);
            bQueryMinShouldMatch++;
        }
        if (logsFilter.getActivities().size()>0) {
            System.out.println("HAS Activities:"+logsFilter.getActivities().size());
            BoolQueryBuilder contextQueryBuilder = createContextQueryBuilder(logsFilter.getActivities(),"Activity1",3);
            bQueryBuilder.should(contextQueryBuilder);
            bQueryMinShouldMatch++;
        }
        if (logsFilter.getStudents().size()>0) {
            System.out.println("HAS STUDENTS:"+logsFilter.getStudents().size());
            BoolQueryBuilder studentsQueryBuilder=QueryBuilders.boolQuery();
            for(Long studentId: logsFilter.getStudents()){
                studentsQueryBuilder.should(QueryBuilders.matchQuery("actorId",studentId));
            }
            studentsQueryBuilder.minimumShouldMatch(1);
            bQueryBuilder.should(studentsQueryBuilder);
            bQueryMinShouldMatch++;
        }
        if (logsFilter.getEventTypes().size()>0) {
            System.out.println("HAS EVENT TYPES:"+logsFilter.getEventTypes().size());
            BoolQueryBuilder eventTypesQueryBuilder=QueryBuilders.boolQuery();
            for(EventType eventType:logsFilter.getEventTypes()){
                eventTypesQueryBuilder.should(QueryBuilders.matchQuery("eventType",eventType.name()));
            }
            eventTypesQueryBuilder.minimumShouldMatch(1);
            bQueryBuilder.should(eventTypesQueryBuilder);
            bQueryMinShouldMatch++;
        }
        if (logsFilter.getAfterDate()>0 && logsFilter.getBeforeDate()>0) {
            System.out.println("HAS AFTER AND BEFORE DATE");
            RangeQueryBuilder datesQueryBuilder=QueryBuilders.rangeQuery("date")
                    .from(logsFilter.getAfterDate())
                    .to(logsFilter.getBeforeDate())
                    .includeLower(false)
                    .includeUpper(false);
            bQueryBuilder.should(datesQueryBuilder);
            bQueryMinShouldMatch++;
        }

        bQueryBuilder.minimumShouldMatch( bQueryMinShouldMatch);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(bQueryBuilder)
                .from(logsFilter.getFrom())
                .size(logsFilter.getSize())
                .sort(new FieldSortBuilder("timestamp").order(SortOrder.DESC));

        try {
            SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ESIndexNames.INDEX_LOGS, ESIndexTypes.LOG);
            System.out.println("EXECUTED");
            printSearchResponse(sResponse);
        } catch (IOException e) {
            logger.error("Error", e);
        }
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
            objQueryBuilder.minimumShouldMatch(2);
            contextQueryBuilder.should(objQueryBuilder);
        }
        contextQueryBuilder.minimumShouldMatch(1);
        return contextQueryBuilder;
    }

    @Test
    public void testLogsFilteringForLongAndCourse(){

        BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder courseQueryBuilder = QueryBuilders.boolQuery();
        for (Long cId:courses) {
            courseQueryBuilder.should(QueryBuilders.matchQuery("courseId",cId));
        }
        courseQueryBuilder.minimumShouldMatch(1);
        bQueryBuilder.should(courseQueryBuilder);
        BoolQueryBuilder userQueryBuilder=QueryBuilders.boolQuery();

        for (Long aId:actors) {
            userQueryBuilder.should(QueryBuilders.matchQuery("actorId",aId));
        }
        userQueryBuilder.minimumShouldMatch(1);
        bQueryBuilder.should(userQueryBuilder);
        bQueryBuilder.minimumShouldMatch(2);
        System.out.println("QUERY:"+ bQueryBuilder.toString());

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(bQueryBuilder)
                .from(0)
                .size(100);

        try {
            SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ESIndexNames.INDEX_LOGS, ESIndexTypes.LOG);
            System.out.println("EXECUTED");
            printSearchResponse(sResponse);
        } catch (IOException e) {
            logger.error("Error", e);
        }
    }



    @Test
    public void testLogsFilteringByContext(){
        BoolQueryBuilder qb=QueryBuilders.boolQuery();

        BoolQueryBuilder qbL1=QueryBuilders.boolQuery();

        QueryBuilder qb1= QueryBuilders.matchQuery("learningContext.context.object_type","Long");
        QueryBuilder qb2= QueryBuilders.matchQuery("learningContext.context.id","1");
        qbL1.should(qb1).should(qb2).minimumShouldMatch(2);


        qb.should(qbL1);
        System.out.println("QUERY:"+ qb.toString());

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(qb)
                .from(0)
                .size(100);

        try {
            SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ESIndexNames.INDEX_LOGS, ESIndexTypes.LOG);
            System.out.println("EXECUTED testLogsFilteringByContext");
            printSearchResponse(sResponse);
        } catch (IOException e) {
            logger.error("Error", e);
        }
    }
    private void printSearchResponse(SearchResponse sResponse){
        if(sResponse != null) {
            System.out.println("HAS RESPONSE");

            SearchHits searchHits = sResponse.getHits();
            if(searchHits != null) {
                for (SearchHit hit : sResponse.getHits()) {
                    String actorId=hit.getSourceAsMap().get("actorId").toString();
                    String courseId=hit.getSourceAsMap().get("courseId").toString();
                    String eventType=hit.getSourceAsMap().get("eventType").toString();
                    String learningContext=hit.getSourceAsMap().get("learningContext").toString();
                    String timestamp=hit.getSourceAsMap().get("timestamp").toString();
                    String date=hit.getSourceAsMap().get("date").toString();
                   // System.out.println(timestamp);
                    System.out.println("HIT:actor:"+actorId+" course:"+courseId+" eventType:"+eventType+" timestamp:"+timestamp+" date:"+date+" context:"+learningContext);
                }
            }
        }
    }
}
