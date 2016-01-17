package org.prosolo.bigdata.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/social/interactions")
public class SocialInteractionStatisticsService {

	private final Logger logger = LoggerFactory.getLogger(SocialInteractionStatisticsService.class);
	
	@GET
	@Path("/cluster")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getClusterInteractions(@QueryParam("courseId") Long courseId, @QueryParam("studentId") Long studentId) {
		logger.debug("Service 'getClusterInteractions' called.");
		Random generator = new Random(courseId * studentId);
		String cluster = "0";
		String course = "1";
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (int i = 1; i < generator.nextInt(100); i++) {
			Map<String, Object> student = new HashMap<String, Object>();
			student.put("courseid", course);
			student.put("clusterid", cluster);
			student.put("student", Integer.toString(generator.nextInt(100)));
			student.put("name", Integer.toString(generator.nextInt(50)));
			student.put("avatar", Integer.toString(generator.nextInt(50)));
			List<Map<String, String>> interactions = new ArrayList<Map<String, String>>();
			for (int j = 1; j < generator.nextInt(20); j++) {
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
	
	@GET
	@Path("/outer")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getOuterInteractions(@QueryParam("courseId") Long courseId, @QueryParam("studentId") Long studentId) {
		logger.debug("Service 'getOuterInteractions' called.");
		Random generator = new Random(courseId * studentId);
		String cluster = "0";
		String course = "1";
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (int i = 1; i < generator.nextInt(20); i++) {
			Map<String, Object> student = new HashMap<String, Object>();
			student.put("courseid", course);
			student.put("clusterid", cluster);
			student.put("student", studentId);
			student.put("direction", generator.nextInt(2) == 0 ? "source" : "target");
			student.put("name", Integer.toString(generator.nextInt(50)));
			student.put("avatar", Integer.toString(generator.nextInt(50)));
			List<Map<String, String>> interactions = new ArrayList<Map<String, String>>();
			for (int j = 1; j < generator.nextInt(20); j++) {
				Map<String, String> interaction = new HashMap<String, String>();
				interaction.put("target", Integer.toString(generator.nextInt(100)));
				interaction.put("clusterid", Integer.toString(generator.nextInt(3) + 1));
				interaction.put("count", Integer.toString(generator.nextInt(50)));
				interactions.add(interaction);
			}
			student.put("interactions", interactions);
			result.add(student);
		}
		return ResponseUtils.corsOk(result);
	}

}
