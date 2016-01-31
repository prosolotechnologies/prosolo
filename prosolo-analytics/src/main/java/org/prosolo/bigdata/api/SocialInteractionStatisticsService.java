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

import com.google.gson.Gson;
import org.prosolo.bigdata.common.dal.pojo.SocialInteractionCount;
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
	
	private UserDAO userDao = new UserDAOImpl();
	
	@GET
	@Path("/all")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllUserInteractions() throws ParseException {
		logger.debug("Service 'getAllUserInteractions' called");
		long courseid=1;
		//TODO Aleksandar This service doesn't have too much sense. It should be for course
		List<SocialInteractionCount> socialInteractionCounts = dbManager.getSocialInteractionCounts(courseid);
		return ResponseUtils.corsOk(socialInteractionCounts);
	}
	
	@GET
	@Path("/student/{id}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getStudentInteractions(@PathParam("id") Long id) {
		logger.debug("Service 'getStudentInteractions' called.");
		//TODO Aleksandar This service should have course
		List<SocialInteractionCount> socialInteractionCounts = dbManager.getSocialInteractionCounts(id);
		return ResponseUtils.corsOk(socialInteractionCounts);
	}
	
	@GET
	@Path("/")
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

	@SuppressWarnings("unused")
	private List<Map<String, Object>> randomClusterInteractions(Long courseId, Long studentId) {
		Random generator = new Random(courseId * studentId);
		String cluster = "0";
		String course = "1";
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (int i = 1; i < generator.nextInt(100); i++) {
			Map<String, Object> student = new HashMap<String, Object>();
			student.put("course", course);
			student.put("cluster", cluster);
			student.put("student", Integer.toString(generator.nextInt(100)));
			student.put("name", Integer.toString(generator.nextInt(50)));
			student.put("avatar", Integer.toString(generator.nextInt(50)));
			List<Map<String, String>> interactions = new ArrayList<Map<String, String>>();
			for (int j = 1; j < generator.nextInt(20); j++) {
				Map<String, String> interaction = new HashMap<String, String>();
				interaction.put("target", Integer.toString(generator.nextInt(100)));
				interaction.put("count", Integer.toString(generator.nextInt(100)));
				interactions.add(interaction);
			}
			student.put("interactions", interactions);
			result.add(student);
		}
		return result;
	}

	@SuppressWarnings("unused")
	private List<Map<String, Object>> randomOuterInteractions(Long courseId, Long studentId) {
		Random generator = new Random(courseId * studentId);
		String cluster = "0";
		String course = "1";
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (int i = 1; i < generator.nextInt(20); i++) {
			Map<String, Object> student = new HashMap<String, Object>();
			student.put("course", course);
			student.put("cluster", cluster);
			student.put("student", studentId);
			student.put("direction", generator.nextInt(2) == 0 ? "SOURCE" : "TARGET");
			student.put("name", Integer.toString(generator.nextInt(50)));
			student.put("avatar", Integer.toString(generator.nextInt(50)));
			List<Map<String, String>> interactions = new ArrayList<Map<String, String>>();
			for (int j = 1; j < generator.nextInt(20); j++) {
				Map<String, String> interaction = new HashMap<String, String>();
				interaction.put("target", Integer.toString(generator.nextInt(100)));
				interaction.put("cluster", Integer.toString(generator.nextInt(100) + 1));
				interaction.put("count", Integer.toString(generator.nextInt(100)));
				interactions.add(interaction);
			}
			student.put("interactions", interactions);
			result.add(student);
		}
		return result;
	}
	
	@SuppressWarnings("unused")
	private Map<Long, Map<String, String>> randomStudentsData(Long[] students) {
		String[] names = new String[] {
				"Nick Powell",
				"Eva Lu Ator",
				"Adam Admin",
				"Bob Alice",
				"P Hacker",
				"John Smith",
				"John Doe",
				"Peter Petrovich",
				"Joe Armstrong",
				"Erik Meijer"
		};
		String[] avatars = new String[] {
				"https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcQiU2-3iy-GtYgWj0JiWocImdK4DTR2MhIX4VD67O-_CjQA3cZw",
				"https://www.pedge.com/secretshopper2x2.jpg",
				"https://metrouk2.files.wordpress.com/2015/06/west.jpg",
				"http://wikis.zum.de/rmg/images/9/92/Bob_neutral.jpg",
				"http://image.eveonline.com/Character/93474039_256.jpg",
				"http://img.bleacherreport.net/img/images/photos/002/156/250/161589374_crop_north.jpg?w=630&h=420&q=75",
				"http://www.johndoe.pro/img/John_Doe.jpg",
				"http://www.chesshistory.com/winter/extra/pics/saburov2.jpg",
				"https://pbs.twimg.com/profile_images/625217739280396288/LI3MIbLg.jpg",
				"http://2014.geekout.ee/wp-content/uploads/sites/4/2014/02/Erik-Meijer.jpg"
		};
		Map<Long, Map<String, String>> result = new HashMap<Long, Map<String, String>>();
		for(Long student : students) {
			Map<String, String> props = new HashMap<String, String>();
			props.put("name", names[(int) (student % 10)]);
			props.put("avatar", avatars[(int) (student % 10)]);
			result.put(student, props);
		}
		return result;
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
		logger.debug("Service 'getClusterInteractions' called.");
		return ResponseUtils.corsOk(dbManager.getClusterInteractions(courseId));
		// return ResponseUtils.corsOk(randomClusterInteractions(courseId, studentId));
	}

	@GET
	@Path("/data")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getStudentData(@QueryParam("students[]") Long[] students) {
		logger.debug("Service 'getStudentData' called.");
		Map<Long, Map<String, String>> studentsData = userDao.getUsersData(students);
		studentsData.keySet().forEach(new Consumer<Long>() {

			@Override
			public void accept(Long key) {
				String avatar = studentsData.get(key).get("avatar");
				studentsData.get(key).put("avatar", getAvatarUrlInFormat(key, avatar, ImageFormat.size60x60));
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
		String serviceUrl = CommonSettings.getInstance().config.fileStore.fileStoreServiceUrl;
		String bucketName = CommonSettings.getInstance().config.fileStore.fileStoreBucketName;
		String avatarPath = avatarConfig.userAvatarPath;
		String separator = File.separator;
		return serviceUrl + separator + bucketName + separator + avatarPath + avatarUrl + separator + format + ".png";
	}
	
}
