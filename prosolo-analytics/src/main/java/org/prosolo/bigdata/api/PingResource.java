package org.prosolo.bigdata.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Zoran Jeremic Apr 2, 2015
 *
 */
@Path("/ping")
@Produces(MediaType.TEXT_PLAIN)
public class PingResource {
	@GET
	public String ping() {
		return "Pong!";
	}
}
