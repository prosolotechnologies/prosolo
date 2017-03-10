package org.prosolo.search.util.competences;

public class CompetenceStudentsSearchFilter {

	private CompetenceStudentsSearchFilterValue filter;
	private long numberOfResults;
	
	public CompetenceStudentsSearchFilter(CompetenceStudentsSearchFilterValue filter, long numberOfResults) {
		this.filter = filter;
		this.numberOfResults = numberOfResults;
	}
	
	public CompetenceStudentsSearchFilterValue getFilter() {
		return filter;
	}
	public void setFilter(CompetenceStudentsSearchFilterValue filter) {
		this.filter = filter;
	}
	public long getNumberOfResults() {
		return numberOfResults;
	}
	public void setNumberOfResults(long numberOfResults) {
		this.numberOfResults = numberOfResults;
	}
	
	
}
