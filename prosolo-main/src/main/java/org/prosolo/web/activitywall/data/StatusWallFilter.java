package org.prosolo.web.activitywall.data;

import org.prosolo.services.activityWall.filters.AllFilter;
import org.prosolo.services.activityWall.filters.AllProsoloFilter;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.filters.MyActivitiesFilter;
import org.prosolo.services.activityWall.filters.MyNetworkFilter;
import org.prosolo.services.activityWall.filters.TwitterFilter;

public enum StatusWallFilter {

	ALL(new AllFilter(), "All"),
	ALL_PROSOLO(new AllProsoloFilter(), "Prosolo posts"),
	MY_ACTIVITIES(new MyActivitiesFilter(), "My posts"),
	MY_NETWORK(new MyNetworkFilter(), "My network"),
	TWITTER(new TwitterFilter(), "Twitter posts");
	
	private Filter filter;
	private String label;
	
	private StatusWallFilter(Filter filter, String label) {
		this.filter = filter;
		this.label = label;
	}
	
	public Filter getFilter() {
		return filter;
	}
	
	public String getLabel() {
		return label;
	}
}
