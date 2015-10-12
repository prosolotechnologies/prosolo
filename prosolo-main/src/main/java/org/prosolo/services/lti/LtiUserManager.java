package org.prosolo.services.lti;

import org.prosolo.common.domainmodel.user.User;

public interface LtiUserManager {

	public User getUserForLaunch(long consumerId, String userId, String name, String lastName, String email, long courseId) throws RuntimeException;

}