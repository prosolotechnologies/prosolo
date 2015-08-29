package org.prosolo.web.dialogs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.web.communications.evaluation.data.EvaluationItemData;
import org.prosolo.web.communications.util.EvaluationItemDataConverter;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "evaluationlist")
@Component("evaluationlist")
@Scope("request")
public class EvaluationListDialog implements Serializable {

	private static final long serialVersionUID = -7585213157581185003L;
	
	private static Logger logger = Logger.getLogger(EvaluationListDialog.class);

	@Autowired private EvaluationManager evaluationManager;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private Collection<EvaluationItemData> evaluations = new ArrayList<EvaluationItemData>();
	private BaseEntity resource;
	
	public void initializeTargetGoalEvaluations(final long targetGoalId, final boolean accepted, final String context) {
		if (targetGoalId > 0) {
			evaluations = EvaluationItemDataConverter
					.convertEvaluations(evaluationManager.getEvaluationsForResource(TargetLearningGoal.class, targetGoalId, accepted));
			
			try {
				resource = evaluationManager.loadResource(TargetLearningGoal.class, targetGoalId, true);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
			
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	actionLogger.logServiceUse(
            			ComponentName.EVALUATION_LIST_DIALOG, 
            			"context", context,
            			"accepted", String.valueOf(accepted),
            			"resourceType", TargetLearningGoal.class.getSimpleName(),
            			"id", String.valueOf(targetGoalId));
	            }
			});
		}
	}

	public void initializeTargetCompetenceEvaluations(final long targetCompId, final boolean accepted, final String context) {
		if (targetCompId > 0) {
			evaluations = EvaluationItemDataConverter
				.convertEvaluations(evaluationManager.getApprovedEvaluationsForResource(TargetCompetence.class, targetCompId));
			
			try {
				resource = evaluationManager.loadResource(TargetCompetence.class, targetCompId);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
			
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	actionLogger.logServiceUse(
            			ComponentName.EVALUATION_LIST_DIALOG, 
            			"context", context,
            			"accepted", String.valueOf(accepted),
            			"resourceType", TargetCompetence.class.getSimpleName(),
            			"id", String.valueOf(targetCompId));
	            }
			});
		}
	}
	
	public void initialize(final BaseEntity resource, final String context) {
		if (resource != null) {
			evaluations = EvaluationItemDataConverter
				.convertEvaluations(evaluationManager.getApprovedEvaluationsForResource(resource));
			
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	actionLogger.logServiceUse(
            			ComponentName.EVALUATION_LIST_DIALOG, 
            			"context", context,
            			"resourceType", resource.getClass().getSimpleName(),
            			"id", String.valueOf(resource.getId()));
	            }
			});
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public Collection<EvaluationItemData> getEvaluations() {
		return evaluations;
	}

	public void setEvaluations(Collection<EvaluationItemData> evaluations) {
		this.evaluations = evaluations;
	}

	public BaseEntity getResource() {
		return resource;
	}
	
}
