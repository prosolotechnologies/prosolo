package org.prosolo.web.manage.students.data;

import org.prosolo.common.domainmodel.evaluation.Evaluation;
import org.prosolo.common.domainmodel.evaluation.EvaluationSubmission;

import java.util.Date;

public class EvaluationSubmissionData {

	private EvaluatorData evaluator;
	private String message;
	private Date submissionDate;
	private boolean accepted;
	private boolean trophyWon;
	
	public EvaluationSubmissionData() {
		
	}
	
	public EvaluationSubmissionData(Evaluation eval) {
		EvaluationSubmission es = eval.getEvaluationSubmission();
		evaluator = new EvaluatorData(es.getMaker());
		message = es.getMessage();
		submissionDate = es.getDateSubmitted();
		accepted = eval.isAccepted();
		if(eval.getBadges() != null && !eval.getBadges().isEmpty()){
			trophyWon = true;
		}
	}

	public long getTime() {
		return submissionDate.getTime();
	}

	public EvaluatorData getEvaluator() {
		return evaluator;
	}
	public void setEvaluator(EvaluatorData evaluator) {
		this.evaluator = evaluator;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getSubmissionDate() {
		return submissionDate;
	}
	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}
	public boolean isAccepted() {
		return accepted;
	}
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	public boolean isTrophyWon() {
		return trophyWon;
	}
	public void setTrophyWon(boolean trophyWon) {
		this.trophyWon = trophyWon;
	}

	
}
