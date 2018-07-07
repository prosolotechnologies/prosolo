package org.prosolo.services.rest.api;


import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.application.HealthCheckService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


/**
 * @author Nikola Milikic
 * @date 2018-07-05
 * @since 1.2
 */
@Path("/health")
public class HealthCheckRESTService {

	@GET
	@Produces("application/json")
	public Response test() {
		try{
			ServiceLocator.getInstance().getService(HealthCheckService.class).pingDatabase();
			return Response.status(Status.OK).build();
		} catch (DbConnectionException e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
	
}

