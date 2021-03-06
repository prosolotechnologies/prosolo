package org.prosolo.services.assessment.data;

import org.prosolo.common.domainmodel.assessment.AssessmentStatus;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;

public class AssessmentData {

	private String studentFullName;
	private String studentAvatarUrl;
	private long studentId;
	private String assessorFullName;
	private String assessorAvatarUrl;
	private long assessorId;
	private long credentialId;
	private long dateRequested;
	private long dateQuit;
	private String credentialTitle;
	private boolean approved;
	private long dateSubmitted;
	private long assessmentId;
	private String encodedAssessmentId;
	private String encodedCredentialId;
	private int totalNumberOfMessages;
	private String initials;
	private AssessmentType type;
	private BlindAssessmentMode blindAssessmentMode = BlindAssessmentMode.OFF;
	private AssessmentStatus status;

//	public static AssessmentData fromAssessment(CredentialAssessment assessment, UrlIdEncoder encoder, DateFormat dateFormat) {
//		AssessmentData data = new AssessmentData();
//		data.setStatus(assessment.getStatus());
//		data.setStudentFullName(assessment.getStudent().getName()+" "+assessment.getStudent().getLastname());
//		data.setStudentAvatarUrl(AvatarUtils.getAvatarUrlInFormat(assessment.getStudent(), ImageFormat.size120x120));
//		data.setStudentId(assessment.getStudent().getId());
//		if (assessment.getAssessor() != null) {
//			data.setAssessorFullName(assessment.getAssessor().getName()+" "+assessment.getAssessor().getLastname());
//			data.setAssessorAvatarUrl(AvatarUtils.getAvatarUrlInFormat(assessment.getAssessor(), ImageFormat.size120x120));
//			data.setAssessorId(assessment.getAssessor().getId());
//		}
//		data.setDateRequested(DateUtil.getMillisFromDate(assessment.getDateCreated()));
//
//		data.setDateQuit(DateUtil.getMillisFromDate(assessment.getQuitDate()));
//		data.setCredentialTitle(assessment.getTargetCredential().getCredential().getCredentialTitle());
//		data.setApproved(assessment.isApproved());
//		data.setDateSubmitted(DateUtil.getMillisFromDate(assessment.getDateApproved()));
//		data.setEncodedAssessmentId(encoder.encodeId(assessment.getId()));
//		data.setEncodedCredentialId(encoder.encodeId(assessment.getTargetCredential().getCredential().getId()));
//		data.setCredentialId(assessment.getTargetCredential().getCredential().getId());
//		data.setInitials(getInitialsFromName(data.getStudentFullName()));
//		data.setBlindAssessmentMode(assessment.getBlindAssessmentMode());
//		return data;
//	}

	public void markAssessmentAsSubmitted() {
		this.approved = true;
		this.status = AssessmentStatus.SUBMITTED;
	}

	public String getStudentFullName() {
		return studentFullName;
	}

	public void setStudentFullName(String studentFullName) {
		this.studentFullName = studentFullName;
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

	public long getDateRequested() {
		return dateRequested;
	}

	public void setDateRequested(long dateRequested) {
		this.dateRequested = dateRequested;
	}

	public long getDateQuit() {
		return dateQuit;
	}

	public void setDateQuit(long dateQuit) {
		this.dateQuit = dateQuit;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public String getEncodedCredentialId() {
		return encodedCredentialId;
	}

	public void setEncodedCredentialId(String encodedCredentialId) {
		this.encodedCredentialId = encodedCredentialId;
	}

	public String getEncodedAssessmentId() {
		return encodedAssessmentId;
	}

	public void setEncodedAssessmentId(String encodedAssessmentId) {
		this.encodedAssessmentId = encodedAssessmentId;
	}

	public int getTotalNumberOfMessages() {
		return totalNumberOfMessages;
	}

	public void setTotalNumberOfMessages(int totalNumberOfMessages) {
		this.totalNumberOfMessages = totalNumberOfMessages;
	}

	public String getStudentAvatarUrl() {
		return studentAvatarUrl;
	}

	public void setStudentAvatarUrl(String studentAvatarUrl) {
		this.studentAvatarUrl = studentAvatarUrl;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public AssessmentType getType() {
		return type;
	}

	public void setType(AssessmentType type) {
		this.type = type;
	}

	private static String getInitialsFromName(String fullname) {
		if(fullname != null && fullname.length() >= 2) {
			String[] firstAndLastName = fullname.split(" ");
			//if we only have name or last name, return first two characters uppercased
			if(firstAndLastName.length == 1) {
				return fullname.substring(0, 1).toUpperCase();
			}
			else return (firstAndLastName[0].charAt(0) + "" + firstAndLastName[1].charAt(0)).toUpperCase();
		}
		else {
			return "N/A";
		}
	}

	public long getAssessmentId() {
		return assessmentId;
	}

	public void setAssessmentId(long assessmentId) {
		this.assessmentId = assessmentId;
	}

	public long getAssessorId() {
		return assessorId;
	}

	public void setAssessorId(long assessorId) {
		this.assessorId = assessorId;
	}

	public long getStudentId() {
		return studentId;
	}

	public void setStudentId(long studentId) {
		this.studentId = studentId;
	}

	public void setBlindAssessmentMode(BlindAssessmentMode blindAssessmentMode) {
		this.blindAssessmentMode = blindAssessmentMode;
	}

	public BlindAssessmentMode getBlindAssessmentMode() {
		return blindAssessmentMode;
	}

	public AssessmentStatus getStatus() {
		return status;
	}

	public void setStatus(AssessmentStatus status) {
		this.status = status;
	}

	public long getDateSubmitted() {
		return dateSubmitted;
	}

	public void setDateSubmitted(long dateSubmitted) {
		this.dateSubmitted = dateSubmitted;
	}

	public long getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(long credentialId) {
		this.credentialId = credentialId;
	}
}
