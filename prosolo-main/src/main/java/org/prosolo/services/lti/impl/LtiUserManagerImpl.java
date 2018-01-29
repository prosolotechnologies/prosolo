package org.prosolo.services.lti.impl;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.lti.LtiConsumer;
import org.prosolo.common.domainmodel.lti.LtiUser;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.LtiUserManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.UUID;

@Service("org.prosolo.services.lti.LtiUserManager")
public class LtiUserManagerImpl extends AbstractManagerImpl implements LtiUserManager {

	private static final long serialVersionUID = -5866975520782311614L;

	private static Logger logger = Logger.getLogger(LtiUserManager.class);

	@Inject
	private UserManager userManager;
	@Inject
	private RoleManager roleManager;

	@Override
	@Transactional
	public User getUserForLaunch(long ltiConsumerId, String userId, String name, String lastName, String email, long unitId, String roleName, long userGroupId, UserContextData context)
			throws DbConnectionException {
		try {
			/**
			 * we must use get() instead of load() here, since load will yield an exception if there is no user,
			 * where get() returns null in such case
			 */
			User user = (User) persistence.currentManager().get(User.class, userId);

			if (user == null) {
				long unitRoleId = roleManager.getRoleIdByName(roleName);
				user = userManager.createNewUserAndConnectToResources(name, lastName, email, UUID.randomUUID().toString(), null, unitId, unitRoleId, userGroupId, context);

				LtiConsumer consumer = (LtiConsumer) persistence.currentManager().load(LtiConsumer.class, ltiConsumerId);
				LtiUser ltiUser = new LtiUser();
				ltiUser.setUserId(userId);
				ltiUser.setConsumer(consumer);
				ltiUser.setUser(user);
				saveEntity(ltiUser);
			}
			return user;
		} catch (Exception e) {
			 throw new DbConnectionException("Error while logging user in");
		}
	}

}
