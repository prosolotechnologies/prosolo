package org.prosolo.bigdata.api;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import com.google.gson.Gson;
//import org.prosolo.bigdata.common.dal.pojo.SocialInteractionCount;
import org.prosolo.bigdata.dal.cassandra.SocialInteractionStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl;
import org.prosolo.bigdata.dal.persistence.UserDAO;
import org.prosolo.bigdata.dal.persistence.impl.UserDAOImpl;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.services.UserServiceConfig;
import org.prosolo.common.util.ImageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/social/interactions")
public class SocialInteractionStatisticsService {

	private final Logger logger = LoggerFactory.getLogger(SocialInteractionStatisticsService.class);
	
	private SocialInteractionStatisticsDBManager  dbManager = SocialInteractionStatisticsDBManagerImpl.getInstance();
	
	private UserDAO userDao = UserDAOImpl.getInstance();
	@GET
	@Path("/ping")
	public String ping() {
		return "Pong!";
	}

	@GET
	@Path("/all")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllUserInteractions() throws ParseException {
		logger.debug("Service 'getAllUserInteractions' called");
		long courseid=1;
		//TODO Aleksandar This service doesn't have too much sense. It should be for course
		//List<SocialInteractionCount> socialInteractionCounts = dbManager.getSocialInteractionCounts(courseid);
		//return ResponseUtils.corsOk(socialInteractionCounts);
		return ResponseUtils.corsOk("");
	}
	
	@GET
	@Path("/student/{id}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getStudentInteractions(@PathParam("id") Long id) {
		logger.debug("Service 'getStudentInteractions' called.");
		//TODO Aleksandar This service should have course
		//List<SocialInteractionCount> socialInteractionCounts = dbManager.getSocialInteractionCounts(id);
		//return ResponseUtils.corsOk(socialInteractionCounts);
		return ResponseUtils.corsOk("");
	}
	
	@GET
	//@Path("/")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getInteractions(@QueryParam("courseId") Long courseId, @QueryParam("studentId") Long studentId) {
		Random generator = new Random(courseId * studentId);
		logger.debug("Service 'getInteractions' called.");
		List<Map<String, String>> result = new ArrayList<Map<String,String>>();
		for(int i = 1; i < generator.nextInt(1000); i++) {
			HashMap<String, String> interaction = new HashMap<String,String>();
			interaction.put("source", Integer.toString(generator.nextInt(100)));
			interaction.put("target", Integer.toString(generator.nextInt(100)));
			interaction.put("cluster", Integer.toString(generator.nextInt(4)));
			interaction.put("count", Integer.toString(generator.nextInt(50)));			
			result.add(interaction);
		}
		return ResponseUtils.corsOk(result);
	}



	@GET
	@Path("/outer")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getOuterInteractions(@QueryParam("courseId") Long courseId, @QueryParam("studentId") Long studentId) {
		logger.debug("Service 'getOuterInteractions' called.");
		return ResponseUtils.corsOk(dbManager.getOuterInteractions(courseId, studentId));
		// return ResponseUtils.corsOk(randomOuterInteractions(courseId, studentId));
	}

	@GET
	@Path("/cluster")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getClusterInteractions(@QueryParam("courseId") Long courseId, @QueryParam("studentId") Long studentId) {
		logger.debug("Service 'getClusterInteractions' called:courseId:"+courseId+" student:"+studentId);
		return ResponseUtils.corsOk(dbManager.getClusterInteractions(courseId, studentId));
		// return ResponseUtils.corsOk(randomClusterInteractions(courseId, studentId));
	}

	@GET
	@Path("/data")
	@Produces({ MediaType.APPLICATION_JSON })
	//public Response getStudentData(@QueryParam("students[]") Long[] students) {
		public Response getStudentData(@QueryParam("students[]") List<Long> studentslist) {
		Long[] students=studentslist.toArray(new Long[studentslist.size()]);
		logger.debug("Service 'getStudentData' called.");
		Map<Long, Map<String, String>> studentsData = userDao.getUsersData(students);
		studentsData.keySet().forEach(new Consumer<Long>() {

			@Override
			public void accept(Long key) {
				String avatar = studentsData.get(key).get("avatar");
				studentsData.get(key).put("avatar", getAvatarUrlInFormat(key, avatar, ImageFormat.size120x120));
			}

		});
		return ResponseUtils.corsOk(studentsData);
		// return ResponseUtils.corsOk(randomStudentsData(students));
	}

	private String getAvatarUrlInFormat(Long userId, String avatarUrl, ImageFormat format) {
		if (avatarUrl != null && avatarUrl.startsWith("http")) {
			return avatarUrl;
		}

		UserServiceConfig avatarConfig = CommonSettings.getInstance().config.services.userService;

		if (avatarUrl == null || avatarUrl.equals("") || avatarUrl.equals(avatarConfig.defaultAvatarName)) {
			return "/" + avatarConfig.defaultAvatarPath + format + ".png";
		}
		//String serviceUrl = CommonSettings.getInstance().config.fileStore.fileStoreServiceUrl;
		//String bucketName = CommonSettings.getInstance().config.fileStore.fileStoreBucketName;
		String avatarPath = avatarConfig.userAvatarPath;
		String separator = File.separator;
		//return serviceUrl + separator + bucketName + separator + avatarPath + avatarUrl + separator + format + ".png";
		return CommonSettings.getInstance().config.fileStore.getFilePath()+ avatarPath + avatarUrl + separator + format + ".png";
	}

	@GET
	@Path("/interactionsbypeers/{course}/{student}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getInteractionsByPeers(@PathParam("course") Long courseId, @PathParam("student") Long studentId) {
		logger.debug("Service 'getInteractionsByPeers' called.");
		return ResponseUtils.corsOk(dbManager.getInteractionsByPeers(courseId, studentId));
		// return ResponseUtils.corsOk(randomOuterInteractions(courseId, studentId));
	}
	@GET
	@Path("/interactionsbytype/{course}/{student}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getInteractionsByType(@PathParam("course") Long courseId, @PathParam("student") Long studentId) {
		logger.debug("Service 'getInteractionsByType' called.");
		System.out.println("INTERACTIONS BY TYPE:"+dbManager.getInteractionsByType(courseId, studentId));
		return ResponseUtils.corsOk(dbManager.getInteractionsByType(courseId, studentId));
		// return ResponseUtils.corsOk(randomOuterInteractions(courseId, studentId));
	}


	
}
