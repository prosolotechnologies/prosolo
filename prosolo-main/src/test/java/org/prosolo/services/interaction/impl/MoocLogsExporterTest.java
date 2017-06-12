package org.prosolo.services.interaction.impl;/**
 * Created by zoran on 29/12/15.
 */

import static org.prosolo.common.domainmodel.events.EventType.Comment;
import static org.prosolo.common.domainmodel.events.EventType.EVALUATION_ACCEPTED;
import static org.prosolo.common.domainmodel.events.EventType.EVALUATION_GIVEN;
import static org.prosolo.common.domainmodel.events.EventType.EVALUATION_REQUEST;
import static org.prosolo.common.domainmodel.events.EventType.JOIN_GOAL_INVITATION;
import static org.prosolo.common.domainmodel.events.EventType.JOIN_GOAL_INVITATION_ACCEPTED;
import static org.prosolo.common.domainmodel.events.EventType.JOIN_GOAL_REQUEST;
import static org.prosolo.common.domainmodel.events.EventType.JOIN_GOAL_REQUEST_APPROVED;
import static org.prosolo.common.domainmodel.events.EventType.JOIN_GOAL_REQUEST_DENIED;
import static org.prosolo.common.domainmodel.events.EventType.Like;
import static org.prosolo.common.domainmodel.events.EventType.SEND_MESSAGE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

//import com.mongodb.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableProducerImpl;
import org.prosolo.core.spring.SpringConfig;
import org.prosolo.services.nodes.CourseManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * zoran 29/12/15
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ SpringConfig.class })
public class MoocLogsExporterTest {
    private static Logger logger = Logger.getLogger(MoocLogsExporterTest.class);
    static final String MOOC_DB_URL = "jdbc:mysql://localhost/prosolo_temp";
    // Database credentials
    static final String USER = "root";
    static final String PASS = "root";
    static Connection conn = null;
    @Inject
    private CourseManager courseManager;
    private EventType[] interactions=new EventType[]{
            Comment, EVALUATION_REQUEST,EVALUATION_ACCEPTED, EVALUATION_GIVEN,
            JOIN_GOAL_INVITATION, JOIN_GOAL_INVITATION_ACCEPTED,JOIN_GOAL_REQUEST,
            JOIN_GOAL_REQUEST_APPROVED, JOIN_GOAL_REQUEST_DENIED,
            Like,SEND_MESSAGE};
    @BeforeClass
    public static void initializeDBConnection(){
        try {
            // STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            // STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(MOOC_DB_URL, USER, PASS);
        } catch (SQLException se) {
            // Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            // Handle errors for Class.forName
            e.printStackTrace();
        }
    }
    @AfterClass
    public static void closeDBConnections(){
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }//
    }
