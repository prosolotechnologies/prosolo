package org.prosolo.services.notifications.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.activities.requests.AchievedCompetenceRequest;
import org.prosolo.domainmodel.activities.requests.ExternalCreditRequest;
import org.prosolo.domainmodel.activities.requests.NodeRequest;
import org.prosolo.domainmodel.activities.requests.Request;
import org.prosolo.domainmodel.activities.requests.RequestStatus;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.domainmodel.portfolio.ExternalCredit;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.notifications.RequestManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.notifications.RequestManager")
public class RequestManagerImpl extends AbstractManagerImpl implements RequestManager {
	
	private static final long serialVersionUID = -8596863587638486904L;

	@Autowired private EventFactory eventFactory;
	
	public Request requestToJoinTargetLearningGoal(long targetGoalId, User sentBy, long receiverId, String comment, String context) throws EventException, ResourceCouldNotBeLoadedException{
		TargetLearningGoal taregtGoal = loadResource(TargetLearningGoal.class, targetGoalId);
		User sentTo = loadResource(User.class, receiverId);
		
		Request request = createRequest(taregtGoal, sentBy, sentTo, comment, EventType.JOIN_GOAL_REQUEST);
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		parameters.put("targetGoalId", String.valueOf(targetGoalId));
		
		eventFactory.generateEvent(EventType.JOIN_GOAL_REQUEST, sentBy, request, parameters);
		return request;
	}

	@Override
	public Request inviteToJoinResource(Node resource, User sentBy, long receiverId, String comment) throws EventException, ResourceCouldNotBeLoadedException {
		User sentTo = loadResource(User.class, receiverId);

		return createRequest(resource, sentBy, sentTo, comment, EventType.JOIN_GOAL_INVITATION);
	}
	
	@Override
	@Transactional
	public Request createRequest(BaseEntity resource, User maker, User sentTo, String comment, EventType requestType) throws EventException{
		Request request = null;
		
		if (resource instanceof Node) {
			request = new NodeRequest();
		} else if (resource instanceof AchievedCompetence) {
			request = new AchievedCompetenceRequest();
		} else if (resource instanceof ExternalCredit) {
			request = new ExternalCreditRequest();
		}
		
		// check if there was a previous request for evaluation for this resource
		Request basedOn = getLatestEvaluationRequestSentToUser(resource, sentTo.getId());
		
		request.setDateCreated(new Date());
		request.setResource(resource);
		request.setMaker(maker);
		request.setSentTo(sentTo);
		request.setComment(comment);
		request.setRequestType(requestType);
		request.setStatus(RequestStatus.SENT);
		request.setBasedOn(basedOn);
		request = saveEntity(request);
//		this.flush();

 	 	 //eventFactory.generateEvent(requestType, maker, request);
		
		return request;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean existsRequestToJoinGoal(User user, LearningGoal goal) {
		String query = 
			"SELECT COUNT(request) " +
			"FROM Request request " +
			"WHERE request.maker = :user " +
				"AND request.nodeResource = :goal "+
				"AND (request.status = :statusSent OR request.status = :statusIgnored)";
		
		Long result = (Long) persistence.currentManager().createQuery(query).
			setEntity("user", user).
			setEntity("goal", goal).
			setString("statusSent", RequestStatus.SENT.name()).
			setString("statusIgnored", RequestStatus.IGNORED.name()).
			uniqueResult();
		
		return result > 0;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getUsersWithUnansweredInvitationForGoal(long targetGoalId) {
		String query = 
			"SELECT DISTINCT user " +
			"FROM Request request " +
			"LEFT JOIN request.sentTo user " +
			"WHERE request.nodeResource.id = :targetGoalId " +
				"AND (request.status = :statusSent OR request.status = :statusIgnored) " +
			"ORDER BY user.name, user.lastname";
		
		@SuppressWarnings("unchecked")
		List<User> result = persistence.currentManager().createQuery(query).
			setLong("targetGoalId", targetGoalId).
			setString("statusSent", RequestStatus.SENT.name()).
			setString("statusIgnored", RequestStatus.IGNORED.name()).
			list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return new ArrayList<User>();
	}
	
	@Override
	@Transactional (readOnly = true)
	public Request getLatestEvaluationRequestSentToUser(BaseEntity resource, long userId) {
		resource = HibernateUtil.initializeAndUnproxy(resource);
		String property = resource.getClass().getSimpleName();
		property = property.substring(0, 1).toLowerCase() + property.substring(1, property.length());
		
		String query = 
			"SELECT evSubmission.request " +
			"FROM EvaluationSubmission evSubmission " +
			"LEFT JOIN evSubmission.evaluations ev " +
			"LEFT JOIN ev."+property+" res " +
			"WHERE res.id = :resourceId " +
				"AND evSubmission.finalized = true " +
				"AND ev.maker.id = :makerId " +
			"ORDER BY evSubmission.dateSubmitted DESC";
		
		@SuppressWarnings("unchecked")
		List<Request> result = getPersistence().currentManager().createQuery(query).
			setLong("resourceId", resource.getId()).
			setLong("makerId", userId).
			setMaxResults(1).
			list();
		
		if (result != null && !result.isEmpty()) {
			return result.iterator().next();
		}
		return null;
	}

}
