package org.prosolo.search.util.credential;

public class LearningStatusFilter {

	private LearningStatus status;
	private long numberOfResults;
	
	public LearningStatusFilter(LearningStatus status, long numberOfResults) {
		this.status = status;
		this.numberOfResults = numberOfResults;
	}
	
	public LearningStatus getStatus() {
		return status;
	}
	
	public void setStatus(LearningStatus status) {
		this.status = status;
	}
	
	public long getNumberOfResults() {
		return numberOfResults;
	}
	
	public void setNumberOfResults(long numberOfResults) {
		this.numberOfResults = numberOfResults;
	}
	
}
