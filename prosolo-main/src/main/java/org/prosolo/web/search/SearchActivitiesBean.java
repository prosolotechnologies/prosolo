package org.prosolo.web.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;

import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.util.string.StringUtil;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.search.data.ActivityData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "searchActivitiesBean")
@Component("searchActivitiesBean")
@Scope("view")
public class SearchActivitiesBean implements Serializable {

	private static final long serialVersionUID = 7425398428736202443L;
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private TextSearch textSearch;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	
	private String query;
	private List<ActivityData> activities;
	private int activitiesSize;
	private int page = 0;
	private int limit = 7;
	private boolean moreToLoad;
	
	private long[] toExclude;
	
	public SearchActivitiesBean() {
		activities = new ArrayList<ActivityData>();
	}
	public void executeSearch(String toExcludeString, String context){
		this.toExclude = StringUtil.fromStringToLong(toExcludeString);
		this.limit = 3;
		searchActivities(this.query, false, context);
	}
	
	@Deprecated
	public void searchActivitiesListener(ValueChangeEvent event) {
		String toExcludeString = (String) ((UIInput) event.getSource()).getAttributes().get("toExclude");
		this.toExclude = StringUtil.fromStringToLong(toExcludeString);
		
		this.limit = 3;
		searchActivities(event.getNewValue().toString(), false, "searchActivitiesListener");
	}
	
	public void searchAllActivities() {
		searchActivities(query, true, "search.allActivities");
	}
	
	public void searchActivities(String searchQuery) {
		searchActivities(searchQuery, false, "search.activities");
	}
	
	public void searchActivities(String searchQuery, boolean loadOneMore, String context) {
		this.activities.clear();
		this.activitiesSize = 0;
		
		fetchActivities(searchQuery, this.toExclude, loadOneMore, true);
		
		if (searchQuery != null && searchQuery.length() > 0) {
			loggingNavigationBean.logServiceUse(
					ComponentName.SEARCH_ACTIVITIES, 
					"query", searchQuery,
					"context", context);
		}
	}
	
	public void loadMoreActivities() {
		page++;
		fetchActivities(query, null, true, false);
	}

	public void fetchActivities(String searchQuery, long[] compsToExclude, boolean loadOneMore, boolean viewed) {
		TextSearchResponse searchResponse = textSearch.searchActivities(
				searchQuery,
				this.page, 
				this.limit,
				loadOneMore,
				compsToExclude);
		
		@SuppressWarnings("unchecked")
		List<Activity> foundActivities = (List<Activity>) searchResponse.getFoundNodes();
		activitiesSize = (int) searchResponse.getHitsNumber();
		
		// if there is more than limit, set moreToLoad to true
		if (loadOneMore && foundActivities.size() == limit+1) {
			foundActivities = foundActivities.subList(0, foundActivities.size()-1);
			moreToLoad = true;
		} else {
			moreToLoad = false;
		}
		
		activities.addAll(convertToActivitiesData(foundActivities, viewed));
	}

	public boolean hasMoreActivities() {
		return activitiesSize > limit + 1;
	}
	
	/*
	 * Utility
	 */
	private List<ActivityData> convertToActivitiesData(List<Activity> activities, boolean viewed) {
		List<ActivityData> activitiesData = new ArrayList<ActivityData>();
		
		if (activities != null && !activities.isEmpty()) {
			for (Activity act : activities) {
				ActivityData actData = new ActivityData(act);
				actData.setViewed(viewed);
				activitiesData.add(actData);
			}
		}
		return activitiesData;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public String getQuery() {
		return query;
	}

	public List<ActivityData> getActivities() {
		return activities;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getActivitiesSize() {
		return activitiesSize;
	}

	public boolean isMoreToLoad() {
		return moreToLoad;
	}
	
}
