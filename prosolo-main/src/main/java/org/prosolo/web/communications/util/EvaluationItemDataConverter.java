/**
 * 
 */
package org.prosolo.web.communications.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.evaluation.Badge;
import org.prosolo.common.domainmodel.evaluation.Evaluation;
import org.prosolo.common.domainmodel.evaluation.EvaluationSubmission;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.portfolio.CompletedGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.NodeData;
import org.prosolo.util.nodes.NodeCreatedDescComparator;
import org.prosolo.web.communications.evaluation.data.EvaluationItemData;
import org.prosolo.web.communications.evaluation.data.SelectedBadge;
import org.prosolo.web.util.ResourceBundleUtil;

/**
 * @author "Nikola Milikic"
 *
 */
public class EvaluationItemDataConverter {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EvaluationItemDataConverter.class);

	public static List<EvaluationItemData> convertEvaluationSubmissions(List<EvaluationSubmission> evaluations, Locale locale) {
		List<EvaluationItemData> evData = new ArrayList<EvaluationItemData>();
		
		if (evaluations != null && !evaluations.isEmpty()) {
			//Collections.sort(evaluations, new NodeCreatedDescComparator());
			
			for (EvaluationSubmission e : evaluations) {
				evData.add(convertEvaluationSubmission(e, locale));
			}
		}
		
		return evData;
	}

	private static EvaluationItemData convertEvaluationSubmission(EvaluationSubmission evaluationSubmission, Locale locale) {
		EvaluationItemData eData = new EvaluationItemData();
		eData.setDate(DateUtil.getPrettyDate(evaluationSubmission.getDateCreated()));
		
		User evaluatedResourceMaker = evaluationSubmission.getRequest().getMaker();
		
		// user data
		eData.setUser(new UserData(evaluatedResourceMaker));
		
		// name
		eData.setName(evaluatedResourceMaker.getName() +" "+evaluatedResourceMaker.getLastname());
		
		// goal name
		BaseEntity resource = evaluationSubmission.getRequest().getResource();
		
		if (resource instanceof CompletedGoal) {
			resource = ((CompletedGoal) resource).getTargetGoal();
		} else if (resource instanceof CompletedGoal) {
			resource = (CompletedGoal) resource;
		} else if (resource instanceof AchievedCompetence) {
			resource = ((AchievedCompetence) resource).getTargetCompetence();
		}
		
		eData.setTitle(resource.getTitle());
		
		// message
		String message = evaluationSubmission.getMessage();
		eData.setMessage(message);
		eData.setResource(new NodeData(resource));
		eData.getResource().setShortType(ResourceBundleUtil.getResourceType(resource.getClass(), locale));
		
		//date
		eData.setDate(DateUtil.getPrettyDate(evaluationSubmission.getDateCreated()));
		eData.setDateSubmitted(DateUtil.getPrettyDate(evaluationSubmission.getDateSubmitted()));
		
		// draft
		eData.setDraft(!evaluationSubmission.isFinalized());
		
		// badges
		for (Badge b : evaluationSubmission.getPrimaryEvaluation().getBadges()) {
			eData.addBadge(new SelectedBadge(b, true));
		}
		
		// evaluation submission
		eData.setEvaluationSubmissionId(evaluationSubmission.getId());
		
		return eData;
	}
	
	public static List<EvaluationItemData> convertEvaluations(List<Evaluation> evaluations) {
		List<EvaluationItemData> evData = new ArrayList<EvaluationItemData>();
		
		if (evaluations != null && !evaluations.isEmpty()) {
			Collections.sort(evaluations, new NodeCreatedDescComparator());
			
			for (Evaluation e : evaluations) {
				evData.add(convertEvaluation(e));
			}
		}
		
		return evData;
	}

	private static EvaluationItemData convertEvaluation(Evaluation e) {
		EvaluationSubmission evaluationSubmission = e.getEvaluationSubmission();
		
		EvaluationItemData eData = new EvaluationItemData();
		eData.setId(e.getId());
		
		User evaluatedResourceMaker = evaluationSubmission.getRequest().getMaker();

		// creator
		eData.setCreator(e.getMaker());
		
		// user data
		eData.setUser(new UserData(evaluatedResourceMaker));
		
		// name
		eData.setName(evaluatedResourceMaker.getName() +" "+evaluatedResourceMaker.getLastname());
		
		// goal name
		eData.setTitle(evaluationSubmission.getRequest().getResource().getTitle());
		
		eData.setAccepted(e.isAccepted());
		
		// message
		
		if (e.getText() != null) {
			eData.setMessage(e.getText());
		} else {
			eData.setMessage(evaluationSubmission.getMessage());
		}
		
		//date
		eData.setDate(DateUtil.getPrettyDate(evaluationSubmission.getDateCreated()));
		
		// evaluation submission
		eData.setEvaluationSubmissionId(evaluationSubmission.getId());
		
		// badge
		for (Badge b : evaluationSubmission.getPrimaryEvaluation().getBadges()) {
			eData.addBadge(new SelectedBadge(b, true));
		}
		
		return eData;
	}
}
