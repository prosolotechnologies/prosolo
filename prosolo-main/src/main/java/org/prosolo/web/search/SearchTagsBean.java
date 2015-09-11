package org.prosolo.web.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.event.ValueChangeEvent;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "searchTagsBean")
@Component("searchTagsBean")
@Scope("view")
public class SearchTagsBean implements Serializable {

	private static final long serialVersionUID = -2666493762916257045L;

	@Autowired private TextSearch textSearch;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	
	private String query;
	private List<Tag> tags;
	private int tagsSize;
	
	private int page = 0;
	private int limit = 7;
	private boolean moreToLoad;
	
	public SearchTagsBean() {
		tags = new ArrayList<Tag>();
	}
	
	public void searchListener(ValueChangeEvent event) {
		this.limit = 3;
		search(event.getNewValue().toString(), null, false);
	}
	
	public void searchAllTags() {
		search(query, null, true);
	}
	
	public void search(String searchQuery) {
		search(searchQuery, null, false);
	}
	
	public void search(String searchQuery, Collection<Tag> tagsToExclude, boolean loadOneMore) {
		this.tags.clear();
		this.tagsSize = 0;
		
		fetchTags(searchQuery, tagsToExclude, loadOneMore, true);
		
		if (searchQuery != null && searchQuery.length() > 0) {
			loggingNavigationBean.logServiceUse(
					ComponentName.SEARCH_TAGS, 
					"query", searchQuery,
					"context", "search.tags"); 
		}
	}
	
	public void loadMore() {
		page++;
		fetchTags(query, null, true, false);
	}

	public void fetchTags(String searchQuery, Collection<Tag> tagsToExclude, boolean loadOneMore, boolean viewed) {
		TextSearchResponse searchResponse = textSearch.searchTags(
				searchQuery,
				this.page, 
				this.limit,
				loadOneMore,
				tagsToExclude);
		
		@SuppressWarnings("unchecked")
		List<Tag> foundTags = (List<Tag>) searchResponse.getFoundNodes();
		tagsSize = (int) searchResponse.getHitsNumber();
		
		// if there is more than limit, set moreToLoad to true
		if (loadOneMore && foundTags.size() == limit+1) {
			foundTags = foundTags.subList(0, foundTags.size()-1);
			moreToLoad = true;
		} else {
			moreToLoad = false;
		}
		
		tags.addAll(foundTags);
	}

	public boolean hasMoreResult() {
		return tagsSize > limit + 1;
	}
	
	public void resetSearchResults() {
		tags = new ArrayList<Tag>();
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public String getQuery() {
		return query;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getTagsSize() {
		return tagsSize;
	}

	public boolean isMoreToLoad() {
		return moreToLoad;
	}
	
}
