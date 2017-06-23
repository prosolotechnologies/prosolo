/**
 * 
 */
package org.prosolo.common.web.digest.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.web.digest.FilterOption;
 

/**
 * @author "Nikola Milikic"
 * 
 */
public class FeedsDigestData implements Serializable {
	
	private static final long serialVersionUID = -1213055305491933013L;
	
	private List<FeedEntryData> entries;
	private int page;
	private boolean moreToLoad;
	private String categoryName;
	private String filter;
	
	public FeedsDigestData() {
		entries = new ArrayList<FeedEntryData>();
		this.page = 1;
	}
	
	public List<FeedEntryData> getEntries() {
		return entries;
	}
	
	public void setEntries(List<FeedEntryData> entries) {
		this.entries = entries;
	}
	
	public void addEntry(FeedEntryData entry) {
		this.entries.add(entry);
	}
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
	
	public void incrementPage() {
		this.page++;
	}
	
	public boolean isMoreToLoad() {
		return moreToLoad;
	}

	public void setMoreToLoad(boolean moreToLoad) {
		this.moreToLoad = moreToLoad;
	}
	
	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	public void setFilter(FilterOption filterOption) {
		this.filter = filterOption.name().toLowerCase();
	}

	@Override
	public String toString() {
		return "FeedsDigestData [entries=" + entries + ", page=" + page + ", moreToLoad="
				+ moreToLoad + "]";
	}

}
