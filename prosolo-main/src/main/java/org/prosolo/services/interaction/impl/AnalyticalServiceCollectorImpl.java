package org.prosolo.services.interaction.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hibernate.Session;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;
import org.prosolo.bigdata.common.rabbitmq.AnalyticalServiceMessage;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.prosolo.services.interaction.AnalyticalServiceDataFactory;
import org.prosolo.services.messaging.AnalyticalServiceMessageDistributer;
import org.prosolo.services.nodes.ActivityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
@author Zoran Jeremic Apr 12, 2015
 *
 */
@Service("org.prosolo.services.interaction.AnalyticalServiceCollector")
public class AnalyticalServiceCollectorImpl implements AnalyticalServiceCollector{
	
	@Autowired AnalyticalServiceDataFactory factory;
	@Autowired AnalyticalServiceMessageDistributer messageDistributer;
	@Autowired ActivityManager activityManager;

	@Override
	public void increaseUserActivityLog(long userid, long daysSinceEpoch) {
		JsonObject data=new JsonObject();
		data.add("userid", new JsonPrimitive(userid));
		data.add("date", new JsonPrimitive(daysSinceEpoch));
		AnalyticalServiceMessage message=factory.createAnalyticalServiceMessage(DataName.USERACTIVITY, DataType.COUNTER, data);
		messageDistributer.distributeMessage(message);
	}
	
	@Override
	public void increaseUserActivityForLearningGoalLog(long userid, long learningGoal, long daysSinceEpoch) {
		JsonObject data=new JsonObject();
		data.add("userid", new JsonPrimitive(userid));
		data.add("learninggoalid", new JsonPrimitive(learningGoal));
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
	public void createTargetCompetenceActivitiesData(long competenceId, long targetCompetenceId, List<TargetActivity> tActivities) {
		JsonObject data=new JsonObject();
		List<Long> ids=new ArrayList<Long>();
		data.add("competenceid", new JsonPrimitive(competenceId));
		data.add("targetcompetenceid", new JsonPrimitive(targetCompetenceId));
		JsonArray actArray=new JsonArray();
		for(TargetActivity tAct:tActivities){
			if(!ids.contains(tAct.getActivity().getId())){
				actArray.add(new JsonPrimitive(tAct.getActivity().getId()));
				ids.add(tAct.getActivity().getId());
			}
			
		}
		data.add("activities",actArray );
		AnalyticalServiceMessage message=factory.createAnalyticalServiceMessage(DataName.TARGETCOMPETENCEACTIVITIES, DataType.RECORD,data);
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

