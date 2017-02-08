package org.prosolo.bigdata.app;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.api.*;

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
		logger.info("INIT WEB APPLICATION: REST API exposed at:http://{host}:{port}/api/{servicepath}");
		singletons.add(new PingResource());
		singletons.add(new RecommendationServices());
		singletons.add(new UsersActivityStatisticsService());
		singletons.add(new TwitterHashtagStatisticsService());
		singletons.add(new SocialInteractionStatisticsService());
		singletons.add(new LearningActivityService());
		singletons.add(new UserProfileService());
		singletons.add(new ActivityRestService());
	}
}
