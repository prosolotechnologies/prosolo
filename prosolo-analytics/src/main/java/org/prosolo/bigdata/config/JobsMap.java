package org.prosolo.bigdata.config;

import java.util.ArrayList;
import java.util.Map;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;

/**
 * @author Zoran Jeremic May 19, 2015
 *
 */
// @Root(name="quartz-jobs")
public class JobsMap {

	// @ElementMap(entry="job", key="class", attribute=true, inline=true)
	@ElementList(entry = "job", inline = true)
	public ArrayList<QuartzJobConfig> jobsConfig;
}
