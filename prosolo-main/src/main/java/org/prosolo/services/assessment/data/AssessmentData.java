package org.prosolo.services.assessment.data;

import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.AvatarUtils;

import java.text.DateFormat;
import java.util.OptionalInt;

public class AssessmentData {

	private String studentFullName;
	private String studentAvatarUrl;
	private String assessorFullName;
	private String assessorAvatarUrl;
	private String dateValue;
	private String title;
	private boolean approved;
	private String encodedAssessmentId;
	private String encodedCredentialId;
	private int totalNumberOfMessages;
	private String initials;
	private AssessmentType type;

	public static AssessmentData fromAssessment(CredentialAssessment assessment, UrlIdEncoder encoder, DateFormat dateFormat) {
		AssessmentData data = new AssessmentData();
		data.setStudentFullName(assessment.getAssessedStudent().getName()+" "+assessment.getAssessedStudent().getLastname());
		data.setStudentAvatarUrl(AvatarUtils.getAvatarUrlInFormat(assessment.getAssessedStudent(), ImageFormat.size120x120));
		if (assessment.getAssessor() != null) {
			data.setAssessorFullName(assessment.getAssessor().getName()+" "+assessment.getAssessor().getLastname());
			data.setAssessorAvatarUrl(AvatarUtils.getAvatarUrlInFormat(assessment.getAssessor(), ImageFormat.size120x120));
		}
		data.setDateValue(dateFormat.format(assessment.getDateCreated()));
		data.setTitle(assessment.getTargetCredential().getCredential().getTitle());
		data.setApproved(assessment.isApproved());
		data.setEncodedAssessmentId(encoder.encodeId(assessment.getId()));
		data.setEncodedCredentialId(encoder.encodeId(assessment.getTargetCredential().getCredential().getId()));
		//TODO optimize, denormalize?
		OptionalInt number = assessment.getCompetenceAssessments().stream()
			.map(competenceAssessment -> competenceAssessment.getCompetenceAssessment().getActivityDiscussions())
			.flatMap(discussions -> discussions.stream())
			.mapToInt(discussion -> discussion.getMessages().size())
			.reduce(Integer::sum);
		data.setTotalNumberOfMessages(number.orElse(0));
		data.setInitials(getInitialsFromName(data.getStudentFullName()));
		return data;
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

	public String getDateValue() {
		return dateValue;
	}

	public void setDateValue(String dateValue) {
		this.dateValue = dateValue;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

}
