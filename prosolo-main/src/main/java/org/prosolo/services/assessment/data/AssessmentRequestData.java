package org.prosolo.services.assessment.data;

import java.io.Serializable;

public class AssessmentRequestData implements Serializable {

	private static final long serialVersionUID = 1L;

	private String messageText;
	private long studentId;
	private long assessorId;
	private String assessorFullName;
	private String assessorAvatarUrl;
	private long resourceId;
	private long targetResourceId;
	private boolean assessorSet;
	private boolean newAssessment;

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public long getStudentId() {
		return studentId;
	}

	public void setStudentId(long studentId) {
		this.studentId = studentId;
	}

	public long getAssessorId() {
		return assessorId;
	}
	
	public String getAssessorFullName() {
		return assessorFullName;
	}

	public void setAssessorFullName(String assessorFullName) {
		this.assessorFullName = assessorFullName;
	}

	public String getAssessorAvatarUrl() {
		return assessorAvatarUrl;
	}

	public void setAssessorAvatarUrl(String assessorAvatarUrl) {
		this.assessorAvatarUrl = assessorAvatarUrl;
	}

	public void setAssessorId(long assessorId) {
		this.assessorId = assessorId;
		assessorSet = true;
	}

	public long getTargetResourceId() {
		return targetResourceId;
	}

	public void setTargetResourceId(long targetResourceId) {
		this.targetResourceId = targetResourceId;
	}

	public boolean isAssessorSet() {
		return assessorSet;
	}
	
	public void resetAssessorData() {
		this.assessorId = 0;
		this.assessorFullName = null;
		this.assessorAvatarUrl = null;
		this.assessorSet = false;
		this.newAssessment = false;
	}

	public void setNewAssessment(boolean newAssessment) {
		this.newAssessment = newAssessment;
	}

	public boolean isNewAssessment() {
		return newAssessment;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public long getResourceId() {
		return resourceId;
	}
}
