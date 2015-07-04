package org.prosolo.bigdata.config;

import org.simpleframework.xml.Element;

/**
@author Zoran Jeremic May 17, 2015
 *
 */

public class InitConfig {
	
	@Element(name = "formatDB", required = false)
	public boolean formatDB = true;
	
	@Element(name = "formatES", required = false)
	public boolean formatES = true;
}

