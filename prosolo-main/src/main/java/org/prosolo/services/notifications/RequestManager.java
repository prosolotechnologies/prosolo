package org.prosolo.services.notifications;

import java.util.List;

import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;

public interface RequestManager {
	
	Request requestToJoinTargetLearningGoal(long resourceId, long userId,
			long receiverId, String comment, String context) throws EventException, ResourceCouldNotBeLoadedException;

	Request createRequest(BaseEntity resource, long makerId, User sentTo,
			String comment, EventType requestType) throws EventException, ResourceCouldNotBeLoadedException;

	Request inviteToJoinResource(Node resource, long userId, long sentToId,
			String comment) throws EventException, ResourceCouldNotBeLoadedException;

	boolean existsRequestToJoinGoal(long userId, LearningGoal goal);

	List<User> getUsersWithUnansweredInvitationForGoal(long targetGoalId);
	
	Request getLatestEvaluationRequestSentToUser(BaseEntity resource, long userId);
	
	User getRequestMaker(long requestId);
}
