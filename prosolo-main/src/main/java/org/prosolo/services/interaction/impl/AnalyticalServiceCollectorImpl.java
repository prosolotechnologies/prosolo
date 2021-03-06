package org.prosolo.services.interaction.impl;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.messaging.data.AnalyticalServiceMessage;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.prosolo.services.interaction.AnalyticalServiceDataFactory;
import org.prosolo.services.messaging.AnalyticalServiceMessageDistributer;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Zoran Jeremic
 * @deprecated since 0.7
 */
//@Deprecated
@Service("org.prosolo.services.interaction.AnalyticalServiceCollector")
public class AnalyticalServiceCollectorImpl implements AnalyticalServiceCollector{
	
	@Autowired AnalyticalServiceDataFactory factory;
	@Autowired AnalyticalServiceMessageDistributer messageDistributer;

	@Override
	public void increaseUserActivityLog(long userid, long daysSinceEpoch) {
		JsonObject data=new JsonObject();
		data.add("userid", new JsonPrimitive(userid));
		data.add("date", new JsonPrimitive(daysSinceEpoch));
		AnalyticalServiceMessage message=factory.createAnalyticalServiceMessage(DataName.USERACTIVITY, DataType.COUNTER, data);
		messageDistributer.distributeMessage(message);
	}
	
	@Override
	public void increaseUserActivityForCredentialLog(long userid, long credentialId, long daysSinceEpoch) {
		JsonObject data=new JsonObject();
		data.add("userid", new JsonPrimitive(userid));
		data.add("learninggoalid", new JsonPrimitive(credentialId));
		data.add("date", new JsonPrimitive(daysSinceEpoch));
		AnalyticalServiceMessage message=factory.createAnalyticalServiceMessage(DataName.USERLEARNINGGOALACTIVITY, DataType.COUNTER, data);
		messageDistributer.distributeMessage(message);
	}

	@Override
	public void createActivityInteractionData(long competenceId, long activityId) {
		JsonObject data=new JsonObject();
		data.add("competenceid", new JsonPrimitive(competenceId));
		data.add("activityid", new JsonPrimitive(activityId));
		AnalyticalServiceMessage message=factory.createAnalyticalServiceMessage(DataName.ACTIVITYINTERACTION, DataType.COUNTER,data);
		messageDistributer.distributeMessage(message);		
	}
	
	@Override
	public void sendUpdateHashtagsMessage(Map<String,String> parameters, long goalId, long userId){
		JsonObject data=new JsonObject();
		data.add("userid",new JsonPrimitive(userId));
		data.add("goalid", new JsonPrimitive(goalId));
		if(parameters.containsKey("newhashtags")){
			data.add("newhashtags", new JsonPrimitive(parameters.get("newhashtags")));
		}
		if(parameters.containsKey("oldhashtags")){
			data.add("oldhashtags", new JsonPrimitive(parameters.get("oldhashtags")));
		}
		AnalyticalServiceMessage message=factory.createAnalyticalServiceMessage(DataName.UPDATEHASHTAGS, DataType.PROCESS,data);
		messageDistributer.distributeMessage(message);
	}

    @Override
	public void updateTwitterUser(long userId, long twitterUserId, boolean addUser){
		JsonObject data=new JsonObject();
		data.add("userId",new JsonPrimitive(userId));
		data.add("twitterId",new JsonPrimitive(twitterUserId));
		data.add("add",new JsonPrimitive(addUser));
		AnalyticalServiceMessage message=factory.createAnalyticalServiceMessage(DataName.UPDATETWITTERUSER, DataType.PROCESS,data);
        messageDistributer.distributeMessage(message);
    }
        
    public String eventName(EventType event, Map<String, String> params) {
    	switch (event) {
    	case SELECT_GOAL :
    		return "goalsviews";
    	case SELECT_COMPETENCE :
    		return "competencesviews";
    	case NAVIGATE :
    		if ("page".equals(params.get("objectType")) && "index".equals(params.get("link"))) {
    			return "homepagevisited";
    		}
    		if ("page".equals(params.get("objectType")) && "learn".equals(params.get("link"))) {
    			return "goalsviews";
    		}
    		if ("page".equals(params.get("objectType")) && params.get("link") != null && params.get("link").startsWith("profile")) {
    			return "profileviews";
    		}
    	default :
    		return event.name().toLowerCase();
    	}
    }

	@Override
	public void increaseUserEventCount(EventType event, Map<String, String> params, long daysSinceEpoch) {
		JsonObject data=new JsonObject();
		data.add("event", new JsonPrimitive(eventName(event, params)));
		data.add("date", new JsonPrimitive(daysSinceEpoch));
		messageDistributer.distributeMessage(factory.createAnalyticalServiceMessage(DataName.EVENTDAILYCOUNT, DataType.COUNTER, data));
	}
	
