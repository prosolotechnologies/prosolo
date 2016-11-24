/**
 * 
 */
package org.prosolo.services.nodes;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.evaluation.Evaluation;
import org.prosolo.common.domainmodel.evaluation.EvaluationSubmission;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.exceptions.EvaluationNotSupportedException;
import org.prosolo.services.nodes.exceptions.InvalidParameterException;
import org.prosolo.web.communications.evaluation.data.EvaluatedResourceData;
import org.prosolo.web.communications.evaluation.data.EvaluationFilter;

/**
 * @author "Nikola Milikic"
 *
 */
public interface EvaluationManager extends AbstractManager {

	Collection<Request> sendEvaluationRequests(long userId, Collection<Long> evaluatorIds, 
			BaseEntity resource, String text) throws EventException;

	Request resubmitEvaluationRequest(long makerId, EvaluationSubmission previousSubmission, String text) throws EventException;

//	EvaluationSubmission createEvaluationSubmission(User maker, Request evaluationRequest,
//			List<EvaluatedResourceData> list,
//			List<EvaluatedResourceData> addedCompetences,
//			List<Badge> badges,
//			String evaluationMessage) throws InvalidParameterException, EventException;
	
	EvaluationSubmission createEvaluationSubmissionDraft(long userId, Request evaluationRequest) throws InvalidParameterException, EventException, EvaluationNotSupportedException, ResourceCouldNotBeLoadedException;

	EvaluationSubmission editEvaluationSubmissionDraft(
			EvaluationSubmission evaluationSubmission,
			List<EvaluatedResourceData> selectedEvaluatedResources,
			String evaluationMessage) throws EventException, InvalidParameterException, EvaluationNotSupportedException;
	
	List<Evaluation> getApprovedEvaluationsForResource(BaseEntity resource);
	
	List<Evaluation> getRejectedEvaluationsForResource(BaseEntity resource);
	
	List<Evaluation> getApprovedEvaluationsForResource(Class<? extends BaseEntity> clazz, long resourceId);
	
	List<Evaluation> getRejectedEvaluationsForResource(Class<? extends BaseEntity> clazz, long resourceId);
	
	List<Evaluation> getEvaluationsForResource(Class<? extends BaseEntity> clazz, long resourceId, boolean approved);
	
	long getApprovedEvaluationCountForResource(Class<? extends BaseEntity> clazz, long resourceId);
	
	long getApprovedEvaluationCountForResource(Class<? extends BaseEntity> clazz, long resourceId, Session session);

	long getRejectedEvaluationCountForResource(Class<? extends BaseEntity> clazz, long resourceId);

	long getRejectedEvaluationCountForResource(Class<? extends BaseEntity> clazz, long resourceId, Session session);
	
	long getEvaluationCountForResource(Class<? extends BaseEntity> clazz, long resourceId, boolean approved, Session session);
	
	List<EvaluationSubmission> getEvaluationsByUser(long userId, boolean sortDesc, EvaluationFilter filter);
	
	List<User> getEvaluatorsWhoAcceptedResource(long userId, BaseEntity resource);

	List<User> getEvaluatorsWhoIgnoredResource(long userId, BaseEntity resource);

	EvaluationSubmission finalizeEvaluationSubmission(
			EvaluationSubmission evaluationSubmission,
			List<EvaluatedResourceData> evaluatedResources,
			EvaluatedResourceData primeEvaluatedResource) throws InvalidParameterException,
			EventException, EvaluationNotSupportedException;

	EvaluationSubmission getEvaluationSubmission(long evaluationRequestId);

	boolean isThereEvaluationSubmissionForRequest(Request request);

	boolean hasUserRequestedEvaluation(long userId, BaseEntity resource);

	boolean isOtherSubmissionBasedOnThisSubmission(EvaluationSubmission evaluationSubmission);

	boolean isWaitingForSubmissionRequestFromUser(BaseEntity resource, User sentTo);
	
	public boolean hasAnyBadge(Class<? extends BaseEntity> clazz, long resourceId) throws DbConnectionException;
	
	public List<Evaluation> getEvaluationsForAResource(Class<? extends BaseEntity> clazz, long resourceId) throws DbConnectionException;

}