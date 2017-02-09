package org.prosolo.web.courses.util.pagination;

import java.util.LinkedList;
import java.util.List;

import org.prosolo.web.util.pagination.PaginationLink;

public class Paginator {

	private int numberOfResults;
	private int limit;
	private int currentPage;
	private boolean generateAllLinks;
	// default is 1
	private int numberOfLinksArroundCurrent = 1;
	private String missingLinksOutput = "...";
	private int numberOfPages;

	public Paginator(int numberOfResults, int limit, int currentPage,
			int numberOfLinksArroundCurrent, String missingLinksOutput) {
		this.numberOfResults = numberOfResults;
		this.limit = limit;
		this.currentPage = currentPage;
		this.numberOfLinksArroundCurrent = numberOfLinksArroundCurrent;
		this.missingLinksOutput = missingLinksOutput;
		this.numberOfPages = (int) Math.ceil((double) this.numberOfResults / this.limit);
	}

	public Paginator(int numberOfResults, int limit, int currentPage, boolean generateAllLinks,
			String missingLinksOutput) {
		this.numberOfResults = numberOfResults;
		this.limit = limit;
		this.currentPage = currentPage;
		this.generateAllLinks = generateAllLinks;
		this.missingLinksOutput = missingLinksOutput;
		this.numberOfPages = (int) Math.ceil((double) this.numberOfResults / this.limit);
	}

	public List<PaginationLink> generatePaginationLinks() {
		List<PaginationLink> result = new LinkedList<>();

		if (generateAllLinks) {
			for (int i = 1; i <= numberOfPages; i++) {
				boolean selected = currentPage == i;
				PaginationLink link = new PaginationLink(i, i + "", selected, true);
				result.add(link);
			}
		} else {
			int start = currentPage - numberOfLinksArroundCurrent;
			// <= 2 because link for first page should always be generated
			// so we shouldn't generate "..."
			if (start <= 2) {
				start = 1;
			} else if (start > 2) {
				PaginationLink firstPage = new PaginationLink(1, 1 + "", false, true);
				result.add(firstPage);
				PaginationLink link = new PaginationLink(0, missingLinksOutput, false, false);
				result.add(link);
			}
			int upperBound = currentPage + numberOfLinksArroundCurrent;
			// >= numberOfPages - 1 because last page link should always be
			// generated
			if (upperBound >= numberOfPages - 1) {
				upperBound = numberOfPages;
			}

			for (int i = start; i <= upperBound; i++) {
				boolean selected = currentPage == i;
				PaginationLink link = new PaginationLink(i, i + "", selected, true);
				result.add(link);
			}

			if (upperBound < numberOfPages - 1) {
				PaginationLink link = new PaginationLink(0, missingLinksOutput, false, false);
				result.add(link);
				PaginationLink lastPage = new PaginationLink(numberOfPages, numberOfPages + "", false, true);
				result.add(lastPage);

			}
		}
		return result;
	}

	public int getNumberOfResults() {
		return numberOfResults;
	}

	public void setNumberOfResults(int numberOfResults) {
		this.numberOfResults = numberOfResults;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public boolean isGenerateAllLinks() {
		return generateAllLinks;
	}

	public void setGenerateAllLinks(boolean generateAllLinks) {
		this.generateAllLinks = generateAllLinks;
	}

	public int getNumberOfLinksArrountCurrent() {
		return numberOfLinksArroundCurrent;
	}

	public void setNumberOfLinksArrountCurrent(int numberOfLinksArrountCurrent) {
		this.numberOfLinksArroundCurrent = numberOfLinksArrountCurrent;
	}

	public String getMissingLinksOutput() {
		return missingLinksOutput;
	}

	public void setMissingLinksOutput(String missingLinksOutput) {
		this.missingLinksOutput = missingLinksOutput;
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

}
