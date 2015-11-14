package org.prosolo.bigdata.api;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
}
