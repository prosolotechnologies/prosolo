package org.prosolo.bigdata.events.observers;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.dal.cassandra.AnalyticalEventDBManager;
import org.prosolo.bigdata.dal.cassandra.LogEventDBManager;
import org.prosolo.bigdata.dal.cassandra.UserObservationsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.LogEventDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.UserObservationsDBManagerImpl;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.activities.events.EventType;

import java.util.*;

/**
 * @author Zoran Jeremic Apr 6, 2015
 *
 */

public class LogEventsPersisterObserver implements EventObserver {
	private LogEventDBManager dbManager = new LogEventDBManagerImpl();
	private UserObservationsDBManager userObservationsDBManager=new UserObservationsDBManagerImpl();
	private AnalyticalEventDBManager analyticalDBManager=new AnalyticalEventDBManagerImpl();

	@Override
	public Topic[] getSupportedTopics() {
		// TODO Auto-generated method stub
		return new Topic[] { Topic.LOGS };
	}

	@Override
	public EventType[] getSupportedTypes() {
		return null;
	}

	@Override
	public void handleEvent(DefaultEvent event) {
		if (event instanceof LogEvent) {
			LogEvent logEvent = (LogEvent) event;
			dbManager.insertLogEvent(logEvent);
			if(logEvent.getTargetUserId()>0){
				Set<Long> courses=new HashSet<Long>();

				if(logEvent.getCourseId()==0){
					Set<Long> actorCourses=userObservationsDBManager.findAllUserCourses(logEvent.getActorId());
					courses.addAll(actorCourses);
					Set<Long> targetUserCourses=userObservationsDBManager.findAllUserCourses(logEvent.getTargetUserId());
					courses.addAll(targetUserCourses);
				}else{
					courses.add(logEvent.getCourseId());
				}
				if(logEvent.getActorId()!=logEvent.getTargetUserId()){
					for(Long courseId:courses){
						Map<String,Object> data=new HashMap<String,Object>();
						data.put("course", courseId);
						data.put("source", logEvent.getActorId());
						data.put("target", logEvent.getTargetUserId());
						analyticalDBManager.updateGenericCounter(DataName.SOCIALINTERACTIONCOUNT,data);
					/*System.out.println("OBSERVED LOG EVENT:"+event.getEventType()
							+" actor:"+logEvent.getActorId()
							+" with Target UserID:"+logEvent.getTargetUserId()
							+" course:"+logEvent.getCourseId()
					+	 " inserted course:"+courseId);*/
					}
				}
			}
		}

	}

}
