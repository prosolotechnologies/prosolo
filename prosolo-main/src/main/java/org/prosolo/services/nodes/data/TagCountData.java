package org.prosolo.services.nodes.data;

import org.prosolo.common.domainmodel.general.BaseEntity;

public class TagCountData extends BaseEntity implements Comparable<TagCountData>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long id;
	private String title;
	private long count;

	public TagCountData(String title, long count) {
		super();
		this.title = title;
		this.count = count;
	}
	
	public TagCountData() {
	}


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "title=" + title + ", count=" + count;
	}

	@Override
	public int compareTo(TagCountData o) {
		return this.getTitle().compareTo(o.getTitle());
	}

}
