/**
 * 
 */
package org.prosolo.services.notifications;

import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;

/**
 * @author "Nikola Milikic"
 *
 */
public interface EvaluationUpdater extends AbstractManager {

	void updateEvaluationData(long evSubmissionId, HttpSession userSession,
			Session session) throws ResourceCouldNotBeLoadedException;
	
}
