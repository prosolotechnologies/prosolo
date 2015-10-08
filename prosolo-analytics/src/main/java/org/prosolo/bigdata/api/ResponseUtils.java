package org.prosolo.bigdata.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;

public class ResponseUtils {
	
	public static Response corsOk(Object counts) {
		return Response
				.status(Status.OK)
				.entity(new Gson().toJson(counts))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods",
						"GET, POST, DELETE, PUT").allow("OPTIONS").build();
	}

}
