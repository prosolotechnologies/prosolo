package org.prosolo.services.logging;

import java.util.Map;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.services.event.context.LearningContext;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.logging.exception.LoggingException;

//import com.mongodb.DBObject;

/**
 * @author Zoran Jeremic 2013-10-07
 *
 */

public interface LoggingService {

	void logServiceUse(long userId, String componentName, String parametersJson,
			String ipAddress) throws LoggingException;

	void logServiceUse(long userId, ComponentName component, String link,
			Map<String, String> parameters, String ipAddress) throws LoggingException;

	void logNavigation(long actorId, String link, String ipAddress) throws LoggingException;

	void logNavigationFromContext(long userId, String link,
			String context, String page, String learningContext, String service, 
			String parametersString, String ipAddress) throws LoggingException;
	
	void logEmailNavigation(long actorId, String link,
			String parametersString, String ipAddress,
			LearningContextData lContext) throws LoggingException;

	void logTabNavigationFromContext(long userId, String tabName, String context,
			String parameters, String ipAddress) throws LoggingException;

	void logEvent(EventType eventType, long actorId, String ipAddress);

	void increaseUserActivityLog(long userid, long date);

	void recordUserActivity(long userid, long time) throws LoggingException;

	void logEventObserved(EventType eventType, long actorId, String objectType, long objectId,
			Map<String, String> parameters, String ipAddress);

	void logSessionEnded(EventType eventType, long actorId, String ipAddress);

	void logEventObserved(EventType eventType, long actorId,
			String objectType, long objectId, String objectTitle,
			String targetType, long targetId,
			Map<String, String> parameters, String ipAddress, LearningContext learningContext) throws LoggingException;
	
	void logServiceUse(long userId, ComponentName component, Map<String, String> params, 
			String ipAddress, LearningContextData context) throws LoggingException;

	void logEventObserved(EventType eventType, long actorId, String objectType, long objectId,
			String objectTitle,	String targetType, long targetId,
			Map<String, String> parameters, String ipAddress) throws LoggingException;

}