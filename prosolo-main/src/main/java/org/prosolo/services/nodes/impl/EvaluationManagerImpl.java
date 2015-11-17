/**
 * 
 */
package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.activities.requests.RequestStatus;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.portfolio.CompletedResource;
import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.workflow.evaluation.AchievedCompetenceEvaluation;
import org.prosolo.common.domainmodel.workflow.evaluation.Badge;
import org.prosolo.common.domainmodel.workflow.evaluation.Evaluation;
import org.prosolo.common.domainmodel.workflow.evaluation.EvaluationSubmission;
import org.prosolo.common.domainmodel.workflow.evaluation.ExternalCreditEvaluation;
import org.prosolo.common.domainmodel.workflow.evaluation.TargetCompetenceEvaluation;
import org.prosolo.common.domainmodel.workflow.evaluation.TargetLearningGoalEvaluation;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.services.nodes.exceptions.EvaluationNotSupportedException;
import org.prosolo.services.nodes.exceptions.InvalidParameterException;
import org.prosolo.services.notifications.RequestManager;
import org.prosolo.web.communications.evaluation.data.EvaluatedResourceData;
import org.prosolo.web.communications.evaluation.data.EvaluationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.EvaluationManager")
public class EvaluationManagerImpl extends AbstractManagerImpl implements EvaluationManager {
	
	private static final long serialVersionUID = -5712641119364871272L;
	
	private static Logger logger = Logger.getLogger(EvaluationManager.class);
	
	@Autowired private RequestManager requestManager;
	@Autowired private PortfolioManager portfolioManager;

