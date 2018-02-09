package org.prosolo.services.nodes.data.assessments;

import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.util.TimeUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.AvatarUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class AssessmentDataFull {

	private long credAssessmentId;
	private String message;
	private String studentFullName;
	private String studentAvatarUrl;
	private String assessorFullName;
	private String assessorAvatarUrl;
	private long assessorId;
	private long assessedStrudentId;
	private String dateValue;
	private String title;
	private boolean approved;
	private String encodedId;
	private String initials;
	private boolean mandatoryFlow;
	private long duration;
	private String durationString;
	private long targetCredentialId;
	private long credentialId;
	private AssessmentType type;
	private int points;
	private int maxPoints;

	private List<CompetenceAssessmentData> competenceAssessmentData;

	public static AssessmentDataFull fromAssessment(CredentialAssessment assessment, int credAssessmentPoints, List<CompetenceData1> userComps,
				UrlIdEncoder encoder, long userId, DateFormat dateFormat) {
		AssessmentDataFull data = new AssessmentDataFull();
		data.setCredAssessmentId(assessment.getId());
		data.setMessage(assessment.getMessage());
		data.setAssessedStrudentId(assessment.getAssessedStudent().getId());
		data.setStudentFullName(assessment.getAssessedStudent().getName()+" "+assessment.getAssessedStudent().getLastname());
		data.setStudentAvatarUrl(AvatarUtils.getAvatarUrlInFormat(assessment.getAssessedStudent(), ImageFormat.size120x120));
		if (assessment.getAssessor() != null) {
			data.setAssessorFullName(assessment.getAssessor().getName()+" "+assessment.getAssessor().getLastname());
			data.setAssessorAvatarUrl(AvatarUtils.getAvatarUrlInFormat(assessment.getAssessor(), ImageFormat.size120x120));
			data.setAssessorId(assessment.getAssessor().getId());
		}
		data.setDateValue(dateFormat.format(assessment.getDateCreated()));
		data.setTitle(assessment.getTargetCredential().getCredential().getTitle());
		data.setApproved(assessment.isApproved());
		data.setCredentialId(assessment.getTargetCredential().getCredential().getId());
		data.setEncodedId(encoder.encodeId(assessment.getId()));
		data.setMandatoryFlow(assessment.getTargetCredential().getCredential().isCompetenceOrderMandatory());
		data.setDuration(assessment.getTargetCredential().getCredential().getDuration());
		data.calculateDurationString();
		data.setTargetCredentialId(assessment.getTargetCredential().getId());
		data.setType(assessment.getType());
		data.setPoints(credAssessmentPoints);

		int maxPoints = 0;
		List<CompetenceAssessmentData> compDatas = new ArrayList<>();
		for (CompetenceData1 compData : userComps) {
			CompetenceAssessmentData cas = CompetenceAssessmentData.from(compData, assessment, encoder, userId);
			maxPoints += cas.getMaxPoints();
			compDatas.add(cas);
		}
		data.setMaxPoints(maxPoints);
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

	public void calculateDurationString() {
		durationString = TimeUtil.getHoursAndMinutesInString(this.duration);
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
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

	public AssessmentType getType() {
		return type;
	}

	public void setType(AssessmentType type) {
		this.type = type;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public int getMaxPoints() {
		return maxPoints;
	}

	public void setMaxPoints(int maxPoints) {
		this.maxPoints = maxPoints;
	}

	public CompetenceAssessmentData findCompetenceAssessmentData(long compAssessmentId) {
		for (CompetenceAssessmentData compAssessment : competenceAssessmentData) {
			if (compAssessment.getCompetenceAssessmentId() == compAssessmentId) {
				return compAssessment;
			}
		}
		return null;
	}

	public String getDurationString() {
		return durationString;
	}

	public void setDurationString(String durationString) {
		this.durationString = durationString;
	}

	public long getCredAssessmentId() {
		return credAssessmentId;
	}

	public void setCredAssessmentId(long credAssessmentId) {
		this.credAssessmentId = credAssessmentId;
	}

}
