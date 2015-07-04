package org.prosolo.web.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.activitywall.SocialStreamSubViewType;
import org.prosolo.search.TextSearch;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.impl.ActivityWallManagerImpl.ArrayCount;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.ActivityWallBean;
import org.prosolo.web.activitywall.util.WallActivityConverter;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "searchHashtagsBean")
@Component("searchHashtagsBean")
@Scope("view")
public class SearchHashtagsBean implements Serializable {

	private static final long serialVersionUID = 7425398428736202443L;
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private TextSearch textSearch;
	@Autowired private WallActivityConverter wallActivityConverter;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	@Autowired private ActivityWallBean activityWall;
	@Autowired private ActivityWallManager activityWallManager;
	
	private String query;
	private List<SocialActivityData> activityWallItems;

	private int itemsSize;
	private int page = 0;
	private int navPage = 0;
	private int limit = 7;
	private boolean moreToLoad;
	
	public SearchHashtagsBean() {
		activityWallItems = new ArrayList<SocialActivityData>();
	}
	
	public void search() {
		if (this.query == null) {
			setQuery("");
		}
		searchHashtags(query, true, "search.allHashtags");
	}
	
	public void searchHashtags(String searchQuery) {
		searchHashtags(searchQuery, false, "search.hashtags");
	}
	
	public void searchHashtags(String searchQuery, 
			boolean loadOneMore,
			String context) {
		
		this.activityWallItems.clear();
		this.itemsSize = 0;
		
 		fetchHashtags(searchQuery != null ? searchQuery : "", this.limit, loadOneMore);
		
 		if (searchQuery != null && searchQuery.length() > 0) {
			loggingNavigationBean.logServiceUse(
					ComponentName.SEARCH_HASHTAGS, 
					"query", searchQuery,
					"context", context);
 		}
	}
	
	
	public void loadMoreActivityWallItems() {
		page++;
		fetchHashtags(query, this.limit, true);
	}

	public void fetchHashtags(String searchQuery, int limit, boolean loadOneMore) {
		
		ArrayCount result = activityWallManager.searchPublicSocialActivitiesWithHashtag(
				searchQuery,
				this.page, 
				limit,
				loadOneMore,
				true,
				false);
		
		@SuppressWarnings("unchecked")
		List<SocialActivity> foundActivityWallItems = (List<SocialActivity>) result.getArray();
		itemsSize = (int) result.getCount();
		
		// if there is more than limit, set moreToLoad to true
		if (loadOneMore && foundActivityWallItems.size() == limit+1) {
			foundActivityWallItems = foundActivityWallItems.subList(0, foundActivityWallItems.size()-1);
			moreToLoad = true;
		} else {
			moreToLoad = false;
		}
		
		activityWallItems.addAll(wallActivityConverter.convertSocialActivities(
				foundActivityWallItems, 
				loggedUser.getUser(),
				SocialStreamSubViewType.STATUS_WALL,
				loggedUser.getLocale()));
		
		itemsSize = activityWallItems.size();
	}

	public boolean hasMoreCompetences() {
		return itemsSize > (navPage + 1) * limit;
	}

	public boolean hasPrevCompetences() {
		if (this.navPage > 0) {
			return true;
		} else
			return false;
	}
	
	public void refresh() {
		
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}

	public List<SocialActivityData> getActivityWallItems() {
		return activityWallItems;
	}

	public boolean isMoreToLoad() {
		return moreToLoad;
	}

}
