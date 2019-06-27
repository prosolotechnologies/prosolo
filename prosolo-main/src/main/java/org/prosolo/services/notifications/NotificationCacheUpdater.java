/**
 * 
 */
package org.prosolo.services.notifications;

import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;

import javax.servlet.http.HttpSession;

/**
 * @author "Nikola Milikic"
 *
 */
public interface NotificationCacheUpdater {

	/**
	 * @param notificationId
	 * @param userSession
	 * @throws ResourceCouldNotBeLoadedException
	 *
	 * @version 0.5
	 */
	void updateNotificationData(long notificationId, HttpSession userSession) throws ResourceCouldNotBeLoadedException;
	
}
