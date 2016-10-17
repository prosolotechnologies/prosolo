package org.prosolo.web.courses.util.pagination;

public class PaginationLink {

	private int page;
	private String linkOutput;
	private boolean selected;
	private boolean isLink;

	public PaginationLink() {

	}

	public PaginationLink(int page, String linkOutput, boolean selected, boolean isLink) {
		this.page = page;
		this.linkOutput = linkOutput;
		this.selected = selected;
		this.isLink = isLink;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getLinkOutput() {
		return linkOutput;
	}

	public void setLinkOutput(String linkOutput) {
		this.linkOutput = linkOutput;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isLink() {
		return isLink;
	}

	public void setLink(boolean isLink) {
		this.isLink = isLink;
	}

}
