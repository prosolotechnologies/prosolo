package org.prosolo.web.util.pagination;

public interface Paginable {

	public void changePage(int page);

	public PaginationData getPaginationData();

}
