package org.prosolo.search.util.credential;

public class InstructorAssignFilter {

	private InstructorAssignFilterValue filter;
	private long numberOfResults;
	
	public InstructorAssignFilter(InstructorAssignFilterValue filter, long numberOfResults) {
		this.filter = filter;
		this.numberOfResults = numberOfResults;
	}
	
	public InstructorAssignFilterValue getFilter() {
		return filter;
	}
	public void setFilter(InstructorAssignFilterValue filter) {
		this.filter = filter;
	}
	public long getNumberOfResults() {
		return numberOfResults;
	}
	public void setNumberOfResults(long numberOfResults) {
		this.numberOfResults = numberOfResults;
	}
	
	
}
