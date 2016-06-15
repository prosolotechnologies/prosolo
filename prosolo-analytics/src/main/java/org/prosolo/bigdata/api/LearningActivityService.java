package org.prosolo.bigdata.api;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.prosolo.bigdata.dal.cassandra.impl.LearningEventsDBManagerImpl;
import org.prosolo.bigdata.session.impl.LearningEventSummary;
import org.prosolo.bigdata.session.impl.LearningEventSummary.Milestone;
import org.prosolo.bigdata.session.impl.MilestoneType;
import org.prosolo.bigdata.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/learning/activity")
public class LearningActivityService {
	
	private final Logger logger = LoggerFactory.getLogger(LearningActivityService.class);
	
	private int[] eventValues = new int[]{0,0,0,0,0,1,1,15,12,18,10,2,1,3,2,0,0,0,3,4,6,2,3,1,9,14,13,15,18,9};
	

	@GET
	@Path("/student/{id}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getStudentLearningActivities(@PathParam("id") Long id, @QueryParam("dateFrom") String dateFrom,
			@QueryParam("dateTo") String dateTo) throws ParseException {
		if(dateFrom==null || dateTo==null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		long daysFrom = DateUtil.parseDaysSinceEpoch(dateFrom, "dd.MM.yyyy. Z");
		long daysTo = DateUtil.parseDaysSinceEpoch(dateTo, "dd.MM.yyyy. Z");
		logger.debug("Parsed days since epoch time: from: {}, to: {}.", daysFrom, daysTo);
		List<LearningEventSummary> learningEventsData = LearningEventsDBManagerImpl.getInstance().getLearningEventsData(id, daysFrom, daysTo);
		for(int i = 0; i < learningEventsData.size(); i++) {
			learningEventsData.get(i).setValue(eventValues[i % eventValues.length]);
		}
		setMilestone(5, learningEventsData, MilestoneType.Credentials, "course.enroll", "SRL.Planning.GoalSetting", 
				"Enrolling a credential","Enroll course");
		setMilestone(10, learningEventsData, MilestoneType.Competences, "learn.competence.complete", "SRL.Evaluation.Reflection.Evaluation", 
				"Marking a competence as completed","Competence completed");
		setMilestone(17, learningEventsData, MilestoneType.Activities, "learn.activity.complete", "SRL.Engagement.WorkingOnTheTask", 
				"Marking an activity as completed","Activity complete");
		setMilestone(22, learningEventsData, MilestoneType.Competences, "learn.goal.complete", "SRL.Planning.GoalSetting", 
				"Marking a learning goal as completed","Goal completed");
		setMilestone(27, learningEventsData, MilestoneType.Activities, "learn.activity.complete", "SRL.Engagement.WorkingOnTheTask", 
				"Marking an activity as completed","Activity complete");
		
		
		return ResponseUtils.corsOk(learningEventsData);
	}
	
	private void setMilestone(int index, List<LearningEventSummary> summaries, 
			MilestoneType type, String id, String process, String description,String name) {
		Milestone milestone = new Milestone();
		milestone.setType(type);
		milestone.setId(id);
		milestone.setDescription(description);
		milestone.setProcess(process);
		milestone.setName(name);
		if(summaries.get(index).getMilestones() == null) {
			summaries.get(index).setMilestones(new ArrayList<>());
		}
		summaries.get(index).getMilestones().add(milestone);
		
	}

}
