package org.prosolo.bigdata.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.prosolo.bigdata.common.dal.pojo.RegisteredUsersCount;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyzedResultsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyzedResultsDBmanagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.UsersActivityStatisticsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.UsersActivityStatisticsDBManagerImpl;
import org.prosolo.bigdata.utils.DateUtil;

import com.google.gson.Gson;

@Path("/users/activity")
public class UsersActivityStatisticsService {

	UsersActivityStatisticsDBManager dbManager = new UsersActivityStatisticsDBManagerImpl();

	@GET
	@Path("/statistics/registered")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getRegistered() {
		List<RegisteredUsersCount> result = dbManager.getRegisteredUsersCount(0, DateUtil.getDaysSinceEpoch());
		return Response
				.status(Status.OK)
				.entity(new Gson().toJson(result))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods",
						"GET, POST, DELETE, PUT").allow("OPTIONS").build();
	}

}
