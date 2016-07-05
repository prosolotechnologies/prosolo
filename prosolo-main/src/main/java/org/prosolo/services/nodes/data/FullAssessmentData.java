package org.prosolo.services.nodes.data;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.AvatarUtils;

public class FullAssessmentData {

	private String studentFullName;
	private String studentAvatarUrl;
	private long assessorId;
	private long assessedStrudentId;
	private String dateValue;
	private String title;
	private boolean approved;
	private String encodedId;
	private String initials;
	private boolean mandatoryFlow;
	private long duration;
	private long targetCredentialId;
	private long credentialId;

	private List<CompetenceAssessmentData> competenceAssessmentData;

	public static FullAssessmentData fromAssessment(CredentialAssessment assessment, UrlIdEncoder encoder,
			User user, DateFormat dateFormat) {
		
		FullAssessmentData data = new FullAssessmentData();
		data.setStudentFullName(assessment.getAssessedStudent().getName()+" "+assessment.getAssessedStudent().getLastname());
		data.setStudentAvatarUrl(AvatarUtils.getAvatarUrlInFormat(assessment.getAssessedStudent(), ImageFormat.size34x34));
		data.setDateValue(dateFormat.format(assessment.getDateCreated()));
		data.setTitle(assessment.getTargetCredential().getTitle());
		data.setApproved(assessment.isApproved());
		data.setCredentialId(assessment.getTargetCredential().getCredential().getId());
		data.setEncodedId(encoder.encodeId(assessment.getId()));
		data.setMandatoryFlow(assessment.getTargetCredential().isCompetenceOrderMandatory());
		data.setDuration(assessment.getTargetCredential().getDuration());
		data.setTargetCredentialId(assessment.getTargetCredential().getId());
		data.setAssessedStrudentId(assessment.getAssessedStudent().getId());
		data.setAssessorId(assessment.getAssessor().getId());
		
		List<CompetenceAssessmentData> compDatas = new ArrayList<>();
		for(CompetenceAssessment compAssessment : assessment.getCompetenceAssessments()) {
			CompetenceAssessmentData compData = CompetenceAssessmentData.from(compAssessment,encoder, user, dateFormat);
			compDatas.add(compData);
		}
		data.setCompetenceAssessmentData(compDatas);
		data.setInitials(getInitialsFromName(data.getStudentFullName()));
		return data;
		
	}

	private static String getInitialsFromName(String fullname) {
		if (fullname != null && fullname.length() >= 2) {
			String[] firstAndLastName = fullname.split(" ");
			// if we only have name or last name, return first two characters
			// uppercased
			if (firstAndLastName.length == 1) {
				return fullname.substring(0, 1).toUpperCase();
			} else
				return (firstAndLastName[0].charAt(0) + "" + firstAndLastName[1].charAt(0)).toUpperCase();
		} else {
			return "N/A";
		}
	}

	public String getStudentFullName() {
		return studentFullName;
	}

	public void setStudentFullName(String studentFullName) {
		this.studentFullName = studentFullName;
	}

	public String getStudentAvatarUrl() {
		return studentAvatarUrl;
	}

	public void setStudentAvatarUrl(String studentAvatarUrl) {
		this.studentAvatarUrl = studentAvatarUrl;
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

	public String getEncodedId() {
		return encodedId;
	}

	public void setEncodedId(String encodedId) {
		this.encodedId = encodedId;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public List<CompetenceAssessmentData> getCompetenceAssessmentData() {
		return competenceAssessmentData;
	}

	public void setCompetenceAssessmentData(List<CompetenceAssessmentData> competenceAssessmentData) {
		this.competenceAssessmentData = competenceAssessmentData;
	}

	public boolean isMandatoryFlow() {
		return mandatoryFlow;
	}

	public void setMandatoryFlow(boolean mandatoryFlow) {
		this.mandatoryFlow = mandatoryFlow;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getTargetCredentialId() {
		return targetCredentialId;
	}

	public void setTargetCredentialId(long targetCredentialId) {
		this.targetCredentialId = targetCredentialId;
	}

	public long getAssessorId() {
		return assessorId;
	}

	public void setAssessorId(long assessorId) {
		this.assessorId = assessorId;
	}

	public long getAssessedStrudentId() {
		return assessedStrudentId;
	}

	public void setAssessedStrudentId(long assessedStrudentId) {
		this.assessedStrudentId = assessedStrudentId;
	}

	public long getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(long credentialId) {
		this.credentialId = credentialId;
	}
	
}