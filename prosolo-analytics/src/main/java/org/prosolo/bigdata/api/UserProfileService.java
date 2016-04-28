package org.prosolo.bigdata.api;

import org.prosolo.bigdata.dal.cassandra.impl.UserObservationsDBManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by zoran on 27/04/16.
 */
@Path("/user/profile")
public class UserProfileService {
    private final Logger logger = LoggerFactory.getLogger(UserProfileService.class);
    @GET
    @Path("/currentprofileincourse/{course}/{student}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getCurrentProfileInCourse(@PathParam("course") Long courseId, @PathParam("student") Long studentId) {
        logger.debug("Service 'currentprofileincourse' called.");
        return ResponseUtils.corsOk(UserObservationsDBManagerImpl.getInstance().findUserCurrentProfileInCourse(courseId, studentId));
        // return ResponseUtils.corsOk(randomOuterInteractions(courseId, studentId));
    }
}
