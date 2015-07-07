/**
 * 
 */
package org.prosolo.web.communications.evaluation.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.web.activitywall.data.ActivityWallData;

/**
 * @author "Nikola Milikic"
 * 
 */
public class EvaluationFormData implements Serializable {

	private static final long serialVersionUID = 2065332318782716832L;

	private User requester;
	private EvaluatedResourceData primeEvaluatedResource;
	private List<EvaluatedResourceData> evaluatedResources;
	private List<EvaluatedResourceData> addedCompetences;
	private String dateSubmitted;
	private String dateRequested;
	private boolean draft;
	private List<ActivityWallData> usedActivities;
	private List<EvaluationSubmissionData> submissionHistory;
	
	private boolean canBeResubmitted;
	private boolean waitingForSubmission;
	
	public EvaluationFormData() {
		evaluatedResources = new ArrayList<EvaluatedResourceData>();
		addedCompetences = new ArrayList<EvaluatedResourceData>();
		submissionHistory = new ArrayList<EvaluationSubmissionData>();
	}

	public User getRequester() {
		return requester;
	}

	public void setRequester(User requester) {
		this.requester = requester;
	}

	public EvaluatedResourceData getPrimeEvaluatedResource() {
		return primeEvaluatedResource;
	}

	public void setPrimeEvaluatedResource(
			EvaluatedResourceData primeEvaluatedResource) {
		this.primeEvaluatedResource = primeEvaluatedResource;
	}

	public List<EvaluatedResourceData> getEvaluatedResources() {
		return evaluatedResources;
	}

	public void setEvaluatedResources(List<EvaluatedResourceData> evaluatedResources) {
		this.evaluatedResources = evaluatedResources;
	}
	
	public boolean addEvaluatedResourceData(EvaluatedResourceData evaluatedResourceData){
		if (evaluatedResourceData != null) {
			if (!getEvaluatedResources().contains(evaluatedResourceData)) {
				return getEvaluatedResources().add(evaluatedResourceData);
			}
		}
		return false;
	}
	
	public List<EvaluatedResourceData> getEvaluatedResourcesOfType(EvaluatedResourceType type){
		List<EvaluatedResourceData> filteredList = new ArrayList<EvaluatedResourceData>();
		
		for (EvaluatedResourceData evaluatedResourceData : evaluatedResources) {
			if (evaluatedResourceData.getType().equals(type)) {
				filteredList.add(evaluatedResourceData);
			}
		}
		
		return filteredList;
	}
	
	public List<EvaluatedResourceData> getEvaluatedLearningGoals(){
		return getEvaluatedResourcesOfType(EvaluatedResourceType.GOAL);
	}
	
	public List<EvaluatedResourceData> getEvaluatedCompetences(){
		List<EvaluatedResourceData> evaluatedCompetences =  getEvaluatedResourcesOfType(EvaluatedResourceType.TARGET_COMPETENCE);
		evaluatedCompetences.addAll(getEvaluatedResourcesOfType(EvaluatedResourceType.ACHIEVED_COMPETENCE));
		return evaluatedCompetences;
	}
	
	public String getDateSubmitted() {
		return dateSubmitted;
	}

	public void setDateSubmitted(String dateSubmitted) {
		this.dateSubmitted = dateSubmitted;
	}
	
	public String getDateRequested() {
		return dateRequested;
	}

	public void setDateRequested(String dateRequested) {
		this.dateRequested = dateRequested;
	}

	public List<EvaluatedResourceData> getAddedCompetences() {
		return addedCompetences;
	}

	public void setAddedCompetences(List<EvaluatedResourceData> addedCompetences) {
		this.addedCompetences = addedCompetences;
	}
	
	public boolean addCompetence(EvaluatedResourceData addedCompetence){
		if (addedCompetence != null) {
			if (!getAddedCompetences().contains(addedCompetence)) {
				return getAddedCompetences().add(addedCompetence);
			}
		}
		return false;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public List<ActivityWallData> getUsedActivities() {
		return usedActivities;
	}

	public void setUsedActivities(List<ActivityWallData> usedActivities) {
		this.usedActivities = usedActivities;
	}

	public boolean isCanBeResubmitted() {
		return canBeResubmitted;
	}

	public void setCanBeResubmitted(boolean canBeResubmitted) {
		this.canBeResubmitted = canBeResubmitted;
	}

	public List<EvaluationSubmissionData> getSubmissionHistory() {
		return submissionHistory;
	}

	public void setSubmissionHistory(List<EvaluationSubmissionData> submissionHistory) {
		this.submissionHistory = submissionHistory;
	}
	
	public void addSubmissionHistory(EvaluationSubmissionData submissionHistory) {
		this.submissionHistory.add(submissionHistory);
	}

	public boolean isWaitingForSubmission() {
		return waitingForSubmission;
	}

	public void setWaitingForSubmission(boolean waitingForSubmission) {
		this.waitingForSubmission = waitingForSubmission;
	}
	
}
