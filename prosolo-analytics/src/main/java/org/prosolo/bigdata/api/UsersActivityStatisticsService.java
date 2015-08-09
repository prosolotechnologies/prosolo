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

import com.google.gson.Gson;

@Path("/users/activity")
public class UsersActivityStatisticsService {

	@GET
	@Path("/statistics/registered")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getRegistered() {
		List<RegisteredUsersCount> result = new ArrayList<>();
		for (long i = 0; i < 100; i++) {
			int count = (int) Math.round(Math.random() * 10);
			result.add(new RegisteredUsersCount("RegisteredUsersCount", count, i));
		}
		return Response
				.status(Status.OK)
				.entity(new Gson().toJson(result))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.allow("OPTIONS")
				.build();
	}

}
