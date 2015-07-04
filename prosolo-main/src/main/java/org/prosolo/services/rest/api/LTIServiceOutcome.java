package org.prosolo.services.rest.api;


import java.io.IOException;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.oauth.OAuthException;

import org.prosolo.app.Settings;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.externalIntegration.BasicLTIResponse;
import org.prosolo.services.externalIntegration.ExternalToolService;
import org.prosolo.services.nodes.CourseManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
@author Zoran Jeremic Dec 20, 2014
 *
 */
@Path("/lti")
public class LTIServiceOutcome {
	
 
	
	@GET
	@Path("/test")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces("application/json")
	public Response test() {
		System.out.println("TEST CALLED");
		return Response.status(Status.OK).entity("{'Status':'OK'}").build();
		//return "{'Status':'OK'}";
	}
	
	@POST
	@Path("/replaceresult")
	@Consumes("application/xml")
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response replaceResult(@HeaderParam("Authorization") String authorization, 
			org.w3c.dom.Document w3cDoc) throws TransformerFactoryConfigurationError, TransformerException {
			System.out.println("REPLACE RESULT CALLED");
			boolean authorised=false;
			String failureMessage=null;
			try {
				String consumerSecret=ServiceLocator.getInstance().getService(ExternalToolService.class).retrieveConsumerSecret(w3cDoc);
				String url= Settings.getInstance().config.application.domain+"api/lti/replaceresult";
				 authorised=ServiceLocator.getInstance().getService(ExternalToolService.class).checkAuthorization(authorization,url,"POST", consumerSecret);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				failureMessage=e.getLocalizedMessage();
			} catch (OAuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				failureMessage=e.getLocalizedMessage();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				failureMessage=e.getLocalizedMessage();
			}
			BasicLTIResponse response=null;
			if(authorised){
				 response=ServiceLocator.getInstance().getService(ExternalToolService.class).processReplaceResultOutcome(w3cDoc);
			}else{
				 response=ServiceLocator.getInstance().getService(ExternalToolService.class).processFailureResponse(w3cDoc,failureMessage);
			}
		   	return Response.status(Status.OK).entity(response.toXML()).build();
		//return "{'Status':'OK'}";
	}
	/*     sprintf(getPOXResponse(),uniqid(),'success', "Score read successfully",$message_ref,$body);
	*/
	
	
}

