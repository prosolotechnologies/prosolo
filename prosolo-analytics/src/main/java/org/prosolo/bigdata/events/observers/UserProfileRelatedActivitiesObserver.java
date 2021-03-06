package org.prosolo.bigdata.events.observers;

import org.prosolo.bigdata.dal.cassandra.impl.UserObservationsDBManagerImpl;
import org.prosolo.bigdata.events.analyzers.ObservationType;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.scala.clustering.ProfileEventsChecker$;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.util.date.DateEpochUtil;

import java.util.List;
import java.util.Set;


/**
* @author Zoran Jeremic, Nov 13, 2015
*
*/
public class UserProfileRelatedActivitiesObserver implements EventObserver{
	
	ProfileEventsChecker$ eventsChecker=ProfileEventsChecker$.MODULE$;
	//UserObservationsDBManager dbManager=new UserObservationsDBManagerImpl();
	EventType[] supportedTypes=null;
 

	@Override
	public Topic[] getSupportedTopics() {
		return new Topic[] { Topic.LOGS };
	}

	@Override
	public EventType[] getSupportedTypes() {
		
		 if(supportedTypes==null){
			List<EventType> supportedTypesList=eventsChecker.getSupportedEventTypes();
			supportedTypes=supportedTypesList.toArray(new EventType[supportedTypesList.size()]);
		}
		return supportedTypes; 
	}

	@Override
	public void handleEvent(DefaultEvent event) {
		//Gson gson=new Gson();
		//String eventC=gson.toJson(event);
		LogEvent logEvent=(LogEvent) event;
		long userid=logEvent.getActorId();
		long courseid=logEvent.getCourseId();
		if(logEvent.getEventType().equals(EventType.ENROLL_COURSE)){
			//courseid=logEvent.getCourseId();
			UserObservationsDBManagerImpl.getInstance().enrollUserToCourse(userid,courseid);
		}else if(logEvent.getEventType().equals(EventType.COURSE_WITHDRAWN)){
			//courseid=logEvent.getCourseId();
			UserObservationsDBManagerImpl.getInstance().withdrawUserFromCourse(userid, courseid);
		}
		if(eventsChecker.isEventObserved(logEvent)){
			 ObservationType observationType=eventsChecker.getObservationType(logEvent);
			 long date = DateEpochUtil.getDaysSinceEpoch(logEvent.getTimestamp());
			if(courseid>0){
				UserObservationsDBManagerImpl.getInstance().updateUserProfileActionsObservationCounter(date, userid, courseid, observationType);
			}else{
				Set<Long> courses=UserObservationsDBManagerImpl.getInstance().findAllUserCourses(userid);
				for(Long course:courses){
				 	UserObservationsDBManagerImpl.getInstance().updateUserProfileActionsObservationCounter(date, userid, course, observationType);

				}
			}
		}

	}

}
