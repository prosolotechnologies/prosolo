package org.prosolo.config.services;

import org.simpleframework.xml.Element;

public class ServicesConfig {
	
	@Element(name = "user")
	public UserServiceConfig userService;
	
	@Element(name = "activity-report")
	public ActivityReportServiceConfig activityReportService;
}
