package org.prosolo.common.domainmodel.credential;

public enum ScoreCalculation {

	BEST_SCORE("Keep the best score"),
	LATEST_SCORE("Keep the latest score"),
	AVG("Calculate average");
	
	private String label;
	
	private ScoreCalculation(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
}
