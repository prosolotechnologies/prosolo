/**
 * 
 */
package org.prosolo.services.interfaceSettings;

import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.services.general.AbstractManager;

/**
 * @author "Nikola Milikic"
 *
 */
@Deprecated
public interface SocialActivityCacheUpdater extends AbstractManager {
	
	void updateSocialActivity(SocialActivity socialActivity, HttpSession userSession, Session session);

	void disableSharing(SocialActivity socialActivity, HttpSession userSession, Session currentManager);

	void removeSocialActivity(SocialActivity socialActivity, HttpSession httpSession, Session session);
	
}
