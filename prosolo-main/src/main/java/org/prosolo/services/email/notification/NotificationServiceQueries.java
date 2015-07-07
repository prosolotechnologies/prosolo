package org.prosolo.services.email.notification;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;

public class NotificationServiceQueries {

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(NotificationServiceQueries.class);

	/**
	 * Reference to OntModelQueryService.
	 */
	public static Collection<User> getUsersToNotify(String frequency) throws Exception {
		return null;
	}
}
