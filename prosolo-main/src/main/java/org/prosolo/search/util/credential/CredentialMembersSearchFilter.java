package org.prosolo.search.util.credential;

public class CredentialMembersSearchFilter {

	private SearchFilter filter;
	private long numberOfResults;

	public CredentialMembersSearchFilter(SearchFilter filter, long numberOfResults) {
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
		All("All students"),
		//Unassigned("Without " + ResourceBundleUtil.getLabel("instructor").toLowerCase()),
		//Assigned("With " + ResourceBundleUtil.getLabel("instructor").toLowerCase()),
		AssessorNotified("Asked to be assessed"),
		Nongraded("Nongraded"),
		Graded("Graded"),
		Completed("Completed");

		private String label;

		SearchFilter(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}
}
