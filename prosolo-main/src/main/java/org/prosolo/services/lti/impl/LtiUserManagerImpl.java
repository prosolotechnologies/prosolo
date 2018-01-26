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
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.UUID;

@Service("org.prosolo.services.lti.LtiUserManager")
public class LtiUserManagerImpl extends AbstractManagerImpl implements LtiUserManager {

	private static final long serialVersionUID = -5866975520782311614L;

	private static Logger logger = Logger.getLogger(LtiUserManager.class);

	@Inject
	private UserManager userManager;
	@Inject
	private RoleManager roleManager;
	@Inject
	private UnitManager unitManager;
	@Inject
	private UserGroupManager userGroupManager;

	@Override
	@Transactional
	public User getUserForLaunch(long consumerId, String userId, String name, String lastName, String email, long organizationId, long unitId, long userGroupId)
			throws DbConnectionException {
		try {
			User user = getUser(consumerId, userId);
			if (user == null) {
				LtiConsumer consumer = (LtiConsumer) persistence.currentManager().load(LtiConsumer.class, consumerId);
				LtiUser ltiUser = new LtiUser();
				ltiUser.setUserId(userId);
				ltiUser.setConsumer(consumer);
				user = createOrGetExistingUser(name, lastName, email, organizationId, unitId, userGroupId);
				ltiUser.setUser(user);
				saveEntity(ltiUser);
			}
			return user;
		} catch (Exception e) {
			 throw new DbConnectionException("Error while logging user in");

		}
	}

	private User createOrGetExistingUser(String name, String lastName, String email, long organizationId, long unitId, long userGroupId) throws DbConnectionException {
		try {
			User user;
			try {
				long roleId = roleManager.getRoleIdByName(SystemRoleNames.USER);
				user = userManager.createNewUser(organizationId, name, lastName, email, true, UUID.randomUUID().toString(), null, null, null, Arrays.asList(roleId));

				if (unitId > 0) {
					unitManager.addUserToUnitWithRole(user.getId(), unitId, roleId, UserContextData.empty());

					if (userGroupId > 0) {
						userGroupManager.addUserToTheGroup(userGroupId, user.getId(), UserContextData.empty());
					}
				}
			} catch (UserAlreadyRegisteredException e) {
				user = userManager.getUser(email);
			}
			return user;
		} catch (Exception e) {
			throw new DbConnectionException("User cannot be retrieved");
		}
	}

	private User getUser(long consumerId, String userId) {
		try {
			String queryString =
				"SELECT user " +
				"FROM LtiUser ltiuser " +
				"INNER JOIN  ltiuser.user user " +
				"INNER JOIN ltiuser.consumer c " +
				"WHERE ltiuser.userId = :userId " +
					"AND c.id = :id";
	
			return (User) persistence.currentManager().createQuery(queryString)
				.setLong("id", consumerId)
				.setString("userId", userId);
		} catch (Exception e) {
			//throw new DbConnectionException("User cannot be retrieved");
			return null;
		}
	}

}
