/**
 * 
 */
package org.prosolo.services.interfaceSettings;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;

/**
 * @author "Nikola Milikic"
 *
 */
public interface InterfaceSettingsManager extends AbstractManager {

	UserSettings getOrCreateUserSettings(long userId) throws ResourceCouldNotBeLoadedException;
	
	UserSettings getOrCreateUserSettings(long userId, Session session) throws ResourceCouldNotBeLoadedException;

	/**
	 * Updates chosen status wall filter for a user and returns flag that indicates whether updated is
	 * successful
	 *
	 * @param userId
	 * @param filter
	 * @param courseId
	 * @return
	 * @throws DbConnectionException
	 */
	boolean changeActivityWallFilter(long userId, FilterType filter, long courseId);
	
	UserSettings acceptTermsOfUse(UserSettings userSettings);

	UserSettings revokeTermsOfUse(UserSettings interfaceSettings);

	UserSettings tutorialsPlayed(long userId, String page, Session session);

	/**
	 * Returns chosen status wall filter from user settings
	 *
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	FilterType getChosenFilter(long userId);
	
}