	@Override
	public void increaseEventCount(long userId, EventType event, Map<String, String> params, long daysSinceEpoch) {
		JsonObject data=new JsonObject();
		data.add("user", new JsonPrimitive(userId));
		data.add("event", new JsonPrimitive(eventName(event, params)));
		data.add("date", new JsonPrimitive(daysSinceEpoch));
		messageDistributer.distributeMessage(factory.createAnalyticalServiceMessage(DataName.USEREVENTDAILYCOUNT, DataType.COUNTER, data));
	}

	@Override
	public void updateInstanceLoggedUserCount(String instance, long timestamp, long count) {
		JsonObject data=new JsonObject();
		data.add("instance", new JsonPrimitive(instance));
		data.add("timestamp", new JsonPrimitive(timestamp));
		data.add("count", new JsonPrimitive(count));
		messageDistributer.distributeMessage(factory.createAnalyticalServiceMessage(DataName.INSTANCELOGGEDUSERSCOUNT, DataType.RECORD, data));
	}
	
	@Override
	public void enableHashtag(String hashtag) {
		JsonObject data=new JsonObject();
		data.add("hashtag", new JsonPrimitive(hashtag));
		data.add("action", new JsonPrimitive("enable"));
		messageDistributer.distributeMessage(factory.createAnalyticalServiceMessage(DataName.DISABLEDHASHTAGS, DataType.RECORD, data));
	}
	
	@Override
	public void disableHashtag(String hashtag) {
		JsonObject data=new JsonObject();
		data.add("hashtag", new JsonPrimitive(hashtag));
		data.add("action", new JsonPrimitive("disable"));
		messageDistributer.distributeMessage(factory.createAnalyticalServiceMessage(DataName.DISABLEDHASHTAGS, DataType.RECORD, data));
	}

	@Override
	public void increaseSocialInteractionCount(long courseid, long source, long target) {
		JsonObject data=new JsonObject();
		data.add("course", new JsonPrimitive(courseid));
		data.add("source", new JsonPrimitive(source));
		data.add("target", new JsonPrimitive(target));
		AnalyticalServiceMessage message=factory.createAnalyticalServiceMessage(DataName.SOCIALINTERACTIONCOUNT, DataType.COUNTER, data);
		messageDistributer.distributeMessage(message);
	}

	@Override
	public void storeNotificationData(String email, NotificationData notificationData){
		Gson gson = new Gson();
		JsonObject data=(JsonObject) new JsonParser().parse(gson.toJson(notificationData));
		data.add("email",new JsonPrimitive(email));
		AnalyticalServiceMessage message=factory.createAnalyticalServiceMessage(DataName.STORENOTIFICATIONDATA, DataType.RECORD, data);
		messageDistributer.distributeMessage(message);
	}
	
	
/*	//temporary
	@Override
	public void testCreateActivityInteractionData() {
		System.out.println("TEST create activity interaction data");
		Session session = (Session) ServiceLocator.getInstance().getService(ActivityManager.class).getPersistence().openSession();
		List<TargetActivity> tActivities=ServiceLocator.getInstance().getService(ActivityManager.class).getAllTargetActivities();
		for(TargetActivity targetActivity:tActivities){
			Random r=new Random();
			int count=r.nextInt(100);
		Activity activity=targetActivity.getActivity();
			 TargetCompetence targetCompetence=targetActivity.getParentCompetence();
			TargetCompetence tc;
				 tc = (TargetCompetence) session.load(TargetCompetence.class, targetCompetence.getId());
				 Competence competence=tc.getCompetence();
				 for(int i=0;i<count;i++){
					 createActivityInteractionData(competence.getId(),activity.getId());
				 }
		
		}
		session.close();		
	}
	@Override
	public void testCreateTargetCompetenceActivitiesAnalyticalData() {
		System.out.println("TEST create target comeptence activities data");
		Session session = (Session) ServiceLocator.getInstance().getService(ActivityManager.class).getPersistence().openSession();
		List<Competence> competences=ServiceLocator.getInstance().getService(ActivityManager.class).getAllResources(Competence.class);
		List<TargetActivity> activities=ServiceLocator.getInstance().getService(ActivityManager.class).getAllResources(TargetActivity.class);
		
		for(Competence comp:competences){
			Random r=new Random();
			int count=r.nextInt(1000);
			for(int i=0;i<count;i++){
				List<TargetActivity> tActs=new ArrayList<TargetActivity>();
				Long tCompId=(long) r.nextInt(10000);
				int actCount=r.nextInt(20);
				for(int k=0;k<actCount;k++){
					int actInd=r.nextInt(activities.size()-1);
					TargetActivity act=activities.get(actInd);
					
					tActs.add(act);
					
				}				
				createTargetCompetenceActivitiesData(comp.getId(),tCompId, tActs);
			}
		}
		
		session.close();		
	}*/

}

