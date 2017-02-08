package org.prosolo.app;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.prosolo.services.rest.api.LTIServiceOutcome;



/**
@author Zoran Jeremic Dec 20, 2014
*
*/
//@ApplicationPath("/api")
public class WebApplication extends Application{
	
	Set<Object> singletons = new HashSet<Object>();
	
	public WebApplication(){
		System.out.println("INIT WEB APPLICATION: REST API exposed at:http://localhost:8080/api/servicepath");
		singletons.add(new LTIServiceOutcome());
		//singletons.add(new ActivityRestService());
	}
	@Override
	public Set<Class<?>> getClasses() {
		HashSet<Class<?>> set = new HashSet<Class<?>>();
		return set;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}