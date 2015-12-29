package org.prosolo.services.nodes.rest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.rest.data.ActivityJsonData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Path("/")
public class ActivityRestService {

	private static Logger logger = Logger.getLogger(ActivityRestService.class);
	
	@GET
	@Path("users/{user}/competences/{competence}/activities/completed")
	@Produces("application/json")
	public String getCompletedActivities(@PathParam("user") long userId, @PathParam("competence") long competenceId) {
		try {
			List<TargetActivity> activities = ServiceLocator.getInstance().getService(ActivityManager.class).getComptenceCompletedTargetActivities(userId, competenceId);
			
			List<ActivityJsonData> jsonActivities = new ArrayList<>();
			for(TargetActivity ta : activities) {
				jsonActivities.add(new ActivityJsonData(ta));
			}
			
			final GsonBuilder builder = new GsonBuilder();
		    final Gson gson = builder.create();
		   // Type listType = new TypeToken<ArrayList<Competence>>() {}.getType();
		   // gson.toJson(activities, typeOfSrc)
		    String s = gson.toJson(jsonActivities);
		    //System.out.println(s);
		    return s;
		} catch (DbConnectionException dbce) {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		} catch (NumberFormatException nfe) {
			logger.error("CourseDataServlet has been passed bad value for 'id' query parameter, it is not long");
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
	}
	
}
