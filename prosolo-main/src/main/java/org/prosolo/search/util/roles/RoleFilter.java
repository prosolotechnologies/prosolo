package org.prosolo.search.util.roles;

public class RoleFilter {

	private long id;
	private String label;
	private long numberOfResults;
	
	public RoleFilter(long id, String label, long numberOfResults) {
		this.id = id;
		this.label = label;
		this.numberOfResults = numberOfResults;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public long getNumberOfResults() {
		return numberOfResults;
	}
	
	public void setNumberOfResults(long numberOfResults) {
		this.numberOfResults = numberOfResults;
	}
	
}
