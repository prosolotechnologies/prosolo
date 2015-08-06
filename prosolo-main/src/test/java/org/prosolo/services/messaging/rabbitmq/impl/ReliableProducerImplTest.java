package org.prosolo.services.messaging.rabbitmq.impl;

 

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.prosolo.app.Settings;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;
import org.prosolo.config.MongoDBServerConfig;
import org.prosolo.config.MongoDBServersConfig;
import org.prosolo.common.messaging.MessageWrapperAdapter;
import org.prosolo.common.messaging.data.AnalyticalServiceMessage;
import org.prosolo.common.messaging.data.LogMessage;
import org.prosolo.common.messaging.data.MessageWrapper;
import org.prosolo.common.messaging.rabbitmq.QueueNames;
import org.prosolo.common.messaging.rabbitmq.ReliableConsumer;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableConsumerImpl;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableProducerImpl;

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
		DBCursor res2=eventsCollection.find();
		int counter=0;
		while(res2.hasNext()){
			if(counter>10000)break;
			DBObject logObject=res2.next();
			wrapMessageAndSend(reliableProducer, logObject);
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
		int counter=0;
		while(res2.hasNext()){
			if(counter>10000)break;
			DBObject logObject=res2.next();
			
			String objectType=((String) logObject.get("objectType"));
			if((objectType!=null) && (objectType.equals("Activity") || objectType.equals("TargetActivity"))){
				System.out.println("LOG OBJECT:"+logObject.toString());
			}
			
			//createActivityInteractionData(reliableProducer, logObject);
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

}
