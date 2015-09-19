package org.prosolo.services.logging;

import java.util.Date;
import java.util.List;

import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.web.reports.LogRow;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
@author Zoran Jeremic Jan 28, 2014
 */

public interface LoggingDBManager {

	List<LogRow> loadLogsForPage(int skip, int limit, DBObject filterQuery);

	int getLogsCount(DBObject filterQuery);
	
	DBObject createFilterQuery(
			List<UserData> usersToInclude,
			boolean turnOfTwitter, List<String> selectedEventTypes, List<String> selectedObjectTypes,
			Date startDate, Date endDate);

	List<String> getAllDistinctValuesOfEventField(String fieldName);

	DBCursor getMostActiveUsersForDate(long date, int limit);

	long getMostActiveUsersLastActivityTimestamp(long userid);


}
