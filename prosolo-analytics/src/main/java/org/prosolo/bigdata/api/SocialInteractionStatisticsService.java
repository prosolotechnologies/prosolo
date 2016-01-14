package org.prosolo.bigdata.api;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.prosolo.bigdata.common.dal.pojo.SocialInteractionCount;
import org.prosolo.bigdata.dal.cassandra.SocialInteractionStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/social/interactions")
public class SocialInteractionStatisticsService {

	private final Logger logger = LoggerFactory.getLogger(SocialInteractionStatisticsService.class);
	
	private SocialInteractionStatisticsDBManager  dbManager = new SocialInteractionStatisticsDBManagerImpl();

	
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
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (int i = 1; i < generator.nextInt(100); i++) {
			Map<String, Object> student = new HashMap<String, Object>();
			student.put("student", Integer.toString(generator.nextInt(100)));
			student.put("cluster", Integer.toString(generator.nextInt(4)));
			student.put("name", Integer.toString(generator.nextInt(50)));
			student.put("avatar", Integer.toString(generator.nextInt(50)));
			List<Map<String, String>> interactions = new ArrayList<Map<String, String>>();
			for (int j = 1; j < generator.nextInt(100); j++) {
				Map<String, String> interaction = new HashMap<String, String>();
				interaction.put("target", Integer.toString(generator.nextInt(100)));
				interaction.put("count", Integer.toString(generator.nextInt(50)));
				interactions.add(interaction);
			}
			student.put("interactions", interactions);
			result.add(student);
		}
		return ResponseUtils.corsOk(result);
	}
	
}
