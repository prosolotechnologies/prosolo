package org.prosolo.bigdata.api;


import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;
import org.prosolo.bigdata.common.rest.pojo.RequestListParametersObject;
import org.prosolo.bigdata.common.rest.pojo.RequestParametersObject;
import org.prosolo.bigdata.services.ActivityRecommendation;
import org.prosolo.bigdata.services.impl.ActivityRecommendationImpl;

import com.google.gson.Gson;


/**
@author Zoran Jeremic Apr 20, 2015
 *
 */
@Path("/recommendation")
public class RecommendationServices {
	private final static Logger logger = Logger
			.getLogger(RecommendationServices.class);
	private ActivityRecommendation activityRecommendation=new ActivityRecommendationImpl();
	
	@POST
	@Path("/recommendedactivities")
	@Produces("application/json")
	public Response getRecommendedOrFrequentActivitiesForCompetence(RequestParametersObject parameters) {
		Gson g=new Gson();
		logger.debug("parameters:" + g.toJson(parameters));
		List<ActivityAccessCount> recommendedActivities=activityRecommendation.getRecommendedActivitiesForCompetence(parameters.getObjectId(), parameters.getIgnoredIds(), parameters.getLimit());
		
		return Response.status(Status.OK).entity(g.toJson(recommendedActivities)).build();
	}
	
	@POST
	@Path("/relatedactivities")
	@Produces("application/json")
	public Response getRelatedActivitiesForActivity(RequestListParametersObject parameters) {
		Gson g=new Gson();
		logger.debug("parameters:" + g.toJson(parameters));
		List<ActivityAccessCount> recommendedActivities=activityRecommendation.getRelatedActivitiesForActivity(parameters.getParameter("competenceid"), parameters.getParameter("activityid"), parameters.getIgnoredIds(),parameters.getLimit());
		
		return Response.status(Status.OK).entity(g.toJson(recommendedActivities)).build();
	}
}

