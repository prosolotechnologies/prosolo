/**
 * 
 */
package org.prosolo.services.notifications;

import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;

/**
 * @author "Nikola Milikic"
 *
 */
public interface NotificationCacheUpdater {

	/**
	 * @param notificationId
	 * @param userSession
	 * @param session
	 * @throws ResourceCouldNotBeLoadedException
	 *
	 * @version 0.5
	 */
	void updateNotificationData(long notificationId, HttpSession userSession, Session session) throws ResourceCouldNotBeLoadedException;
	
}
