package org.prosolo.services.nodes.data.assessments;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class CompetenceAssessmentData {
	
	private String title;
	private boolean approved;
	private long competenceAssessmentId;
	private String competenceAssessmentEncodedId;
	private long competenceId;
	private long targetCompetenceId;
	private List<ActivityAssessmentData> activityAssessmentData;
	private int points;
	private int maxPoints;
	//if true, activity assessments can't be graded and messages can't be posted
	private boolean readOnly;

//	public static CompetenceAssessmentData from(CompetenceAssessment compAssessment, UrlIdEncoder encoder,
//			long userId, DateFormat dateFormat) {
//
//		CompetenceAssessmentData data = new CompetenceAssessmentData();
//		data.setTitle(compAssessment.getTargetCompetence().getCompetence().getTitle());
//		data.setCompetenceAssessmentId(compAssessment.getId());
//		data.setCompetenceAssessmentEncodedId(encoder.encodeId(compAssessment.getId()));
//		data.setApproved(compAssessment.isApproved());
//		data.setPoints(compAssessment.getPoints());
//
//		List<TargetActivity1> targetActivities = compAssessment.getTargetCompetence().getTargetActivities();
//
//		int maxPoints = 0;
//		List<ActivityAssessmentData> activityAssessmentData = new ArrayList<>();
//		for (TargetActivity1 targetActivity : targetActivities) {
//			ActivityAssessmentData assessmentData = ActivityAssessmentData.from(targetActivity, compAssessment, encoder, userId);
//			maxPoints += assessmentData.getGrade().getMaxGrade();
//			activityAssessmentData.add(assessmentData);
//		}
//		data.setMaxPoints(maxPoints);
//		data.setActivityAssessmentData(activityAssessmentData);
//
//		return data;
//	}

	public static CompetenceAssessmentData from(CompetenceData1 cd, CredentialAssessment credAssessment,
				UrlIdEncoder encoder, long userId, DateFormat dateFormat) {

		CompetenceAssessmentData data = new CompetenceAssessmentData();
		data.setTitle(cd.getTitle());
		data.setCompetenceId(cd.getCompetenceId());
		data.setTargetCompetenceId(cd.getTargetCompId());
		CompetenceAssessment compAssessment = credAssessment.getCompetenceAssessmentByCompetenceId(cd.getCompetenceId());
		if (compAssessment != null) {
			data.setCompetenceAssessmentId(compAssessment.getId());
			data.setCompetenceAssessmentEncodedId(encoder.encodeId(compAssessment.getId()));
			data.setApproved(compAssessment.isApproved());
			data.setPoints(compAssessment.getPoints());
		} else if (!cd.isEnrolled()) {
			//if user is not enrolled in a competency readOnly should be set to true
			data.setReadOnly(true);
		}

		int maxPoints = 0;
		List<ActivityAssessmentData> activityAssessmentData = new ArrayList<>();
		for (ActivityData ad : cd.getActivities()) {
			ActivityAssessmentData assessmentData = ActivityAssessmentData.from(ad, compAssessment, encoder, userId);
			maxPoints += assessmentData.getGrade().getMaxGrade();
			activityAssessmentData.add(assessmentData);
		}
		data.setMaxPoints(maxPoints);
		data.setActivityAssessmentData(activityAssessmentData);

		return data;
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

	public long getCompetenceAssessmentId() {
		return competenceAssessmentId;
	}

	public void setCompetenceAssessmentId(long competenceAssessmentId) {
		this.competenceAssessmentId = competenceAssessmentId;
	}

	public String getCompetenceAssessmentEncodedId() {
		return competenceAssessmentEncodedId;
	}

	public void setCompetenceAssessmentEncodedId(String competenceAssessmentEncodedId) {
		this.competenceAssessmentEncodedId = competenceAssessmentEncodedId;
	}

	public List<ActivityAssessmentData> getActivityAssessmentData() {
		return activityAssessmentData;
	}

	public void setActivityAssessmentData(List<ActivityAssessmentData> activityAssessmentData) {
		this.activityAssessmentData = activityAssessmentData;
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

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public long getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(long competenceId) {
		this.competenceId = competenceId;
	}

	public long getTargetCompetenceId() {
		return targetCompetenceId;
	}

	public void setTargetCompetenceId(long targetCompetenceId) {
		this.targetCompetenceId = targetCompetenceId;
	}
}
