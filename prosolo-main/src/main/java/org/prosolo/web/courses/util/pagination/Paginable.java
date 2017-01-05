package org.prosolo.web.courses.util.pagination;

public interface Paginable {

	public void changePage(int page);

	public PaginationData getPaginationData();

}
