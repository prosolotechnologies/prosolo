/**
 * 
 */
package org.prosolo.services.interfaceSettings.impl;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.interfacesettings.ActivityWallSettings;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.interfacesettings.LocaleSettings;
import org.prosolo.common.domainmodel.interfacesettings.TermsOfUse;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.interfaceSettings.InterfaceSettingsManager")
public class InterfaceSettingsManagerImpl extends AbstractManagerImpl implements InterfaceSettingsManager, Serializable {
	
	private static final long serialVersionUID = -843060822986598453L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(InterfaceSettingsManager.class);
	
	@Override
	@Transactional
	public UserSettings getOrCreateUserSettings(long userId) throws ResourceCouldNotBeLoadedException {
		return getOrCreateUserSettings(userId, persistence.currentManager());
	}
	
	@Override
	@Transactional
	public UserSettings getOrCreateUserSettings(long userId, Session session) throws ResourceCouldNotBeLoadedException {
		UserSettings result = getUserSettings(userId);
		
		if (result != null) {
			return result;
		} else {
			User user = loadResource(User.class, userId);
			
			UserSettings userSettings = new UserSettings(user);
			LocaleSettings ls = new LocaleSettings("en", "US"); 
			session.saveOrUpdate(ls);
			userSettings.setLocaleSettings(ls);
			userSettings.setUser(user);
			session.saveOrUpdate(userSettings);
			session.flush();
			
			return userSettings;
		}
	}


	private UserSettings getUserSettings(long userId) {
		return getUserSettings(userId, persistence.currentManager());
	}
	
	@Transactional
	private UserSettings getUserSettings(long userId, Session session) {
		String query = 
			"SELECT settings " +
			"FROM UserSettings settings " +
			"LEFT JOIN settings.user user " +
			"WHERE user.id = :userId";
		
		UserSettings result = (UserSettings) session.createQuery(query).
			setLong("userId", userId).
			uniqueResult();
		return result;
	}
	
	@Override
	@Transactional
	public boolean changeActivityWallFilter(long userId, FilterType filter, long courseId) {
		try {
			UserSettings userSettings = getOrCreateUserSettings(userId);
			ActivityWallSettings awSettings = userSettings.getActivityWallSettings();
			awSettings.setChosenFilter(filter);
			awSettings.setCourseId(courseId);
			saveEntity(awSettings);
			return true;
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error updating the chosen status wall filter");
		}
	}
	
	@Override
	@Transactional
	public UserSettings acceptTermsOfUse(UserSettings userSettings) {
		TermsOfUse termsOfUse = userSettings.getTermsOfUse();
		
		if (termsOfUse == null) {
			termsOfUse = new TermsOfUse();
		}
		termsOfUse.setAccepted(true);
		termsOfUse.setDate(new Date());
		termsOfUse = saveEntity(termsOfUse);
		
		userSettings.setTermsOfUse(termsOfUse);
		return saveEntity(userSettings);
	}
	
	@Override
	@Transactional (readOnly = false)
	public UserSettings revokeTermsOfUse(UserSettings userSettings) {
		TermsOfUse termsOfUse = userSettings.getTermsOfUse();
		termsOfUse.setAccepted(false);
		termsOfUse = saveEntity(termsOfUse);
		
		userSettings.setTermsOfUse(termsOfUse);
		return saveEntity(userSettings);
	}
	
	@Override
	@Transactional (readOnly = false)
	public UserSettings tutorialsPlayed(long userId, String page, Session session) {
		UserSettings userSettings = getUserSettings(userId, session);
		userSettings.getPagesTutorialPlayed().add(page);
		session.saveOrUpdate(userSettings);
		return userSettings;
	}

	@Override
	@Transactional
	public FilterType getChosenFilter(long userId) {
		try {
			UserSettings userSettings = getOrCreateUserSettings(userId);
			return userSettings.getActivityWallSettings().getChosenFilter();
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading chosen status wall filter type");
		}
	}
	
}
