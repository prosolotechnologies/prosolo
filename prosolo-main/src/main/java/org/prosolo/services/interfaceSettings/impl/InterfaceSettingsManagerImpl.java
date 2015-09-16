/**
 * 
 */
package org.prosolo.services.interfaceSettings.impl;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.interfacesettings.ActivityWallSettings;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.interfacesettings.LocaleSettings;
import org.prosolo.common.domainmodel.interfacesettings.TermsOfUse;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.User;
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
	public UserSettings getOrCreateUserSettings(User user) {
		UserSettings result = getUserSettings(user.getId());
		
		if (result != null) {
			return result;
		} else {
			UserSettings userSettings = new UserSettings(user);
			userSettings.setLocaleSettings(saveEntity(new LocaleSettings("en", "US")));
			userSettings.setUser(user);
			this.persistence.save(userSettings);
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
	public boolean changeActivityWallFilter(UserSettings userSettings, FilterType filter, long courseId) {
		ActivityWallSettings awSettings = userSettings.getActivityWallSettings();
		awSettings.setChosenFilter(filter);
		awSettings.setCourseId(courseId);
		saveEntity(awSettings);
		return true;
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
	
}
