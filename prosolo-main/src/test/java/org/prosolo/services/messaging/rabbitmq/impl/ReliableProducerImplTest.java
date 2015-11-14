package org.prosolo.services.messaging.rabbitmq.impl;

 

import static org.junit.Assert.fail;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.prosolo.app.Settings;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.messaging.MessageWrapperAdapter;
import org.prosolo.common.messaging.data.AnalyticalServiceMessage;
import org.prosolo.common.messaging.data.LogMessage;
import org.prosolo.common.messaging.data.MessageWrapper;
import org.prosolo.common.messaging.rabbitmq.QueueNames;
import org.prosolo.common.messaging.rabbitmq.ReliableConsumer;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableConsumerImpl;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableProducerImpl;
import org.prosolo.config.MongoDBServerConfig;
import org.prosolo.config.MongoDBServersConfig;
import org.prosolo.services.interaction.impl.AnalyticalServiceDataFactoryImpl;
 

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

/**
@author Zoran Jeremic Sep 7, 2014
 */

public class ReliableProducerImplTest{
	private static Logger logger = Logger.getLogger(ReliableProducerImplTest.class);
	@Ignore
	@Test
	public void generateAnalyticsFromMongoTest() {
		ReliableProducerImpl reliableProducer = new ReliableProducerImpl();
		reliableProducer.setQueue(QueueNames.ANALYTICS.name().toLowerCase());
		reliableProducer.startAsynchronousPublisher();

		MongoDBServersConfig dbServersConfig = Settings.getInstance().config.mongoDatabase.dbServersConfig;
		List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();
		for (MongoDBServerConfig dbsConfig : dbServersConfig.dbServerConfig) {
			ServerAddress serverAddress;
			try {
				serverAddress = new ServerAddress(dbsConfig.dbHost, dbsConfig.dbPort);
				serverAddresses.add(serverAddress);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		MongoClient mongoClient = new MongoClient(serverAddresses);
		DB db = mongoClient.getDB(Settings.getInstance().config.mongoDatabase.dbName);
		DBCollection eventsCollection = db.getCollection("log_events_observed");
		DBObject query = new BasicDBObject();
		query.put("actorId", 2);
		int count = eventsCollection.find().count();
		System.out.println("COLLECTION HAS EVENTS:" + count);
		int counter = 0;
		int batchSize = 100;
		List<EventType> supported = Arrays.asList(new EventType[] { EventType.Registered, EventType.LOGIN,
				EventType.NAVIGATE, EventType.SELECT_GOAL, EventType.SELECT_COMPETENCE });
		while (counter < count) {
			DBCursor cursor = eventsCollection.find();
			cursor.skip(counter);
			cursor.limit(batchSize);
			for (int i = 0; i < batchSize; i++) {
				counter++;
				if (!cursor.hasNext()) {
					break;
				}
				DBObject log = cursor.next();
				long timestamp = (long) log.get("timestamp");
				long userId = (long) log.get("actorId");
				String event = (String) log.get("eventType");
				long daysSinceEpoch = timestamp / 86400000;

				DBObject parameters = (DBObject) log.get("parameters");
				String ip = "";
				if (parameters.containsField("ip")) {
					ip = (String) parameters.get("ip");
				}

				EventType eventType;
				try {
					eventType = EventType.valueOf(event);
					if (!supported.contains(eventType))
						continue;
				} catch (Exception e) {
					continue;
				}
				Map<String, String> params = new HashMap<String, String>();
				params.put("objectType", (String) log.get("objectType"));
				params.put("link", (String) log.get("link"));
				AnalyticalServiceMessage iec = eventCount(userId, eventType, params, daysSinceEpoch);
				AnalyticalServiceMessage iuec = userEventCount(eventType, params, daysSinceEpoch);

				Gson gson = new GsonBuilder().create();
				reliableProducer.send(gson.toJson(wrap(ip, iec)));
				reliableProducer.send(gson.toJson(wrap(ip, iuec)));
			}
		}
		// Wait for background threads to finish.
		try {
			Thread.sleep(5 * 60 * 1000);
		} catch (InterruptedException e) {
			fail("Thread interrupted.");
		}
	}

	private MessageWrapper wrap(String ip, AnalyticalServiceMessage message) {
		MessageWrapper result = new MessageWrapper();
		result.setSender(ip);
		result.setMessage(message);
		result.setTimecreated(System.currentTimeMillis());
		return result;
	}

	private AnalyticalServiceMessage userEventCount(EventType event, Map<String, String> params, long days) {
		AnalyticalServiceDataFactoryImpl factory = new AnalyticalServiceDataFactoryImpl();
		JsonObject data = new JsonObject();
		data.add("event", new JsonPrimitive(eventName(event, params)));
		data.add("date", new JsonPrimitive(days));
		return factory.createAnalyticalServiceMessage(DataName.EVENTDAILYCOUNT, DataType.COUNTER, data);
	}

	private AnalyticalServiceMessage eventCount(long userId, EventType event, Map<String, String> params, long days) {
		AnalyticalServiceDataFactoryImpl factory = new AnalyticalServiceDataFactoryImpl();
		JsonObject data = new JsonObject();
		data.add("user", new JsonPrimitive(userId));
		data.add("event", new JsonPrimitive(eventName(event, params)));
		data.add("date", new JsonPrimitive(days));
		return factory.createAnalyticalServiceMessage(DataName.USEREVENTDAILYCOUNT, DataType.COUNTER, data);
	}

	private String eventName(EventType event, Map<String, String> params) {
		switch (event) {
		case SELECT_GOAL:
			return "goalsviews";
		case SELECT_COMPETENCE:
			return "competencesviews";
		case NAVIGATE:
			String link = params.get("link");
			if ("page".equals(params.get("objectType")) && link != null && link.startsWith("index")) {
				return "homepagevisited";
			}
			if ("page".equals(params.get("objectType")) && link != null && link.startsWith("learn")) {
				return "goalsviews";
			}
			if ("page".equals(params.get("objectType")) && link != null && link.startsWith("profile")) {
				return "profileviews";
			}
		default:
			return event.name().toLowerCase();
		}
	}

	@Test
	public void generateLogsFromMongoTest(){
		ReliableProducerImpl reliableProducer=new ReliableProducerImpl();
		 reliableProducer.setQueue(QueueNames.LOGS.name().toLowerCase());
		 reliableProducer.startAsynchronousPublisher();
		 
		 MongoDBServersConfig dbServersConfig=Settings.getInstance().config.mongoDatabase.dbServersConfig;
		  List<ServerAddress> serverAddresses=new ArrayList<ServerAddress>();
		  for(MongoDBServerConfig dbsConfig:dbServersConfig.dbServerConfig){
			ServerAddress serverAddress;
			try {
				serverAddress = new ServerAddress(dbsConfig.dbHost,dbsConfig.dbPort);
				serverAddresses.add(serverAddress);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		  }
		  MongoClient mongoClient=new MongoClient(serverAddresses);
		  DB db=mongoClient.getDB(Settings.getInstance().config.mongoDatabase.dbName);
		 DBCollection eventsCollection= db.getCollection("log_events_observed");
		 DBObject query=new BasicDBObject();
		 query.put("actorId",2);
		int count= eventsCollection.find().count();
		System.out.println("COLLECTION HAS EVENTS:"+count);
		int counter = 0;
		int batchSize = 100;
		while(counter < count) {
			DBCursor cursor = eventsCollection.find();
			cursor.skip(counter);
			cursor.limit(batchSize);
			for(int i = 0; i<batchSize; i++) {
				counter++;
				if (!cursor.hasNext()) {
					break;
				}
				DBObject dbObject=cursor.next();
				String eventType=dbObject.get("eventType").toString();
				boolean ignore=false;
				if(eventType.equals("TwitterPost")|| eventType.equals("LOGOUT") || eventType.equals("SESSIONENDED")	){
					ignore=true;					
				}
				if(!ignore){
					wrapMessageAndSend(reliableProducer, dbObject);
				}
			}
		}
	}
	
	@Test
	public void extractUserActivitiesFromMongoLogsTest(){
		ReliableProducerImpl reliableProducer=new ReliableProducerImpl();
		 reliableProducer.setQueue(QueueNames.LOGS.name().toLowerCase());
		 reliableProducer.startAsynchronousPublisher();
		 
		 MongoDBServersConfig dbServersConfig=Settings.getInstance().config.mongoDatabase.dbServersConfig;
		  List<ServerAddress> serverAddresses=new ArrayList<ServerAddress>();
		  for(MongoDBServerConfig dbsConfig:dbServersConfig.dbServerConfig){
			ServerAddress serverAddress;
			try {
				serverAddress = new ServerAddress(dbsConfig.dbHost,dbsConfig.dbPort);
				serverAddresses.add(serverAddress);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		  }
		  MongoClient mongoClient=new MongoClient(serverAddresses);
		  DB db=mongoClient.getDB(Settings.getInstance().config.mongoDatabase.dbName);
		 DBCollection eventsCollection= db.getCollection("log_events_observed");
		 DBObject query=new BasicDBObject();
		 query.put("actorId",2);
		int count= eventsCollection.find().count();
		logger.info("COLLECTION HAS EVENTS:"+count);
		int counter = 0;
		int batchSize = 1000;
		int batchesCounter=0;
		while(counter < count) {
			
			batchesCounter++;
			if((batchesCounter % 10)==0){
				logger.info(batchesCounter*1000+"/"+count);
			}
			DBCursor cursor = eventsCollection.find();
			cursor.skip(counter);
			cursor.limit(batchSize);
			for(int i = 0; i<batchSize; i++) {
				counter++;
				if (!cursor.hasNext()) {
					break;
				}
				DBObject dbObject=cursor.next();
				String eventType=dbObject.get("eventType").toString();
				
				boolean ignore=false;
				if(eventType.equals("TwitterPost")|| eventType.equals("LOGOUT") || eventType.equals("SESSIONENDED")	){
					ignore=true;					
				}
				if(dbObject.containsKey("objectType") && dbObject.get("objectType")!=null){
					String objectType=dbObject.get("objectType").toString();
					if(objectType.equals("MOUSE_CLICK")){
						ignore=true;
					}
				}
				
				if(!ignore){
					wrapMessageAndSend(reliableProducer, dbObject);
				}
				
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void generateActivityInteractionsLogsFromMongoTest(){
		ReliableProducerImpl reliableProducer=new ReliableProducerImpl();
		 reliableProducer.setQueue(QueueNames.LOGS.name().toLowerCase());
		 reliableProducer.startAsynchronousPublisher();
		 
		 MongoDBServersConfig dbServersConfig=Settings.getInstance().config.mongoDatabase.dbServersConfig;
		  List<ServerAddress> serverAddresses=new ArrayList<ServerAddress>();
		  for(MongoDBServerConfig dbsConfig:dbServersConfig.dbServerConfig){
			ServerAddress serverAddress;
			try {
				serverAddress = new ServerAddress(dbsConfig.dbHost,dbsConfig.dbPort);
				serverAddresses.add(serverAddress);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		  }
		  MongoClient mongoClient=new MongoClient(serverAddresses);
		  DB db=mongoClient.getDB(Settings.getInstance().config.mongoDatabase.dbName);
		 DBCollection eventsCollection= db.getCollection("log_events_observed");
		 DBObject query=new BasicDBObject();
		 query.put("actorId",2);
		int count= eventsCollection.find().count();
		System.out.println("COLLECTION HAS EVENTS:"+count);
		 
		DBCursor res2=eventsCollection.find();
		int counter = 0;
		int batchSize = 100;
		while(counter < count) {
			DBCursor cursor = eventsCollection.find();
			cursor.skip(counter);
			cursor.limit(batchSize);
			for(int i = 0; i<batchSize; i++) {
				counter++;
				if (!cursor.hasNext()) {
					break;
				}
				DBObject logObject = cursor.next();
				String objectType = ((String) logObject.get("objectType"));
				if (objectType != null && (objectType.equals("Activity") || objectType.equals("TargetActivity"))) {
					System.out.println("LOG OBJECT:" + logObject.toString());
				}
			}
		}	 
	}
	
	public void createActivityInteractionData(ReliableProducerImpl reliableProducer,long competenceId, long activityId) {
		GsonBuilder gson = new GsonBuilder();
		 gson.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());
		JsonObject data=new JsonObject();
		data.add("competenceid", new JsonPrimitive(competenceId));
		data.add("activityid", new JsonPrimitive(activityId));
		//AnalyticalServiceMessage message=factory.createAnalyticalServiceMessage(DataName.ACTIVITYINTERACTION, DataType.COUNTER,data);
		AnalyticalServiceMessage message=new AnalyticalServiceMessage();
		message.setDataName(DataName.ACTIVITYINTERACTION);
		message.setDataType(DataType.COUNTER);
		message.setData(data);
		//return message;
		MessageWrapper wrapper = new MessageWrapper();
		wrapper.setSender("0.0.0.0");
		wrapper.setMessage(message);
		wrapper.setTimecreated(System.currentTimeMillis());
		String msg = gson.create().toJson(wrapper);
		reliableProducer.send(msg);
		
	}
	
	private void wrapMessageAndSend(ReliableProducerImpl reliableProducer,DBObject logObject){
		LogMessage message = new LogMessage();
		Gson g=new Gson();
		DBObject parameters=(DBObject) logObject.get("parameters");
		String ip="";
		if(parameters.containsField("ip")){
			ip=(String) parameters.get("ip");
		}
			message.setTimestamp((long) logObject.get("timestamp"));
			message.setEventType((String) logObject.get("eventType"));
			message.setActorId((long) logObject.get("actorId"));
			message.setActorFullname((String) logObject.get("actorFullname"));
			message.setObjectType((String) logObject.get("objectType"));
			message.setObjectId((long) logObject.get("objectId"));
			message.setObjectTitle((String) logObject.get("objectTitle"));
			message.setTargetType((String) logObject.get("targetType"));
			message.setTargetId((long) logObject.get("targetId"));
			message.setReasonType((String) logObject.get("reasonType"));
			message.setReasonId((long) logObject.get("reasonId"));
			message.setLink((String) logObject.get("link"));
			message.setParameters(parameters);
			//wrapMessageAndSend(reliableProducer, message, ip);
		GsonBuilder gson = new GsonBuilder();
		 gson.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());
		MessageWrapper wrapper = new MessageWrapper();
		wrapper.setSender(ip);
		wrapper.setMessage(message);
		wrapper.setTimecreated(System.currentTimeMillis());
		String msg = gson.create().toJson(wrapper);
		reliableProducer.send(msg);
	}
	
	@Test
	public void testSend() {
		ReliableConsumer reliableConsumer=new ReliableConsumerImpl();
		reliableConsumer.setWorker(new DefaultMessageWorker());
	 	reliableConsumer.setQueue(QueueNames.SESSION.name().toLowerCase());
	 	reliableConsumer.StartAsynchronousConsumer();
	 	
	 	ReliableConsumer reliableConsumer2=new ReliableConsumerImpl();
	 	reliableConsumer2.setWorker(new DefaultMessageWorker());
	 	reliableConsumer2.setQueue(QueueNames.SYSTEM.name().toLowerCase());
	 	reliableConsumer2.StartAsynchronousConsumer();
	 	
	 	ReliableConsumer reliableConsumer3=new ReliableConsumerImpl();
	 	reliableConsumer3.setWorker(new DefaultMessageWorker());
	 	reliableConsumer3.setQueue(QueueNames.LOGS.name().toLowerCase());
	 	reliableConsumer3.StartAsynchronousConsumer();
	 	while(true){
	 	 	try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 	}
	
		//while(true){
	 	/*
	 	ReliableProducerImpl  reliableProducer=new ReliableProducerImpl();
		 reliableProducer.setQueue(QueueNames.SESSION.name().toLowerCase());
		 reliableProducer.startAsynchronousPublisher();
		 
		 ReliableProducerImpl reliableProducer2=new ReliableProducerImpl();
		 reliableProducer2.setQueue(QueueNames.SYSTEM.name().toLowerCase());
		 reliableProducer2.startAsynchronousPublisher();
		 
		 ReliableProducerImpl reliableProducer3=new ReliableProducerImpl();
		 reliableProducer3.setQueue(QueueNames.LOGS.name().toLowerCase());
		 reliableProducer3.startAsynchronousPublisher();
		// reliableProducer.init(QueueNames.SESSION);
		//reliableProducer.startAsynchronousPublisher();
		GsonBuilder gson = new GsonBuilder();
		 gson.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());
		 //gson.registerTypeAdapter(SessionMessage.class, new MessageWrapperAdapter());
		while(true){
		for(int i=0;i<10;i++){
			SessionMessage sm=new SessionMessage();
			sm.setReceiverId(i);
			MessageWrapper mw=new MessageWrapper();
			mw.setMessage(sm);
			String msg=gson.create().toJson(mw);
			 reliableProducer.send(msg);
			 System.out.println("Sent session message from reliable producer:"+msg);
			 
			 SystemMessage sm2=new SystemMessage();
				sm2.setServiceType(ServiceType.ADD_COMMENT);
				MessageWrapper mw2=new MessageWrapper();
				mw2.setMessage(sm2);
				String msg2=gson.create().toJson(mw2);
				 reliableProducer2.send(msg2);
				 System.out.println("Sent system message from reliable producer:"+msg2);
				 
				LogMessage sm3=new LogMessage();
					sm3.setActorFullname("Zoran");
					MessageWrapper mw3=new MessageWrapper();
					mw3.setMessage(sm3);
					String msg3=gson.create().toJson(mw3);
					 reliableProducer3.send(msg3);
					 System.out.println("Sent Log message from reliable producer:"+msg3);
			 
			 
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		}
		*/ 
		
	}
	
	private int random(int limit, double bias) {
		return (int) (1 + Math.round((Math.random() * bias) * limit));
	}

	private List<Long> generateEvents(long from, long to) {
		List<Long> result = new ArrayList<Long>();
		for(long i = from; i<to; i++) {
			double bias = 1;
			if (i % 30 == 0) {
				bias = Math.random();
			}
			for(int j = 0; j<random(20, bias); j++) {
				result.add(i);
			}
		}
		return result;
	}
	
	private long daysSinceEpoch(String date) throws ParseException {
		Date result = new SimpleDateFormat("yyyy-MM-dd Z").parse(date);
		return result.getTime() / 86400000;
	}
	
	private String createEvent(String event, Long daysSinceEpoch) {
		JsonObject data=new JsonObject();
		data.add("event", new JsonPrimitive(event));
		data.add("date", new JsonPrimitive(daysSinceEpoch));
		
		AnalyticalServiceMessage message = new AnalyticalServiceMessage();
		message.setDataName(DataName.EVENTDAILYCOUNT);
		message.setDataType(DataType.COUNTER);
		message.setData(data);
		
		MessageWrapper wrapper = new MessageWrapper();
		wrapper.setSender("127.0.0.1");
		wrapper.setMessage(message);
		wrapper.setTimecreated(System.currentTimeMillis());
		
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());
		return gson.create().toJson(wrapper);
	}
	
	@Ignore
	@Test
	public void generateRandomUserEventLogsTest() throws Exception {
		ReliableProducerImpl reliableProducer = new ReliableProducerImpl();
		reliableProducer.setQueue(QueueNames.ANALYTICS.name().toLowerCase());
		reliableProducer.startAsynchronousPublisher();

		long from = daysSinceEpoch("2015-01-01 UTC");
		long to = daysSinceEpoch("2015-12-31 UTC");

		String[] events = new String[] { "login", "registered", "homepagevisited", "goalsviews", "competencesviews",
				"profileviews" };
		for (String event : events) {
			for (Long day : generateEvents(from, to)) {
				reliableProducer.send(createEvent(event, day));
			}
		}
		Thread.sleep(5 * 60 * 1000);
	}

}
