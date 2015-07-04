package org.prosolo.web.communications.evaluation.data;

import java.io.Serializable;
import java.util.Date;

public class EvaluationSubmissionData implements Serializable, Comparable<EvaluationSubmissionData> {
	
	private static final long serialVersionUID = -4435703497821965122L;
	
	private long id;
	private Date dateCreated;
	private boolean actionable;
	
	public EvaluationSubmissionData(long id, Date dateCreated, boolean actionable) {
		this.id = id;
		this.dateCreated = dateCreated;
		this.actionable = actionable;
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public boolean isActionable() {
		return actionable;
	}

	public void setActionable(boolean actionable) {
		this.actionable = actionable;
	}
	
	@Override
	public int compareTo(EvaluationSubmissionData o) {
		return this.dateCreated.compareTo(o.getDateCreated());
	}
	
}
