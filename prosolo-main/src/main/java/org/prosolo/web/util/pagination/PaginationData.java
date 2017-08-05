package org.prosolo.web.util.pagination;

import java.util.LinkedList;
import java.util.List;

public class PaginationData {

	private int numberOfResults;
	private int limit;
	private int page;
	private boolean generateAllLinks;
	// default is 1
	private int numberOfLinksArroundCurrent = 1;
	private String missingLinksOutput = "...";
	private int numberOfPages;
	
	private List<PaginationLink> links;
	
	public PaginationData() {
		this.limit = 10;
		this.page = 1;
		this.generateAllLinks = false;
		this.numberOfLinksArroundCurrent = 1;
		update(numberOfResults);
	}
	
	public PaginationData(int numberOfResults, int limit, int page,
			boolean generateAllLinks, int numberOfLinksArroundCurrent, String missingLinksOutput) {
		this.limit = limit;
		this.page = page;
		this.generateAllLinks = generateAllLinks;
		this.numberOfLinksArroundCurrent = numberOfLinksArroundCurrent;
		this.missingLinksOutput = missingLinksOutput;
		update(numberOfResults);
	}
	
	public PaginationData(int limit) {
		this();
		this.limit = limit;
		update(numberOfResults);
	}

	public void update(int numberOfResults) {
		this.numberOfResults = numberOfResults;
		this.numberOfPages = (int) Math.ceil((double) this.numberOfResults / this.limit);
		generatePaginationLinks();
	}

	private void generatePaginationLinks() {
		this.links = new LinkedList<>();

		if (generateAllLinks) {
			for (int i = 1; i <= numberOfPages; i++) {
				boolean selected = page == i;
				PaginationLink link = new PaginationLink(i, i + "", selected, true);
				this.links.add(link);
			}
		} else {
			int start = page - numberOfLinksArroundCurrent;
			// <= 2 because link for first page should always be generated
			// so we shouldn't generate "..."
			if (start <= 2) {
				start = 1;
			} else if (start > 2) {
				PaginationLink firstPage = new PaginationLink(1, 1 + "", false, true);
				this.links.add(firstPage);
				PaginationLink link = new PaginationLink(0, missingLinksOutput, false, false);
				this.links.add(link);
			}
			int upperBound = page + numberOfLinksArroundCurrent;
			// >= numberOfPages - 1 because last page link should always be
			// generated
			if (upperBound >= numberOfPages - 1) {
				upperBound = numberOfPages;
			}

			for (int i = start; i <= upperBound; i++) {
				boolean selected = page == i;
				PaginationLink link = new PaginationLink(i, i + "", selected, true);
				this.links.add(link);
			}

			if (upperBound < numberOfPages - 1) {
				PaginationLink link = new PaginationLink(0, missingLinksOutput, false, false);
				this.links.add(link);
				PaginationLink lastPage = new PaginationLink(numberOfPages, numberOfPages + "", false, true);
				this.links.add(lastPage);
			}
		}
	}
	
	public boolean isCurrentPageFirst() {
		return page == 1 || numberOfPages == 0;
	}
	
	public boolean isCurrentPageLast() {
		return page == numberOfPages || numberOfPages == 0;
	}
	
	public boolean isResultSetEmpty() {
		return numberOfResults == 0;
	}
	
	public boolean shouldBeDisplayed() {
		return numberOfPages > 1;
	}
	
	public int getNextPage() {
		return page + 1;
	}
	
	public int getPreviousPage() {
		return page - 1;
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public int getNumberOfResults() {
		return numberOfResults;
	}

	public void setNumberOfResults(int numberOfResults) {
		this.numberOfResults = numberOfResults;
	}

	public int getLimit() {
		return limit;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public boolean isGenerateAllLinks() {
		return generateAllLinks;
	}

	public int getNumberOfLinksArrountCurrent() {
		return numberOfLinksArroundCurrent;
	}

	public String getMissingLinksOutput() {
		return missingLinksOutput;
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

	public List<PaginationLink> getLinks() {
		return links;
	}
	
	/*
	 * UTILITY
	 */


	public String getPageString() {
		return String.valueOf(page);
	}

	public void setPageString(String pageString) {
		try {
			page = Integer.parseInt(pageString);
		} catch (Exception e) {
		}
	}
}
