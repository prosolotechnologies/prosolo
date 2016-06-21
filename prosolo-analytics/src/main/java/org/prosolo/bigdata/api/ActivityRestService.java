package org.prosolo.bigdata.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.api.data.ActivityJsonData;
import org.prosolo.bigdata.dal.persistence.impl.ActivityDAOImpl;
import org.prosolo.common.domainmodel.credential.TargetActivity1;

@Path("/")
public class ActivityRestService {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ActivityRestService.class);
	
	@GET
	@Path("competences/{competence}/activities")
	@Produces("application/json")
	public Response getCompletedActivities(@PathParam("competence") long competenceId) {
		try {
			List<TargetActivity1> activities = ActivityDAOImpl.getInstance()
					.getTargetActivities(competenceId);
			
			List<ActivityJsonData> jsonActivities = new ArrayList<>();
			for(TargetActivity1 ta : activities) {
				long timeSpent = ta.getTimeSpent();
				if(timeSpent != 0 || ta.isCompleted()) {
					//TODO uncomment when fixed
					long activityId = ta.getActivity().getId();
					List<Long> usersTimes = ActivityDAOImpl.getInstance()
								.getTimeSpentOnActivityForAllUsersSorted(activityId);
					
					int timeSpentGroup = ActivityDAOImpl.getInstance()
							.getPercentileGroup(usersTimes, timeSpent);
					
					ActivityJsonData jsonActivity = new ActivityJsonData();
					jsonActivity.setId(ta.getId());
					jsonActivity.setName(ta.getTitle());
					jsonActivity.setCompleted(ta.isCompleted());
					jsonActivity.setTimeNeeded(timeSpentGroup);
					
					//to be changed when complexity algorithm is implemented
					int min = 1;
					int max = 5;
					
					jsonActivity.setComplexity(ThreadLocalRandom.current().nextInt(min, max + 1));
					jsonActivities.add(jsonActivity);
				} 
//				//TODO comment all when fixed
//				ActivityJsonData jsonActivity = new ActivityJsonData();
//				jsonActivity.setId(ta.getId());
//				jsonActivity.setName(ta.getTitle());
//				jsonActivity.setCompleted(ta.isCompleted());
//				
//				//TODO return to time spent calculations when fixed
//				//Random random = new Random();
//				//int randomNumber = random.nextInt(maxTime - minTime + 1) + minTime;
//				//to be changed when complexity algorithm is implemented
//				int min = 1;
//				int max = 5;
//				jsonActivity.setTimeNeeded(ThreadLocalRandom.current().nextInt(min, max + 1));
//				jsonActivity.setComplexity(ThreadLocalRandom.current().nextInt(min, max + 1));
//				jsonActivities.add(jsonActivity);
			}
			
//			final GsonBuilder builder = new GsonBuilder();
//		    final Gson gson = builder.create();
		   // Type listType = new TypeToken<ArrayList<Competence>>() {}.getType();
		   // gson.toJson(activities, typeOfSrc)
//		    String s = gson.toJson(jsonActivities);
		   // System.out.println(s);
			return ResponseUtils.corsOk(jsonActivities);
		} catch (Exception dbce) {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
}
