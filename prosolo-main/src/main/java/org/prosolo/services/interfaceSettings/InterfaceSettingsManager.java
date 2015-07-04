/**
 * 
 */
package org.prosolo.services.interfaceSettings;

import org.hibernate.Session;
import org.prosolo.domainmodel.interfacesettings.FilterType;
import org.prosolo.domainmodel.interfacesettings.UserSettings;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.general.AbstractManager;

/**
 * @author "Nikola Milikic"
 *
 */
public interface InterfaceSettingsManager extends AbstractManager {

	UserSettings getOrCreateUserSettings(User user);
	
	boolean changeActivityWallFilter(UserSettings userSettings, FilterType filter, long courseId);
	
	boolean changeGoalHints(UserSettings userSettings, boolean setGoalHints);

	boolean hideBubbleMessage(UserSettings userSettings, String messageBubble);
	
	boolean showAllBubbleMessages(UserSettings userSettings);

	UserSettings acceptTermsOfUse(UserSettings userSettings);

	UserSettings revokeTermsOfUse(UserSettings interfaceSettings);

	UserSettings tutorialsPlayed(long userId, String page, Session session);
	
}