//    @Test
//    public void extractUserActivitiesFromMongoLogsTest(){
//        System.out.println("Extract user activities from mongo logs");
//        ReliableProducerImpl reliableProducer=new ReliableProducerImpl();
//        reliableProducer.setQueue(QueueNames.LOGS.name().toLowerCase());
//        reliableProducer.startAsynchronousPublisher();
//
//        MongoDBServersConfig dbServersConfig= Settings.getInstance().config.mongoDatabase.dbServersConfig;
//        List<ServerAddress> serverAddresses=new ArrayList<ServerAddress>();
//        for(MongoDBServerConfig dbsConfig:dbServersConfig.dbServerConfig){
//            ServerAddress serverAddress;
//            try {
//                serverAddress = new ServerAddress(dbsConfig.dbHost,dbsConfig.dbPort);
//                serverAddresses.add(serverAddress);
//            } catch (UnknownHostException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//        }
//        MongoClient mongoClient=new MongoClient(serverAddresses);
//        DB db=mongoClient.getDB(Settings.getInstance().config.mongoDatabase.dbName);
//        DBCollection eventsCollection= db.getCollection("log_events_observed");
//        //DBObject query=new BasicDBObject();
//        //query.put("actorId",2);
//        int count= eventsCollection.find().count();
//        logger.info("COLLECTION HAS EVENTS:"+count);
//        int counter = 0;
//        int batchSize = 1000;
//        int batchesCounter=0;
//        while(counter < count) {
//
//            batchesCounter++;
//            if((batchesCounter % 10)==0){
//                logger.info(batchesCounter*1000+"/"+count);
//            }
//            DBCursor cursor = eventsCollection.find();
//            cursor.skip(counter);
//            cursor.limit(batchSize);
//            for(int i = 0; i<batchSize; i++) {
//                counter++;
//                if (!cursor.hasNext()) {
//                    break;
//                }
//                DBObject dbObject=cursor.next();
//                String eventType=dbObject.get("eventType").toString();
//
//                boolean ignore=false;
//                boolean socialinteraction=false;
//                if(eventType.equals("TwitterPost")|| eventType.equals("LOGOUT") || eventType.equals("SESSIONENDED")	){
//                    ignore=true;
//                }
//                if(eventType.equals("ENROLL_COURSE") || eventType.equals("COURSE_WITHDRAWN")){
//                    ignore=true;
//                }
//                if(dbObject.containsKey("objectType") && dbObject.get("objectType")!=null){
//                    String objectType=dbObject.get("objectType").toString();
//                    if(objectType.equals("MOUSE_CLICK")){
//                        ignore=true;
//                    }
//                }
//
//                if(!ignore){
//                    wrapMessageAndSend(reliableProducer, dbObject);
//                }
//
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    private Long extractCourseIdForUsedResource(String objectType, long objectId, String targetType, long targetId) {
        Map<String, Long> types=new HashMap<String, Long>();
        types.put(objectType, objectId);
        types.put(targetType, targetId);
        if(types.containsKey("TargetLearningGoal")){
            return courseManager.findCourseIdForTargetLearningGoal(types.get("TargetLearningGoal"));
        }else if(types.containsKey("TargetCompetence")){
            return courseManager.findCourseIdForTargetCompetence(types.get("TargetCompetence"));
        }else if(types.containsKey("TargetActivity")){
            return courseManager.findCourseIdForTargetActivity(types.get("TargetActivity"));
        }
        /*else  if(types.containsKey("NodeRequest")){

        }else  if(types.containsKey("PostSocialActivity")){

        }else{
            System.out.println("THIS IS NOT COVERED YET...");
        }*/
        return 1l;
    }
    private void wrapMessageAndSend(ReliableProducerImpl reliableProducer, JSONObject logObject){
      /*  LogMessage message = new LogMessage();
        Gson g=new Gson();
        DBObject parameters=(DBObject) logObject.get("parameters");
        String ip="";
        if(parameters.containsField("ip")){
            ip=(String) parameters.get("ip");
        }
        message.setTimestamp((long) logObject.get("timestamp"));
        message.setEventType((String) logObject.get("eventType"));
        message.setActorId((long) logObject.get("actorId"));
        message.setObjectType((String) logObject.get("objectType"));
        message.setObjectId((long) logObject.get("objectId"));
        message.setObjectTitle((String) logObject.get("objectTitle"));
        message.setTargetType((String) logObject.get("targetType"));
        message.setTargetId((long) logObject.get("targetId"));
        message.setReasonType((String) logObject.get("reasonType"));
        message.setLink((String) logObject.get("link"));
       long courseId=extractCourseIdForUsedResource((String) logObject.get("objectType"), (long) logObject.get("objectId"),
                (String) logObject.get("targetType"), (long) logObject.get("targetId"), (String) logObject.get("reasonType"));
        message.setCourseId(courseId);
        long targetUserId =0;
        EventType eventType=EventType.valueOf((String) logObject.get("eventType"));
        if(Arrays.asList(interactions).contains(eventType)) {
        //   System.out.println("INTERACTION SHOULD BE PROCESSED:" + logObject.toString());
            targetUserId = extractSocialInteractionTargetUser(logObject, eventType);
            message.setTargetUserId(targetUserId);
        }

        message.setParameters(parameters);
        //wrapMessageAndSend(reliableProducer, message, ip);
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setSender(ip);
        wrapper.setMessage(message);
        wrapper.setTimecreated(System.currentTimeMillis());
        String msg = gson.create().toJson(wrapper);

        reliableProducer.send(msg);*/
    }
    //private Long extractSocialInteractionTargetUser(DBObject logObject, EventType eventType){
       /* long actorId=(long) logObject.get("actorId");
        String objectType=(String) logObject.get("objectType");
        long objectId= (long) logObject.get("objectId");
        String targetType=   (String) logObject.get("targetType");
        long targetId= (long) logObject.get("targetId");
        String reasonType= (String) logObject.get("reasonType");

        Long targetUserId=(long)0;
        if(objectType.equals("NodeRequest")){
            targetUserId=getRequestMaker(actorId, objectId);
        }else if(objectType.equals("EvaluationSubmission")){

            targetUserId=getEvaluationSubmissionRequestMaker(actorId,objectId);
        }else {
            if(eventType.equals(EventType.SEND_MESSAGE)){
                DBObject parameters=(DBObject) logObject.get("parameters");
           //     System.out.println("SEND MESSAGE:"+logObject.toString()+" USER:"+parameters.get("user"));
               targetUserId=  Long.valueOf(parameters.get("user").toString());
            }else if(eventType.equals(EventType.Like)){

                 if(objectType.equals("PostSocialActivity")||objectType.equals("SocialActivityComment")||
                        objectType.equals("TwitterPostSocialActivity")||objectType.equals("UserSocialActivity")||
                                objectType.equals("UserSocialActivity")||objectType.equals("NodeSocialActivity")||
                        objectType.equals("NodeComment")){
                        targetUserId=getSocialActivityMaker(actorId,objectId);
             //       System.out.println("MOOC GET FOR SOCIAL ACTIVITY :"+actorId+" targetUser:"+targetUserId+" objectId:"+objectId);
                }
            }else if(eventType.equals(EventType.Comment)){
                if(objectType.equals("SocialActivityComment")){
                    targetUserId=getSocialActivityMaker(actorId,targetId);
               //     System.out.println("GET FOR COMMENT ON SOCIAL ACTIVITY:"+actorId+" targetUser:"+targetUserId+" objectId:"+objectId);
                }
            }
        }

        return targetUserId;*/
   // }
    private long getSocialActivityMaker(long actorId, long objectId){
        long makerId=0;
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            String sql="Select maker from social_activity where id="+objectId;
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                makerId=rs.getLong("maker");
               // long sentTo=rs.getLong("sent_to");
              ///  System.out.println("ACTOR:"+actorId+" MAKER:"+makerId+" sentTo:"+actorId);
               /* if(maker==actorId){
                    makerId=sentTo;
                }else{
                    makerId=maker;
                }*/
            }
            rs.close();
            stmt.close();
        }catch(java.sql.SQLException se){
            se.printStackTrace();
        }
        return makerId;
    }
    private long getRequestMaker(long actorId, long objectId){
        long makerId=0;
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            String sql="Select maker, sent_to from request where id="+objectId;
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                long maker=rs.getLong("maker");
                long sentTo=rs.getLong("sent_to");
              //  System.out.println("ACTOR:"+actorId+" MAKER:"+maker+" sentTo:"+sentTo);
               if(maker==actorId){
                   makerId=sentTo;
               }else{
                   makerId=maker;
               }
            }
            rs.close();
            stmt.close();
        }catch(java.sql.SQLException se){
            se.printStackTrace();
        }
        return makerId;

    }
    private long getEvaluationSubmissionRequestMaker(long actorId, long objectId){
        long makerId=0;
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            String sql="SELECT request.maker, request.sent_to FROM prosolo_temp.request as request LEFT JOIN prosolo_temp.evaluation_submission as s on s.request=request.id where  request.id="+objectId;
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                long maker=rs.getLong("maker");
                long sentTo=rs.getLong("sent_to");
               // System.out.println("getEvaluationSubmissionRequestMaker ACTOR:"+actorId+" MAKER:"+maker+" sentTo:"+sentTo);
                if(maker==actorId){
                    makerId=sentTo;
                }else{
                    makerId=maker;
                }
            }
            rs.close();
            stmt.close();
        }catch(java.sql.SQLException se){
            se.printStackTrace();
        }
        return makerId;

    }
}
