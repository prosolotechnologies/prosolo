package org.prosolo.services.logging;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityreport.LoggedEvent;
import org.prosolo.services.event.context.LearningContext;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.logging.exception.LoggingException;

import com.mongodb.DBObject;

/**
 * @author Zoran Jeremic 2013-10-07
 *
 */

public interface LoggingService {

	void logEventObserved(EventType eventType, User actor, String objectType,
			long objectId, String objectTitle, String targetType,
			long targetId, String reasonType, long reasonId,
			Map<String, String> parameters, String ipAddress) throws LoggingException;

	void logServiceUse(User user, String componentName, String parametersJson,
			String ipAddress) throws LoggingException;

	void logServiceUse(User user, ComponentName component, String link,
			Map<String, String> parameters, String ipAddress) throws LoggingException;

	void logNavigation(User user, String link, String ipAddress) throws LoggingException;

	void logNavigationFromContext(User user, String link,
			String context, String page, String learningContext, String service, 
			String parametersString, String ipAddress) throws LoggingException;
	
	void logEmailNavigation(User user, String link,
			String parametersString, String ipAddress,
			LearningContextData lContext) throws LoggingException;

	void logTabNavigationFromContext(User user, String tabName, String context,
			String parameters, String ipAddress) throws LoggingException;

	void logEvent(EventType eventType, User actor, String ipAddress);

	void increaseUserActivityLog(long userid, long date);

	void recordUserActivity(long userid, long time) throws LoggingException;

	void logEventObserved(EventType eventType, User actor, String objectType, long objectId,
			Map<String, String> parameters, String ipAddress);

	void recordActivityReportGenerated(List<Long> userIds,
			Date reportGenerationDate);

	List<Date> getReportDays(Date start, Date end, Long userId);

	Map<Long, Collection<LoggedEvent>> getAllLoggedEvents(Date start, Date end);

	Map<Long, Collection<LoggedEvent>> getAllLoggedEvents(DBObject filterQuery);

	boolean collectionExists(String collectionName);

	boolean reportDatesCollectionExists();

	Long getOldestObservedEventTime();

	Collection<LoggedEvent> getLoggedEventsList(DBObject filterQuery);
	
	void logSessionEnded(EventType eventType, User actor, String ipAddress);

	void logEventObserved(EventType eventType, User actor,
			String objectType, long objectId, String objectTitle,
			String targetType, long targetId, String reasonType, long reasonId,
			Map<String, String> parameters, String ipAddress, LearningContext learningContext) throws LoggingException;

}