package org.prosolo.bigdata.config;

import org.simpleframework.xml.Element;

/**
 * @author Zoran Jeremic May 19, 2015
 *
 */

public class QuartzJobConfig {

	@Element(name = "class-name", required = true)
	public String className;

	@Element(name = "activated", required = false)
	public boolean activated = true;

	@Element(name = "on-startup", required = false)
	public boolean onStartup = true;

	@Element(name="schedule", required=false)
	public String schedule;
}
