package org.prosolo.bigdata.api;

import java.text.ParseException;
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
import org.prosolo.common.util.date.DateEpochUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/learning/activity")
public class LearningActivityService {
	
	private final Logger logger = LoggerFactory.getLogger(LearningActivityService.class);
	
	@GET
	@Path("/student/{id}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getStudentLearningActivities(@PathParam("id") Long id, @QueryParam("dateFrom") String dateFrom,
			@QueryParam("dateTo") String dateTo) throws ParseException {

		if(dateFrom==null || dateTo==null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		long daysFrom = DateEpochUtil.parseDaysSinceEpoch(dateFrom, "dd.MM.yyyy. Z");
		long daysTo = DateEpochUtil.parseDaysSinceEpoch(dateTo, "dd.MM.yyyy. Z");
		logger.debug("Parsed days since epoch time: from: {}, to: {}.", daysFrom, daysTo);
		List<LearningEventSummary> learningEventsData = LearningEventsDBManagerImpl.getInstance().getLearningEventsData(id, daysFrom, daysTo+1);
		return ResponseUtils.corsOk(learningEventsData);
	}

}
