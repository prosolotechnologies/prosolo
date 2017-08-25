package org.prosolo.services.logging;

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.context.LearningContext;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.logging.exception.LoggingException;

import java.util.Map;

//import com.mongodb.DBObject;

/**
 * @author Zoran Jeremic 2013-10-07
 *
 */

public interface LoggingService {

	void logServiceUse(UserContextData context, String componentName,
					   String parametersJson, String ipAddress) throws LoggingException;

	void logNavigation(UserContextData context, String link, String ipAddress) throws LoggingException;

	void logNavigationFromContext(UserContextData context, String link,
								  String ctx, String parametersString, String ipAddress)
			throws LoggingException;
	
	void logEmailNavigation(UserContextData context, String link,
							String parametersString, String ipAddress) throws LoggingException;

	void logTabNavigationFromContext(UserContextData userContext, String tabName,
									 String context, String parametersString, String ipAddress) throws LoggingException;

	void logEvent(EventType eventType, UserContextData context, String ipAddress);

	void increaseUserActivityLog(long userid, long date);

	void recordUserActivity(long userid, long time) throws LoggingException;

	void logEventObservedAsync(EventType eventType, UserContextData context, String objectType, long objectId,
							   Map<String, String> parameters, String ipAddress);

	void logEventObserved(EventType eventType, long actorId, long organizationId, String sessionId,
			String objectType, long objectId, String objectTitle, String targetType, long targetId,
			Map<String, String> parameters, String ipAddress, LearningContext learningContext) throws LoggingException;
	
	void logServiceUse(UserContextData context, ComponentName component, String link, Map<String, String> params,
			String ipAddress) throws LoggingException;

	void logEventObserved(EventType eventType, UserContextData context, String objectType, long objectId, String objectTitle,
						  String targetType, Map<String, String> params, String ipAddress) throws LoggingException;

}