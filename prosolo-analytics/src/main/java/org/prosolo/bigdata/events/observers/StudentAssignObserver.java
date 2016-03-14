package org.prosolo.bigdata.events.observers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.cassandra.impl.StudentAssignEventDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.data.StudentAssign;
import org.prosolo.bigdata.dal.cassandra.impl.data.StudentAssignEventData;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.jobs.InstructorEmailSenderJob;
//import org.prosolo.bigdata.scala.twitter.util.TwitterUtils$;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.activities.events.EventType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class StudentAssignObserver implements EventObserver {

	private final static Logger logger = Logger
			.getLogger(StudentAssignObserver.class.getName());

	@Override
	public Topic[] getSupportedTopics() {
		return new Topic[] { Topic.LOGS };
	}

	@Override
	public EventType[] getSupportedTypes() {
		return new EventType[] {
			EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR,
			EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR,
			EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR
		};
	}

	@Override
	public void handleEvent(DefaultEvent event) {
		try {
			if (event instanceof LogEvent) {
				LogEvent ev = (LogEvent) event;
				List<StudentAssignEventData> events = getProcessedEvents(ev);
				StudentAssignEventDBManagerImpl manager = StudentAssignEventDBManagerImpl.getInstance();
				events.forEach(e -> manager.saveStudentAssignEvent(e));
			}
			//InstructorEmailSenderJob job = new InstructorEmailSenderJob();
			//job.execute();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	private List<StudentAssignEventData> getProcessedEvents(LogEvent event) {
		List<StudentAssignEventData> events = new ArrayList<>();
		
		long instructorId = event.getTargetId();
		long studentId = event.getObjectId();
		
		JsonObject parametersJson = event.getParameters();
		JsonElement courseEl = parametersJson.get("courseId");
		long courseId = courseEl.getAsLong();
		
		switch(event.getEventType()) {
			case STUDENT_ASSIGNED_TO_INSTRUCTOR:
				StudentAssignEventData ev1 = new StudentAssignEventData(courseId, instructorId, 
						studentId, StudentAssign.ASSIGNED);
				events.add(ev1);
				break;
			case STUDENT_UNASSIGNED_FROM_INSTRUCTOR:
				StudentAssignEventData ev2 = new StudentAssignEventData(courseId, instructorId, 
						studentId, StudentAssign.UNASSIGNED);
				events.add(ev2);
				break;
			case STUDENT_REASSIGNED_TO_INSTRUCTOR:
				StudentAssignEventData ev3 = new StudentAssignEventData(courseId, instructorId, 
						studentId, StudentAssign.ASSIGNED);
				events.add(ev3);
				
				JsonElement instructorFromEl = parametersJson.get("reassignedFromInstructorUserId");
				long instructorFromId = instructorFromEl.getAsLong();
				StudentAssignEventData ev4 = new StudentAssignEventData(courseId, instructorFromId, 
						studentId, StudentAssign.UNASSIGNED);
				events.add(ev4);
				break;
			default: 
				break;
		}
		
		return events;
	}


}
