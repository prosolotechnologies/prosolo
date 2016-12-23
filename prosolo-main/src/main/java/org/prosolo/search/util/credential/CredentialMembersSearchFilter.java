package org.prosolo.search.util.credential;

public class CredentialMembersSearchFilter {

	private CredentialMembersSearchFilterValue filter;
	private long numberOfResults;
	
	public CredentialMembersSearchFilter(CredentialMembersSearchFilterValue filter, long numberOfResults) {
		this.filter = filter;
		this.numberOfResults = numberOfResults;
	}
	
	public CredentialMembersSearchFilterValue getFilter() {
		return filter;
	}
	public void setFilter(CredentialMembersSearchFilterValue filter) {
		this.filter = filter;
	}
	public long getNumberOfResults() {
		return numberOfResults;
	}
	public void setNumberOfResults(long numberOfResults) {
		this.numberOfResults = numberOfResults;
	}
	
	
}
