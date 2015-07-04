/**
 * 
 */
package org.prosolo.services.rest.courses;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.rest.courses.data.CompetenceJsonData;

/**
 * @author "Nikola Milikic"
 *
 */
@Path("/courses")
public class CourseRestService {

	private static Logger logger = Logger.getLogger(CourseRestService.class);

	@GET
	@Path("{id}")
	@Produces("application/json")
	public String getCourseCompetences(@PathParam("id") long id) {
		try{
			List<CompetenceJsonData> competences = ServiceLocator.getInstance().getService(CourseManager.class).getCourseComeptences(id);
	
			if (competences == null) {
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}
			
			return CourseParser.printToJson(competences);
		} catch (NumberFormatException nfe) {
			logger.error("CourseDataServlet has been passed bad value for 'id' query parameter, it is not long");
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
	}
	
}
