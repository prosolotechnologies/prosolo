package org.prosolo.config.app;

import org.simpleframework.xml.Element;

public class ProfileConfig {
	
	@Element(name = "goals-to-show")
	public int goalsToShow;

}
