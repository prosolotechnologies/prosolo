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
		List<SocialInteractionCount> socialInteractionCounts = dbManager.getSocialInteractionCounts();
		return ResponseUtils.corsOk(socialInteractionCounts);
	}
	
	@GET
	@Path("/student/{id}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getStudentInteractions(@PathParam("id") Long id) {
		logger.debug("Service 'getStudentInteractions' called.");
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
	
}
