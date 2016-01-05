package org.prosolo.services.nodes.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.ActivityTimeSpentPercentileService;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.rest.data.ActivityJsonData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path("/")
public class ActivityRestService {

	private static Logger logger = Logger.getLogger(ActivityRestService.class);
	
	@GET
	@Path("competences/{competence}/activities")
	@Produces("application/json")
	public String getCompletedActivities(@PathParam("competence") long competenceId) {
		try {
			List<TargetActivity> activities = ServiceLocator.getInstance()
					.getService(CompetenceManager.class)
						.getTargetActivities(competenceId);
			
			List<ActivityJsonData> jsonActivities = new ArrayList<>();
			for(TargetActivity ta : activities) {
				long timeSpent = ta.getTimeSpent();
				if(timeSpent != 0 || ta.isCompleted()) {
					long activityId = ta.getActivity().getId();
					List<Long> usersTimes = ServiceLocator.getInstance()
							.getService(ActivityManager.class)
								.getTimeSpentOnActivityForAllUsersSorted(activityId);
					
					int timeSpentGroup = ServiceLocator.getInstance()
							.getService(ActivityTimeSpentPercentileService.class)
							.getPercentileGroup(usersTimes, timeSpent);
					
					ActivityJsonData jsonActivity = new ActivityJsonData();
					jsonActivity.setId(ta.getId());
					jsonActivity.setName(ta.getTitle());
					jsonActivity.setCompleted(ta.isCompleted());
					jsonActivity.setTimeNeeded(timeSpentGroup);
					
					//to be changed when complexity algorithm is implemented
					int min = 1;
					int max = 5;
					
					Random r = new Random();
					double randomVal = min + (max - min) * r.nextDouble();
					jsonActivity.setComplexity(randomVal);
					jsonActivities.add(jsonActivity);
				}
			}
			
			final GsonBuilder builder = new GsonBuilder();
		    final Gson gson = builder.create();
		   // Type listType = new TypeToken<ArrayList<Competence>>() {}.getType();
		   // gson.toJson(activities, typeOfSrc)
		    String s = gson.toJson(jsonActivities);
		   // System.out.println(s);
		    return s;
		} catch (DbConnectionException dbce) {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		} catch (NumberFormatException nfe) {
			logger.error("CourseDataServlet has been passed bad value for 'id' query parameter, it is not long");
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
	}
	
}
