package org.prosolo.web.reports;

import java.util.List;

import org.prosolo.web.activitywall.data.UserData;

/**
 * @author Zoran Jeremic Feb 23, 2014
 */

@SuppressWarnings("hiding")
public interface LazyLogsDataModel<LogRow> {
	
	void loadFilteredData(List<UserData> usersToInclude, boolean turnOfTwitterPosts);
	
}
