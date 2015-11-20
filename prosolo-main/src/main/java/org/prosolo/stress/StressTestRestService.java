package org.prosolo.stress;

import java.io.InputStream;

import javax.faces.bean.ManagedBean;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.web.LoggedUserBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
@author Zoran Jeremic Nov 19, 2013
 */
@Component
@ManagedBean(name="restServiceTest")
 
@Scope("session")
@Path("/test/stress")
public class StressTestRestService {

	//@Autowired LoggedUserBean loggedUser;
 
	@POST
	@Path("/basic")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ "application/x-javascript", MediaType.APPLICATION_JSON })
	public Response runBasicStressTests(InputStream input) {
	  String inputString=ServiceUtils.convertInputStreamToString(input);
	  Gson gson=new Gson();
	 TestInpuParameters inputParams= gson.fromJson(inputString, TestInpuParameters.class);
		LoggedUserBean loggedUser=ServiceLocator.getInstance().getService(LoggedUserBean.class);
		//loggedUser.setEmail(inputParams.getUsername());
		//loggedUser.setPassword(inputParams.getPassword());
	    //change
		//loggedUser.login();
	 return Response.status(200).entity("output").build();
		
	}
}
