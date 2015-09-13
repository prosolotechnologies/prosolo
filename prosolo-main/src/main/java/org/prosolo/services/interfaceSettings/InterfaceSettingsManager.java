/**
 * 
 */
package org.prosolo.services.interfaceSettings;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.general.AbstractManager;

/**
 * @author "Nikola Milikic"
 *
 */
public interface InterfaceSettingsManager extends AbstractManager {

	UserSettings getOrCreateUserSettings(User user);
	
	boolean changeActivityWallFilter(UserSettings userSettings, FilterType filter, long courseId);
	
	UserSettings acceptTermsOfUse(UserSettings userSettings);

	UserSettings revokeTermsOfUse(UserSettings interfaceSettings);

	UserSettings tutorialsPlayed(long userId, String page, Session session);
	
}
