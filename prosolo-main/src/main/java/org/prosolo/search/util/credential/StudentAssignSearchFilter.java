package org.prosolo.search.util.credential;

public class StudentAssignSearchFilter {

	private SearchFilter filter;
	private long numberOfResults;
	
	public StudentAssignSearchFilter(SearchFilter filter, long numberOfResults) {
		this.filter = filter;
		this.numberOfResults = numberOfResults;
	}
	
	public SearchFilter getFilter() {
		return filter;
	}
	public void setFilter(SearchFilter filter) {
		this.filter = filter;
	}
	public long getNumberOfResults() {
		return numberOfResults;
	}
	public void setNumberOfResults(long numberOfResults) {
		this.numberOfResults = numberOfResults;
	}

    public enum SearchFilter {
        All("All"),
        Unassigned("Only unassigned"),
        Assigned("Only assigned");

        private String label;

        private SearchFilter(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
