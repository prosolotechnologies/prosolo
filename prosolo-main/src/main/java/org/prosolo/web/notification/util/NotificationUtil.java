/**
 * 
 */
package org.prosolo.web.notification.util;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.notifications.NotificationAction;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;

/**
 * @author "Nikola Milikic"
 *
 */
public class NotificationUtil {
	
	private static Logger logger = Logger.getLogger(NotificationUtil.class);

	public static String getNotificationActionName(NotificationAction action, Locale locale) {
		if (action == null)
			return null;
		
		try {
			return ResourceBundleUtil.getMessage(
					"action.name."+action.toString(), 
					locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error("Could not find localized message for notification "+action+". " + e.getMessage());
			return "action";
		}
	}
	
	public static String getNotificationChosenActionName(NotificationAction action, Locale locale) {
		if (action == null)
			return null;
		
		try {
			return ResourceBundleUtil.getMessage(
					"action.chosen.name."+action.toString(), 
					locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error("Could not find localized message for notification "+action+". " + e.getMessage());
			return "action";
		}
	}
}
