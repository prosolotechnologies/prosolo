package org.prosolo.services.notifications;

import java.util.List;

import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.activities.requests.Request;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.event.EventException;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;

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
