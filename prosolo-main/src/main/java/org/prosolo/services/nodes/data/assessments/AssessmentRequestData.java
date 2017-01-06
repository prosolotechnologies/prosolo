package org.prosolo.services.nodes.data.assessments;

import java.io.Serializable;

public class AssessmentRequestData implements Serializable {

	private static final long serialVersionUID = 1L;

	private String messageText;
	private long studentId;
	private long assessorId;
	private String assessorFullName;
	private String assessorAvatarUrl;
	private long credentialId;
	private long targetCredentialId;
	private boolean assessorSet;
	private String credentialTitle;

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

	public long getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(long credentialId) {
		this.credentialId = credentialId;
	}

	public long getTargetCredentialId() {
		return targetCredentialId;
	}

	public void setTargetCredentialId(long targetCredentialId) {
		this.targetCredentialId = targetCredentialId;
	}

	public boolean isAssessorSet() {
		return assessorSet;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}
	
	public void resetAssessorData() {
		this.assessorId = 0;
		this.assessorFullName = null;
		this.assessorAvatarUrl = null;
		this.assessorSet = false;
	}

}