	@Override
	@Transactional (readOnly = false)
	public Collection<Request> sendEvaluationRequests(User maker, Collection<Long> evaluatorIds, BaseEntity resource, String text) throws EventException{
		if (resource != null && evaluatorIds != null && !evaluatorIds.isEmpty()) {
			
			Collection<Request> requests = new ArrayList<Request>();
			
			for (Long evaluatorId : evaluatorIds) {
				try {
					User evaluator = loadResource(User.class, evaluatorId);
					Request request = requestManager.createRequest(resource, maker, evaluator, text, EventType.EVALUATION_REQUEST);
					requests.add(request);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
			return requests;
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public Request resubmitEvaluationRequest(User maker, EvaluationSubmission previousSubmission, String text) throws EventException{
		User evaluator = merge(previousSubmission.getMaker());
		Request request = previousSubmission.getRequest();
		
		return requestManager.createRequest(
				request.getResource(), 
				maker, 
				evaluator, 
				text, 
				EventType.EVALUATION_REQUEST);
	}
	
	@Override
	@Transactional (readOnly = false)
	public EvaluationSubmission createEvaluationSubmissionDraft(User maker,
			Request evaluationRequest) throws InvalidParameterException, EventException, EvaluationNotSupportedException, ResourceCouldNotBeLoadedException {
		
		if (maker != null && evaluationRequest != null) {
			// check whether this is evaluation resubmission
			EvaluationSubmission previousSubmission = null;
			
			if (evaluationRequest.getBasedOn() != null) {
				previousSubmission = getEvaluationSubmission(evaluationRequest.getBasedOn().getId());
			}
			
			Date currentTime = new Date();
			
			BaseEntity resource = evaluationRequest.getResource();

			EvaluationSubmission evaluationSubmission = new EvaluationSubmission();
			evaluationSubmission.setMaker(maker);
			evaluationSubmission.setDateCreated(currentTime);
			evaluationSubmission.setMessage("");
			evaluationSubmission.setRequest(evaluationRequest);
			evaluationSubmission.setBasedOn(previousSubmission);
			evaluationSubmission = saveEntity(evaluationSubmission);
			
			if (previousSubmission != null) {
				previousSubmission.setBasedOnChild(evaluationSubmission);
				previousSubmission = saveEntity(previousSubmission);
			}
			
			// resource the evaluation has been requested for is marked as primary evaluation
			Evaluation primaryEvaluation = creteEvaluation(maker, currentTime, evaluationSubmission, resource);
			primaryEvaluation.setPrimaryEvaluation(true);
			primaryEvaluation = saveEntity(primaryEvaluation);
			
			evaluationSubmission.setPrimaryEvaluation(primaryEvaluation);
			evaluationSubmission.addEvaluation(primaryEvaluation);
			
			if (resource instanceof TargetLearningGoal) {
				Collection<TargetCompetence> targetCompetences = ((TargetLearningGoal) resource).getTargetCompetences();
				
				for (TargetCompetence tComp : targetCompetences) {
					Evaluation ev = creteEvaluation(maker, currentTime, evaluationSubmission, tComp);

					evaluationSubmission.addEvaluation(ev);
				}
			} if (resource instanceof ExternalCredit) {
				Collection<AchievedCompetence> competences = ((ExternalCredit) resource).getCompetences();
				
				for (AchievedCompetence achievedCompetence : competences) {
					Evaluation ev1 = creteEvaluation(maker, currentTime, evaluationSubmission, achievedCompetence);
					
					evaluationSubmission.addEvaluation(ev1);
				}
			}
			
			return saveEntity(evaluationSubmission);
		} else
			throw new InvalidParameterException("Several of parameters passed have null value: " +
					"maker ["+maker+"], evaluationRequest ["+evaluationRequest+"] ");
	}

	private Evaluation creteEvaluation(User maker, Date currentTime, EvaluationSubmission evaluationSubmission, BaseEntity resource)
			throws EvaluationNotSupportedException, ResourceCouldNotBeLoadedException {
		
		Evaluation ev = instantiateEvaluation(resource);
		ev.setDateCreated(currentTime);
		ev.setMaker(maker);
		ev.setResource(resource);
		ev.setEvaluationSubmission(evaluationSubmission);
		
		EvaluationSubmission previousSubmission = evaluationSubmission.getBasedOn();
		
		// if it is evaluation resubmission, find previous evaluation
		if (previousSubmission != null) {
			Evaluation previousEvaluation = findEvaluationForResource(previousSubmission.getEvaluations(), resource);
			
			if (previousEvaluation != null && previousEvaluation.isAccepted()) {
				ev.setText(previousEvaluation.getText());
				ev.setAccepted(true);
				ev.setReadOnly(true);

				if (previousEvaluation.getBadges() != null && !previousEvaluation.getBadges().isEmpty()) {
					Set<Badge> badges = new HashSet<Badge>();
					
					for (Badge badge : previousEvaluation.getBadges()) {
						badges.add(loadResource(Badge.class, badge.getId()));
					}
					ev.setBadges(badges);
				}
			}
		}
		
		return saveEntity(ev);
	}
	
	private Evaluation findEvaluationForResource(Collection<Evaluation> evaluations, BaseEntity resource) {
		if (resource != null && evaluations != null && !evaluations.isEmpty()) {
			for (Evaluation evaluation : evaluations) {
				if (evaluation.getResource().getId() == resource.getId()) {
					return evaluation;
				}
			}
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public EvaluationSubmission finalizeEvaluationSubmission(
			EvaluationSubmission evaluationSubmission,
			List<EvaluatedResourceData> evaluatedResources,
			EvaluatedResourceData primeEvaluatedResource) throws InvalidParameterException, EventException, EvaluationNotSupportedException {
		
		evaluationSubmission = editEvaluationSubmissionDraft(evaluationSubmission, evaluatedResources, primeEvaluatedResource.getComment());

		evaluationSubmission.setFinalized(true);
		evaluationSubmission.setDateSubmitted(new Date());
		evaluationSubmission.setAccepted(primeEvaluatedResource.isAccepted());
		evaluationSubmission = saveEntity(evaluationSubmission);
		
		return evaluationSubmission;
	}
	
	@Override
	@Transactional (readOnly = false)
	public EvaluationSubmission editEvaluationSubmissionDraft(
			EvaluationSubmission evaluationSubmission,
			List<EvaluatedResourceData> evaluatedResources,
			String evaluationMessage) throws EventException, InvalidParameterException, EvaluationNotSupportedException {

		if (evaluationSubmission != null && evaluatedResources != null) {
			evaluationSubmission = merge(evaluationSubmission);
			
			Set<Evaluation> currentEvaluations = evaluationSubmission.getEvaluations();
			Set<Evaluation> evaluationsDeleted = new HashSet<Evaluation>(currentEvaluations);
			
			Date currentTime = new Date();
			boolean submissionChanged = false;
			
			outer: for (EvaluatedResourceData evaluatedResData : evaluatedResources) {
				
				// find evaluation for specific evaluatedResource
				for (Evaluation ev : currentEvaluations) {
					BaseEntity evaluatedRes = HibernateUtil.initializeAndUnproxy(ev.getResource());
					
					// if evaluation has been found for specific evaluatedResData, then update if needed
					if ((evaluatedRes instanceof AchievedCompetence &&
						((AchievedCompetence) evaluatedRes).getCompetence().getId() == evaluatedResData.getId())
						||
						(evaluatedRes.getId() == evaluatedResData.getId())) {
						
						boolean evaluationChanged = false;
						
						if (ev.isAccepted() != evaluatedResData.isAccepted()) {
							ev.setAccepted(evaluatedResData.isAccepted());
							evaluationChanged = true;
						}
						
						if (ev.getText() != null) {
							if (!ev.getText().equals(evaluatedResData.getComment())) {
								ev.setText(evaluatedResData.getComment());
								evaluationChanged = true;
							}
						} else {
							ev.setText(evaluatedResData.getComment());
							evaluationChanged = true;
						}
						
						
						// check if badges has been changed for this evaluatedResData
						List<Badge> selectedBadges = evaluatedResData.getChosenBadges();
						
						if (!hasSameBadges(selectedBadges, ev.getBadges())) {
							ev.getBadges().clear();
							ev.getBadges().addAll(selectedBadges);
							evaluationChanged = true;
						}
						
					 	
						if (evaluationChanged) {
							ev = saveEntity(ev);
//							eventFactory.generateEvent(EventType.EVALUATION_EDITED, user, ev);
						}
						
						Iterator<Evaluation> iterator = evaluationsDeleted.iterator();
						
						evaluationsDeletedLoop: while (iterator.hasNext()) {
							Evaluation evaluation = (Evaluation) iterator.next();
							
							if (evaluation.getId() == ev.getId()) {
								iterator.remove();
								break evaluationsDeletedLoop;
							}
						}
						
						continue outer;
					}
				}
				
				// if reached here, that means that there is no Evaluation instance for this specific evaluatedResData
				// this is probably because this is added Competence
				CompletedResource completedRes = portfolioManager.getCompletedResource(evaluationSubmission.getRequest().getMaker(), evaluatedResData.getResource());
				
				BaseEntity resourceToBeEvaluated = null;
				
				if (completedRes.getResource() != null) {
					resourceToBeEvaluated = completedRes.getResource();
				} else {
					resourceToBeEvaluated = completedRes;
				}
				
				Evaluation ev = instantiateEvaluation(resourceToBeEvaluated);
				ev.setDateCreated(currentTime);
				ev.setMaker(evaluationSubmission.getMaker());
				ev.setResource(resourceToBeEvaluated);
				ev.setEvaluationSubmission(evaluationSubmission);
				ev.setAccepted(true);
				
				if (evaluatedResData.getComment() == null || evaluatedResData.getComment().equals("")) {
					ev.setText(evaluationMessage);
				} else {
					ev.setText(evaluatedResData.getComment());
				}
				
				List<Badge> selectedBadges = evaluatedResData.getChosenBadges();
				
				ev.getBadges().addAll(selectedBadges);
				ev = saveEntity(ev);
				
				evaluationSubmission.addEvaluation(ev);
				submissionChanged = true;
			}
			
			// delete evaluations for which there is no EvaluatedResourceData instance
			if (!evaluationsDeleted.isEmpty()) {
				for (Evaluation evaluation : evaluationsDeleted) {
					evaluationSubmission.deleteEvaluation(evaluation);
					
					// TOTO: ask others what to do with this instance
					//portfolioManager.delete(evaluation);
				}
				submissionChanged = true;
			}
			
			// update text
			if (evaluationSubmission.getMessage() != null && evaluationMessage != null) {
				if (!evaluationSubmission.getMessage().equals(evaluationMessage)) {
					evaluationSubmission.setMessage(evaluationMessage);
					submissionChanged = true;
				}
			}
			
			if (submissionChanged) {
				evaluationSubmission.setFinalized(false);
				evaluationSubmission.setDateSubmitted(null);
				evaluationSubmission = saveEntity(evaluationSubmission);
//				eventFactory.generateEvent(EventType.EVALUATION_EDITED, maker, evaluationSubmission);
			}
			
			return evaluationSubmission;
		} else
			throw new InvalidParameterException("Several of parameters passed have null value: " +
					"evaluatedResources ["+evaluatedResources+"] ");
	}
	
	/**
	 * @param badges1
	 * @param badges2
	 * @return
	 */
	private boolean hasSameBadges(Collection<Badge> badges1, Collection<Badge> badges2) {
		if (badges1 != null && badges2 != null) {
			if (badges1.size() != badges2.size()) {
				return false;
			} else {
				for (Badge b1 : badges1) {
					boolean hasBadge = false;
					
					for (Badge b2 : badges2) {
						if (b1.getId() == b2.getId()) {
							hasBadge = true;
							break;
						}
					}
					
					if (!hasBadge) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	@Transactional (readOnly = true)
	public long getApprovedEvaluationCountForResource(Class<? extends BaseEntity> clazz, long resourceId) {
		return getApprovedEvaluationCountForResource(clazz, resourceId, getPersistence().currentManager());
	}
	
	@Override
	@Transactional (readOnly = true)
	public long getApprovedEvaluationCountForResource(Class<? extends BaseEntity> clazz, long resourceId, Session session) {
		return getEvaluationCountForResource(clazz, resourceId, true, session);
	}
	
	@Override
	@Transactional (readOnly = true)
	public long getRejectedEvaluationCountForResource(Class<? extends BaseEntity> clazz, long resourceId) {
		return getRejectedEvaluationCountForResource(clazz, resourceId, getPersistence().currentManager());
	}
	
	@Override
	@Transactional (readOnly = true)
	public long getRejectedEvaluationCountForResource(Class<? extends BaseEntity> clazz, long resourceId, Session session) {
		return getEvaluationCountForResource(clazz, resourceId, false, session);
	}

	@Override
	@Transactional (readOnly = true)
	public long getEvaluationCountForResource(Class<? extends BaseEntity> clazz, long resourceId, boolean approved, Session session) {
		String property = clazz.getSimpleName();
		property = property.substring(0, 1).toLowerCase() + property.substring(1, property.length());
		
		String query = 
			"SELECT COUNT(DISTINCT ev) " +
			"FROM EvaluationSubmission evSubmission " +
			"LEFT JOIN evSubmission.evaluations ev " +
			"LEFT JOIN ev."+property+" res " +
			"WHERE res.id = :resourceId " +
				"AND evSubmission.finalized = true " +
				"AND ev.accepted = :accepted " +
			"ORDER BY ev.dateCreated DESC";
		
		Long count = (Long) session.createQuery(query).
			setLong("resourceId", resourceId).
			setBoolean("accepted", approved).
			uniqueResult();
		
		if (clazz.equals(AchievedCompetence.class)) {
			TargetCompetence tComp = portfolioManager.getTargetCompetenceOfAchievedCompetence(resourceId, session);
			
			if (tComp != null) {
				long tCompEvaluationCount = getEvaluationCountForResource(TargetCompetence.class, tComp.getId(), approved, session);
				
				count += tCompEvaluationCount;
			}
		}
		return count;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Evaluation> getApprovedEvaluationsForResource(Class<? extends BaseEntity> clazz, long resourceId) {
		return getEvaluationsForResource(clazz, resourceId, true);
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Evaluation> getRejectedEvaluationsForResource(Class<? extends BaseEntity> clazz, long resourceId) {
		return getEvaluationsForResource(clazz, resourceId, false);
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Evaluation> getEvaluationsForResource(Class<? extends BaseEntity> clazz, long resourceId, boolean approved) {
		String property = clazz.getSimpleName();
		property = property.substring(0, 1).toLowerCase() + property.substring(1, property.length());
		
		String query = 
			"SELECT DISTINCT ev " +
			"FROM EvaluationSubmission evSubmission " +
			"LEFT JOIN evSubmission.evaluations ev " +
			"LEFT JOIN ev."+property+" res " +
			"WHERE res.id = :resourceId " +
				"AND evSubmission.finalized = true " +
				"AND ev.accepted = :accepted " +
			"ORDER BY ev.dateCreated DESC";
		
		@SuppressWarnings("unchecked")
		List<Evaluation> result = persistence.currentManager().createQuery(query).
			setLong("resourceId", resourceId).
			setBoolean("accepted", approved).
			list();
		
		if (result == null) {
			result = new ArrayList<Evaluation>();
		}
		
		if (clazz.equals(AchievedCompetence.class)) {
			TargetCompetence tComp = portfolioManager.getTargetCompetenceOfAchievedCompetence(resourceId);
			
			if (tComp != null) {
				List<Evaluation> tCompEvaluations = getEvaluationsForResource(TargetCompetence.class, tComp.getId(), approved);
				
				if (tCompEvaluations != null) {
					result.addAll(tCompEvaluations);
				}
			}
		}
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Evaluation> getApprovedEvaluationsForResource(BaseEntity resource) {
		resource = HibernateUtil.initializeAndUnproxy(resource);
		return getApprovedEvaluationsForResource(resource.getClass(), resource.getId());
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Evaluation> getRejectedEvaluationsForResource(BaseEntity resource) {
		resource = HibernateUtil.initializeAndUnproxy(resource);
		return getRejectedEvaluationsForResource(resource.getClass(), resource.getId());
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<EvaluationSubmission> getEvaluationsByUser(User user, boolean sortDesc, EvaluationFilter filter) {
		StringBuffer query = new StringBuffer(
			"SELECT DISTINCT evSubm " +
			"FROM EvaluationSubmission evSubm " +
			"WHERE evSubm.maker = :user ");
			
		if (filter.equals(EvaluationFilter.DRAFT) || filter.equals(EvaluationFilter.SUBMITTED)) {
			query.append("AND evSubm.finalized = :finalized");
		}

		query.append(" ORDER BY evSubm.dateSubmitted " + (sortDesc ? "DESC" : "ASC"));
		
		Query q = persistence.currentManager().createQuery(query.toString()).
				setEntity("user", user);
		
		if (filter.equals(EvaluationFilter.DRAFT)) {
			q.setBoolean("finalized", false);
		} else if (filter.equals(EvaluationFilter.SUBMITTED)) {
			q.setBoolean("finalized", true);
		}
		
		@SuppressWarnings("unchecked")
		List<EvaluationSubmission> result = q.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}

		return null;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getEvaluatorsWhoAcceptedResource(User user, BaseEntity resource) {
		String property = resource.getClass().getSimpleName();
		property = property.substring(0, 1).toLowerCase() + property.substring(1, property.length());
		
		String query = 
			"SELECT maker " +
			"FROM EvaluationSubmission evSubmission " +
			"LEFT JOIN evSubmission.evaluations ev " +
			"LEFT JOIN ev."+property+" res " +
			"LEFT JOIN ev.maker maker " +
			"LEFT JOIN ev.evaluationSubmission evSubmission " +
			"LEFT JOIN evSubmission.request request " +
			"LEFT JOIN request.maker user " +
			"WHERE res.id = :resourceId " + 
				"AND user = :user " +
				"AND evSubmission.accepted = true " +
				"AND evSubmission.finalized = true";

		@SuppressWarnings("unchecked")
		List<User> result = persistence.currentManager().createQuery(query).
			setEntity("user", user).
			setLong("resourceId", resource.getId()).
			list();
		
		if (result == null) {
			result = new ArrayList<User>();
		}
		
		if (resource.getClass().equals(AchievedCompetence.class)) {
			TargetCompetence tComp = portfolioManager.getTargetCompetenceOfAchievedCompetence(resource.getId());
			
			if (tComp != null) {
				TargetCompetence targetCompetence = ((AchievedCompetence) resource).getTargetCompetence();
				
				if (targetCompetence != null) {
					
					List<User> tCompEvaluators = getEvaluatorsWhoAcceptedResource(user, targetCompetence);
					
					result.addAll(tCompEvaluators);
				}
			}
		}
		
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean hasUserRequestedEvaluation(User user, BaseEntity resource) {
		String requestProperty = resource.getClass().getSimpleName();
		requestProperty = requestProperty.substring(0, 1).toLowerCase() + requestProperty.substring(1, requestProperty.length());
		
		if (Node.class.isAssignableFrom(resource.getClass())) {
			requestProperty = "node";
		}
		requestProperty += "Resource";
		
		String query = 
			"SELECT COUNT(request) " +
			"FROM Request request " +
			"WHERE request.maker = :user " +
				"AND request."+requestProperty+" = :resource ";
		
		Long result = (Long) persistence.currentManager().createQuery(query).
			setEntity("user", user).
			setEntity("resource", resource).
			uniqueResult();
		
		return result > 0;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getEvaluatorsWhoIgnoredResource(User user, BaseEntity resource) {
		List<User> result = null;
		
//		if (resource instanceof Node || resource instanceof ExternalCredit) {
			// get users who have ignored evaluation request
			String requestProperty = resource.getClass().getSimpleName();
			requestProperty = requestProperty.substring(0, 1).toLowerCase() + requestProperty.substring(1, requestProperty.length());
			
			if (Node.class.isAssignableFrom(resource.getClass())) {
				requestProperty = "node";
			}
			requestProperty += "Resource";
			
			String query = 
				"SELECT request.sentTo " +
				"FROM Request request " +
				"WHERE request.maker = :user " +
					"AND request."+requestProperty+" = :resource " +
					"AND request.requestType = :requestType " +
					"AND request.status != :acceptedStatus ";
			
			@SuppressWarnings("unchecked")
			List<User> users = persistence.currentManager().createQuery(query).
				setEntity("user", user).
				setEntity("resource", resource).
				setString("requestType", EventType.EVALUATION_REQUEST.name()).
				setString("acceptedStatus", RequestStatus.ACCEPTED.name()).
				list();
			
			result = users;
//		}
		
		if (result == null) {
			result = new ArrayList<User>();
		}
		
		// get users who have created unfinalised evaluation submissions
		String evaluationProperty = resource.getClass().getSimpleName();
		evaluationProperty = evaluationProperty.substring(0, 1).toLowerCase() + evaluationProperty.substring(1, evaluationProperty.length());
		
		String query1 = 
			"SELECT maker " +
			"FROM EvaluationSubmission evSubmission " +
			"LEFT JOIN evSubmission.evaluations ev " +
			"LEFT JOIN ev."+evaluationProperty+" res " +
			"LEFT JOIN ev.maker maker " +
			"LEFT JOIN ev.evaluationSubmission evSubmission " +
			"LEFT JOIN evSubmission.request request " +
			"LEFT JOIN request.maker user " +
			"WHERE res.id = :resourceId " + 
				"AND user = :user " +
				"AND evSubmission.finalized = false";

		@SuppressWarnings("unchecked")
		List<User> result1 = persistence.currentManager().createQuery(query1).
			setEntity("user", user).
			setLong("resourceId", resource.getId()).
			list();
		
		if (result1 == null) {
			result1 = new ArrayList<User>();
		}

		result.addAll(result1);
		
		if (resource.getClass().equals(AchievedCompetence.class)) {
			TargetCompetence tComp = portfolioManager.getTargetCompetenceOfAchievedCompetence(resource.getId());
			
			if (tComp != null) {
				TargetCompetence targetCompetence = ((AchievedCompetence) resource).getTargetCompetence();
				
				if (targetCompetence != null) {
					
					List<User> tCompIgnoredEvaluators = getEvaluatorsWhoIgnoredResource(user, targetCompetence);
					
					result.addAll(tCompIgnoredEvaluators);
				}
			}
		}
		
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public EvaluationSubmission getEvaluationSubmission(long requestId) {
		String query = 
			"SELECT evaluationSubmission " +
			"FROM EvaluationSubmission evaluationSubmission " +
			"LEFT JOIN evaluationSubmission.request request " +
			"WHERE request.id = :requestId ";
		
		EvaluationSubmission result = (EvaluationSubmission) persistence.currentManager().createQuery(query).
			setLong("requestId", requestId).
			uniqueResult();
		
		return result;
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean isThereEvaluationSubmissionForRequest(Request request) {
		String query = 
			"SELECT COUNT(evaluationSubmission) " +
			"FROM EvaluationSubmission evaluationSubmission " +
			"LEFT JOIN evaluationSubmission.request request " +
			"WHERE request = :request ";
		
		Long number = (Long) persistence.currentManager().createQuery(query).
			setEntity("request", request).
			uniqueResult();
			
		return number > 0;
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean isOtherSubmissionBasedOnThisSubmission(EvaluationSubmission evaluationSubmission) {
		String query = 
			"SELECT COUNT(evaluationSubmission) " +
			"FROM EvaluationSubmission evaluationSubmission " +
			"WHERE evaluationSubmission.basedOn = :evaluationSubmission ";
		
		Long number = (Long) persistence.currentManager().createQuery(query).
			setEntity("evaluationSubmission", evaluationSubmission).
			uniqueResult();
			
		return number > 0;
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean isWaitingForSubmissionRequestFromUser(BaseEntity resource, User sentTo) {
		String requestProperty = resource.getClass().getSimpleName();
		requestProperty = requestProperty.substring(0, 1).toLowerCase() + requestProperty.substring(1, requestProperty.length());
		
		if (Node.class.isAssignableFrom(resource.getClass())) {
			requestProperty = "node";
		}
		requestProperty += "Resource";
		
		String query = 
			"SELECT COUNT(request) " +
			"FROM Request request " +
			"WHERE request.sentTo = :sentTo " +
				"AND request.status = :status " +
				"AND request."+requestProperty+" = :resource ";
		
		Long result = (Long) persistence.currentManager().createQuery(query).
			setEntity("sentTo", sentTo).
			setEntity("resource", resource).
			setString("status", RequestStatus.SENT.name()).
			uniqueResult();
		
		return result > 0;
	}
	
	public Evaluation instantiateEvaluation(BaseEntity resource) throws EvaluationNotSupportedException {
		if (resource instanceof AchievedCompetence) {
			return new AchievedCompetenceEvaluation();
		} else if (resource instanceof TargetLearningGoal) {
			return new TargetLearningGoalEvaluation();
		} else if (resource instanceof ExternalCredit) {
			return new ExternalCreditEvaluation();
		} else if (resource instanceof TargetCompetence) {
			return new TargetCompetenceEvaluation();
		} else {
			throw new EvaluationNotSupportedException("Evaluation for resource of a type " 
					+ resource.getClass() + " is not supported");
		}
	}
	
	@Override
	@Transactional(readOnly=true)
	public boolean hasAnyBadge(Class<? extends BaseEntity> clazz, long resourceId) throws DbConnectionException {
		try{
			String property = clazz.getSimpleName();
			property = property.substring(0, 1).toLowerCase() + property.substring(1, property.length());
			
			String query = 
				"SELECT COUNT(DISTINCT badge) " +
				"FROM EvaluationSubmission evSubmission " +
				"LEFT JOIN evSubmission.evaluations ev " +
				"LEFT JOIN ev."+property+" res " +
				"LEFT JOIN ev.badges badge " +
				"WHERE res.id = :resourceId " +
					"AND evSubmission.finalized = true " +
					"AND ev.accepted = :accepted ";
			
			Long count = (Long) persistence.currentManager().createQuery(query).
				setLong("resourceId", resourceId).
				setBoolean("accepted", true).
				uniqueResult();
			
			if(count > 0) {
				return true;
			}
			return false;
		}catch(Exception e){
			throw new DbConnectionException("Error while loading badges");
		}
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<Evaluation> getEvaluationsForAResource(Class<? extends BaseEntity> clazz, long resourceId) throws DbConnectionException {
		try{
			String property = clazz.getSimpleName();
			property = property.substring(0, 1).toLowerCase() + property.substring(1, property.length());
			
			String query = 
				"SELECT ev " +
				"FROM EvaluationSubmission evSubmission " +
				"LEFT JOIN evSubmission.evaluations ev " +
				"LEFT JOIN ev."+property+" res " +
				"WHERE res.id = :resourceId " +
					"AND evSubmission.finalized = true " +
					"ORDER BY ev.dateCreated DESC";
			
			List<Evaluation> evaluations = persistence.currentManager().createQuery(query).
				setLong("resourceId", resourceId).
				list();
			return evaluations;
		}catch(Exception e){
			throw new DbConnectionException("Error while loading badges");
		}
	}
}
