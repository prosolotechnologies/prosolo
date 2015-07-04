/**
 * 
 */
package org.prosolo.web.communications.evaluation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.prosolo.domainmodel.activities.requests.Request;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.domainmodel.portfolio.ExternalCredit;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.workflow.evaluation.Badge;
import org.prosolo.domainmodel.workflow.evaluation.Evaluation;
import org.prosolo.domainmodel.workflow.evaluation.EvaluationSubmission;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.util.date.DateUtil;
import org.prosolo.util.nodes.NodeTitleComparator;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.communications.evaluation.data.EvaluatedExCreditData;
import org.prosolo.web.communications.evaluation.data.EvaluatedResourceData;
import org.prosolo.web.communications.evaluation.data.EvaluatedResourceType;
import org.prosolo.web.communications.evaluation.data.EvaluationFormData;
import org.prosolo.web.communications.evaluation.data.EvaluationSubmissionData;
import org.prosolo.web.communications.evaluation.data.SelectedBadge;
import org.prosolo.web.goals.util.CompWallActivityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.web.communications.evaluation.EvaluationDataFactory")
public class EvaluationDataFactory implements Serializable {
	
	private static final long serialVersionUID = 5728785672837678349L;

	@Autowired private CompWallActivityConverter compWallActivityConverter;
	@Autowired private EvaluationManager evaluationManager;

	public EvaluationFormData create(Request request, List<Badge> badges, Locale locale) {
		BaseEntity resource = request.getResource();

		EvaluationFormData formData = new EvaluationFormData();
		
		formData.setRequester(request.getMaker());
		formData.setDateRequested(DateUtil.getPrettyDate(request.getDateCreated()));
		formData.setCanBeResubmitted(false);
		
		if (resource instanceof TargetLearningGoal) {
			TargetLearningGoal targetGoal = (TargetLearningGoal) resource;
			
			EvaluatedResourceData goalEvaluationData = new EvaluatedResourceData(
					targetGoal, 
					EvaluatedResourceType.GOAL, 
					false);
			goalEvaluationData.addBadges(badges);
			
			formData.addEvaluatedResourceData(goalEvaluationData);
			formData.setPrimeEvaluatedResource(goalEvaluationData);
			
			// removing TargetCompetence duplicates
			// TODO: duplicates shouldn't be here in the first place
			List<TargetCompetence> competences = new ArrayList<TargetCompetence>();
			competences.addAll(targetGoal.getTargetCompetences());
			Collections.sort(competences, new NodeTitleComparator());
			
			if (!competences.isEmpty()) {
				for (TargetCompetence targetCompetence : competences) {
					EvaluatedResourceData evaluatedResourceData = new EvaluatedResourceData(
						targetCompetence, 
						EvaluatedResourceType.TARGET_COMPETENCE,
						false);
					evaluatedResourceData.addBadges(badges);
					
					formData.addEvaluatedResourceData(evaluatedResourceData);
				}
			}
		} else if (resource instanceof AchievedCompetence) {
			AchievedCompetence achievedCompetence = (AchievedCompetence) resource;
			
			EvaluatedResourceData compEvaluationData = new EvaluatedResourceData(
					achievedCompetence.getId(),
					achievedCompetence.getCompetence().getTitle(), 
					EvaluatedResourceType.ACHIEVED_COMPETENCE);
			
			compEvaluationData.setResource(achievedCompetence);
			compEvaluationData.addBadges(badges);
			
			formData.addEvaluatedResourceData(compEvaluationData);
			formData.setPrimeEvaluatedResource(compEvaluationData);
		} else if (resource instanceof ExternalCredit) {
			ExternalCredit exCredit = (ExternalCredit) resource;
			
			EvaluatedResourceData exCreditEvaluationData = new EvaluatedExCreditData(
					exCredit, 
					EvaluatedResourceType.EXTERNAL_CREDIT);
			
			exCreditEvaluationData.addBadges(badges);
			
			formData.addEvaluatedResourceData(exCreditEvaluationData);
			formData.setPrimeEvaluatedResource(exCreditEvaluationData);
			
			List<AchievedCompetence> competences = exCredit.getCompetences();
			Collections.sort(competences, new NodeTitleComparator());
			
			if (competences != null && !competences.isEmpty()) {
				for (AchievedCompetence achievedComp : competences) {
					EvaluatedResourceData evaluatedResourceData = new EvaluatedResourceData(
							achievedComp.getId(),
							achievedComp.getCompetence().getTitle(),
							EvaluatedResourceType.ACHIEVED_COMPETENCE);
					
					evaluatedResourceData.setResource(achievedComp);
					evaluatedResourceData.addBadges(badges);
					
					formData.addEvaluatedResourceData(evaluatedResourceData);
				}
			}
			
			List<ActivityWallData> activitiesData = compWallActivityConverter.generateCompWallActivitiesData(exCredit, locale);
			formData.setUsedActivities(activitiesData);
		} else if (resource instanceof TargetCompetence) {
			TargetCompetence targetCompetence = (TargetCompetence) resource;
			
			EvaluatedResourceData compEvaluationData = new EvaluatedResourceData(
					targetCompetence,
					EvaluatedResourceType.TARGET_COMPETENCE,
					true);
			compEvaluationData.setTitle(targetCompetence.getCompetence().getTitle());
			
			compEvaluationData.addBadges(badges);
			
			formData.addEvaluatedResourceData(compEvaluationData);
			formData.setPrimeEvaluatedResource(compEvaluationData);
			
			List<ActivityWallData> activitiesData = compWallActivityConverter.generateCompWallActivitiesData(targetCompetence, locale);
			formData.setUsedActivities(activitiesData);
		}
		
		return formData;
	}
	
