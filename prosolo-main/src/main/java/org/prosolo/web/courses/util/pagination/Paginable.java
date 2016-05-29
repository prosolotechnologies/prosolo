package org.prosolo.web.courses.util.pagination;

public interface Paginable {

	public boolean isCurrentPageFirst();
	
	public boolean isCurrentPageLast();
	
	public void changePage(int page);
	
	public void goToPreviousPage();
	
	public void goToNextPage();
	
	public boolean isResultSetEmpty();
}
