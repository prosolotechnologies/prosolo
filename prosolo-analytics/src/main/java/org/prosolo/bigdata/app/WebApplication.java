package org.prosolo.bigdata.app;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.api.PingResource;
import org.prosolo.bigdata.api.RecommendationServices;

/**
 * @author Zoran Jeremic Apr 2, 2015
 *
 */

public class WebApplication extends Application {
	private final static Logger logger = Logger.getLogger(WebApplication.class);
	HashSet<Object> singletons = new HashSet<Object>();

	@Override
	public Set<Class<?>> getClasses() {
		HashSet<Class<?>> set = new HashSet<Class<?>>();
		return set;
	}

	@Override
	public Set<Object> getSingletons() {
		return this.singletons;
	}

	public WebApplication() {
		logger.info("INIT WEB APPLICATION: REST API exposed at:http://{host}:8080/api/{servicepath}");
		singletons.add(new PingResource());
		singletons.add(new RecommendationServices());
	}
}
