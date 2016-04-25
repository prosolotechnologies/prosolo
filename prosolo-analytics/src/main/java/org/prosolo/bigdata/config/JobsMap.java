package org.prosolo.bigdata.config;


import java.util.Map;

import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

/**
 * @author Zoran Jeremic May 19, 2015
 *
 */

//@Root
public class JobsMap {

	@ElementMap(entry= "class", key="name", attribute=true, inline=true)
	public Map<String,QuartzJobConfig> jobsConfig;
	 //@ElementList(entry = "job", inline = true)
	 //public ArrayList<QuartzJobConfig> jobsConfig;

}
