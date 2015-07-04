package org.prosolo.bigdata.common.config;

import java.util.ArrayList;

import org.simpleframework.xml.ElementList;

/**
 * @author Zoran Jeremic Oct 8, 2014
 *
 */

public class ElasticSearchHostsConfig {

	@ElementList(entry = "es-host", inline = true)
	 public ArrayList<ElasticSearchHost> esHosts;
}
