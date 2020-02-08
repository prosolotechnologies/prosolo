package org.prosolo.services.lti.impl;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.lti.LtiConsumer;
import org.prosolo.common.domainmodel.lti.LtiUser;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.LtiUserManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.UserCreationData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Optional;
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
	private LtiUserManager self;
	@Inject
	private EventFactory eventFactory;

	@Override
	// nt
	public Optional<String> getUserForLaunch(long ltiConsumerId, String userId, String name, String lastName, String email, long unitId, String roleName, long userGroupId, UserContextData context)
			throws DbConnectionException {
		Result<User> res = self.getUserForLaunchAndGetEvents(
				ltiConsumerId, userId, name, lastName, email, unitId, roleName, userGroupId, context);

		if (res.getResult() != null) {
			eventFactory.generateAndPublishEvents(res.getEventQueue());
			return Optional.ofNullable(res.getResult().getEmail());
		}
		return Optional.empty();
	}

	@Override
	@Transactional
	public Result<User> getUserForLaunchAndGetEvents(long ltiConsumerId, String userId, String name, String lastName, String email, long unitId, String roleName, long userGroupId, UserContextData context)
			throws DbConnectionException {
		try {
			Result<User> result = new Result<>();

			User user = getUser(ltiConsumerId, email);
			if (user == null) {
				long unitRoleId = roleManager.getRoleIdByName(roleName);
				Result<UserCreationData> userResult = userManager.createNewUserConnectToResourcesAndGetEvents(name, lastName, email, UUID.randomUUID().toString(), null, unitId, unitRoleId, userGroupId, context);

				user = userResult.getResult().getUser();
				result.appendEvents(userResult.getEventQueue());

				LtiConsumer consumer = (LtiConsumer) persistence.currentManager().load(LtiConsumer.class, ltiConsumerId);
				LtiUser ltiUser = new LtiUser();
				ltiUser.setUserId(userId);
				ltiUser.setConsumer(consumer);
				ltiUser.setUser(user);
				saveEntity(ltiUser);
			}
			result.setResult(user);

			return result;
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			throw e;
		}
	}

	private User getUser(long consumerId, String email) {
		String queryString =
				"SELECT user " +
				"FROM LtiUser ltiuser " +
				"INNER JOIN  ltiuser.user user " +
				"INNER JOIN ltiuser.consumer c " +
				"WHERE user.email = :email " +
				"AND c.id = :id";

		return (User) persistence.currentManager().createQuery(queryString)
				.setLong("id", consumerId)
				.setString("email", email.toLowerCase())
				.setMaxResults(1)
				.uniqueResult();
	}

}
