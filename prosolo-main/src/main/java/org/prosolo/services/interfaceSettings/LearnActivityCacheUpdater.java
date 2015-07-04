/**
 * 
 */
package org.prosolo.services.interfaceSettings;

import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.domainmodel.outcomes.Outcome;
import org.prosolo.services.general.AbstractManager;

/**
 * @author "Nikola Milikic"
 *
 */
public interface LearnActivityCacheUpdater extends AbstractManager {
	
	boolean updateActivityOutcome(long targetActivityId, Outcome outcome, HttpSession userSession, Session session);
	
}
