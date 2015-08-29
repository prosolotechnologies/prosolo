package org.prosolo.services.notifications;

import java.util.List;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;

public interface RequestManager {
	
	Request requestToJoinTargetLearningGoal(long resourceId, User maker,
			long receiverId, String comment, String context) throws EventException, ResourceCouldNotBeLoadedException;

	Request createRequest(BaseEntity resource, User maker, User sentTo,
			String comment, EventType requestType) throws EventException;

	Request inviteToJoinResource(Node resource, User maker, long sentToId,
			String comment) throws EventException, ResourceCouldNotBeLoadedException;

	boolean existsRequestToJoinGoal(User user, LearningGoal goal);

	List<User> getUsersWithUnansweredInvitationForGoal(long targetGoalId);
	
	Request getLatestEvaluationRequestSentToUser(BaseEntity resource, long userId);
}