	public EvaluationFormData create(EvaluationSubmission evaluationSubmission, List<Badge> badges, Locale locale, User loggedUser) {
		Request request = evaluationSubmission.getRequest();
		
		EvaluationFormData formData = create(request, badges, locale);
		
		formData.setDraft(!evaluationSubmission.isFinalized());
		formData.setDateSubmitted(DateUtil.getPrettyDate(evaluationSubmission.getDateSubmitted()));
		formData.setDateRequested(DateUtil.getPrettyDate(request.getDateCreated()));
		formData.getPrimeEvaluatedResource().setComment(evaluationSubmission.getMessage());
		
		// fill in evaluation submission history
		formData.setSubmissionHistory(constructEvaluationHistory(evaluationSubmission, true, false));
		formData.getSubmissionHistory().add(new EvaluationSubmissionData(evaluationSubmission.getId(), evaluationSubmission.getDateCreated(), false));
		formData.getSubmissionHistory().addAll(constructEvaluationHistory(evaluationSubmission, true, true));
		Collections.sort(formData.getSubmissionHistory());

		Set<Evaluation> evaluations = evaluationSubmission.getEvaluations();
		
		if (evaluations != null && !evaluations.isEmpty()) {
			
			// loop through all EvaluatedResourceData instances and select those for which 
			// there is an evaluation comment
			for (Evaluation ev : evaluations) {
				BaseEntity res = ev.getResource();
				
				evaluationDataLoop: for (EvaluatedResourceData evaluatedResData : formData.getEvaluatedResources()) {
					BaseEntity evaluatedRes = evaluatedResData.getResource();
						
					if (res instanceof AchievedCompetence) {
						if (((AchievedCompetence) res).getTargetCompetence() != null) {
							res = ((AchievedCompetence) res).getTargetCompetence();
						} else { 
							res = ((AchievedCompetence) res).getCompetence();
						}
					}
					
					if (res.equals(evaluatedRes)) {
						evaluatedResData.setAccepted(ev.isAccepted());
						evaluatedResData.setComment(ev.getText());
						evaluatedResData.setReadOnly(ev.isReadOnly());
						
						badgeLoop: for (Badge badge : ev.getBadges()) {
							for (SelectedBadge selectedBadge : evaluatedResData.getBadges()) {
								if (badge.getId() == selectedBadge.getBadge().getId()) {
									selectedBadge.setSelected(true);
									break badgeLoop;
								}
							}
						}
						
						break evaluationDataLoop;
					}
				}
			}
			
			// loop through all evaluations to look for the ones referencing added competences
			evaluationLoop: for (Evaluation ev : evaluations) {
				
				BaseEntity res = ev.getResource();
				
				if (res instanceof AchievedCompetence) {
					
					for (EvaluatedResourceData evaluatedResData : formData.getEvaluatedResources()) {
						BaseEntity evaluatedRes = evaluatedResData.getResource();
						
						if (evaluatedRes instanceof TargetCompetence) {
							if (((TargetCompetence) evaluatedRes).getCompetence().equals(((AchievedCompetence) res).getCompetence())) {
								continue evaluationLoop;
							}
						} else if (evaluatedRes instanceof Competence) {
							if (((Competence) evaluatedRes).equals(((AchievedCompetence) res).getCompetence())) {
								continue evaluationLoop;
							}
						} else if (evaluatedRes instanceof AchievedCompetence) {
							if (((AchievedCompetence) evaluatedRes).getCompetence().equals(((AchievedCompetence) res).getCompetence())) {
								continue evaluationLoop;
							}
						}
					}
					
					// if the loop has not been interrupted, i.e. the resource for the evaluation has 
					// not been found, then add this competences to the list of added competences
					EvaluatedResourceData addedCompData = new EvaluatedResourceData(res, EvaluatedResourceType.ACHIEVED_COMPETENCE, ev.getText());
					addedCompData.addBadges(badges);
					
					badgeLoop: for (Badge badge : ev.getBadges()) {
						for (SelectedBadge selectedBadge : addedCompData.getBadges()) {
							if (badge.getId() == selectedBadge.getBadge().getId()) {
								selectedBadge.setSelected(true);
								break badgeLoop;
							}
						}
					}
					
					addedCompData.setAccepted(false);
					formData.addCompetence(addedCompData);
				}
			}
		}
		
		if (evaluationManager.isWaitingForSubmissionRequestFromUser(request.getResource(), request.getSentTo())) {
			formData.setWaitingForSubmission(true);
		}
		
		if (!formData.isDraft() && !formData.getPrimeEvaluatedResource().isAccepted() && 
				!evaluationManager.isOtherSubmissionBasedOnThisSubmission(evaluationSubmission) &&
				!formData.isWaitingForSubmission() &&
				request.getMaker().getId() == loggedUser.getId()) {
			formData.setCanBeResubmitted(true);
		}
		
		return formData;
	}

	private List<EvaluationSubmissionData> constructEvaluationHistory(EvaluationSubmission evaluationSubmission, boolean first, boolean addChildren) {
		List<EvaluationSubmissionData> historyList = new ArrayList<EvaluationSubmissionData>();
		
		if (addChildren) {
			EvaluationSubmission basedOn = evaluationSubmission.getBasedOnChild();
			
			if (basedOn != null)
				historyList.addAll(constructEvaluationHistory(basedOn, false, addChildren));
		} else {
			EvaluationSubmission basedOn = evaluationSubmission.getBasedOn();

			if (basedOn != null)
				historyList.addAll(constructEvaluationHistory(basedOn, false, addChildren));
		}
		
		if (!first) {
			EvaluationSubmissionData ev = new EvaluationSubmissionData(evaluationSubmission.getId(), evaluationSubmission.getDateCreated(), true);
			
			historyList.add(ev);
		}
		
		return historyList;
	}
	
}
