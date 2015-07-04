package org.prosolo.config.app;

import org.simpleframework.xml.Element;

public class LearningProgressConfig {
	
	@Element(name = "number-of-competences")
	public int numberOfCompetences;
	
	@Element(name = "number-of-goals")
	public int numberOfGoals;

}
