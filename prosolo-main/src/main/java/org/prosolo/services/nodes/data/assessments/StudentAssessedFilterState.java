package org.prosolo.services.nodes.data.assessments;

public class StudentAssessedFilterState {

	private StudentAssessedFilter filter;
	private boolean applied;
	
	public StudentAssessedFilterState() {
		
	}
	
	public StudentAssessedFilterState(StudentAssessedFilter filter, boolean applied) {
		this.filter = filter;
		this.applied = applied;
	}

	public StudentAssessedFilter getFilter() {
		return filter;
	}

	public void setFilter(StudentAssessedFilter filter) {
		this.filter = filter;
	}

	public boolean isApplied() {
		return applied;
	}

	public void setApplied(boolean applied) {
		this.applied = applied;
	}
	
}
