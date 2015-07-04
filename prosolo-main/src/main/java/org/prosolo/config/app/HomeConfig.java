package org.prosolo.config.app;

import org.simpleframework.xml.Element;

public class HomeConfig {
	
	@Element(name = "learning-progress")
	public LearningProgressConfig learningProgress;

}
