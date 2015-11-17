package org.prosolo.web.students.data.learning;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.prosolo.common.domainmodel.workflow.evaluation.Evaluation;
import org.prosolo.common.domainmodel.workflow.evaluation.EvaluationSubmission;

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
	
	public String getFormattedDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		return sdf.format(submissionDate);
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
