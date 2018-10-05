package org.prosolo.services.interaction;

import java.util.Map;

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;

/**
 * @author Zoran Jeremic
 * @deprecated since 0.7
 */
@Deprecated
public interface AnalyticalServiceCollector {

	void increaseUserActivityLog(long userid, long daysSinceEpoch);

	void createActivityInteractionData(long id, long id2);

	void increaseUserActivityForCredentialLog(long userid, long learningGoal,
			long daysSinceEpoch);

	void sendUpdateHashtagsMessage(Map<String, String> parameters, long goalId,
			long userId);

	void updateTwitterUser(long userId, long twitterUserId, boolean addUser);

	void increaseUserEventCount(EventType event, Map<String, String> params,
			long daysSinceEpoch);

	void increaseEventCount(long userId, EventType event, Map<String, String> params, long daysSinceEpoch);

	void updateInstanceLoggedUserCount(String ip, long timestamp, long count);

	void enableHashtag(String hashtag);

	void disableHashtag(String hashtag);

	void increaseSocialInteractionCount(long courseid, long source, long target);

    void storeNotificationData(String email,NotificationData notificationData);
}
