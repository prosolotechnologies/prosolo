package org.prosolo.services.nodes.data.assessments;

import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.urlencoding.UrlIdEncoder;

import java.util.ArrayList;
import java.util.List;

public class CompetenceAssessmentData {
	
	private String title;
	private boolean approved;
	private long competenceAssessmentId;
	private String competenceAssessmentEncodedId;
	private long competenceId;
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
				UrlIdEncoder encoder, long userId) {

		CompetenceAssessmentData data = new CompetenceAssessmentData();
		data.setTitle(cd.getTitle());
		data.setCompetenceId(cd.getCompetenceId());
		CompetenceAssessment compAssessment = credAssessment.getCompetenceAssessmentByCompetenceId(cd.getCompetenceId());
		data.setCompetenceAssessmentId(compAssessment.getId());
		data.setCompetenceAssessmentEncodedId(encoder.encodeId(compAssessment.getId()));
		data.setApproved(compAssessment.isApproved());
		data.setPoints(compAssessment.getPoints());
		if (!cd.isEnrolled()) {
			data.setReadOnly(true);
		}

		int maxPoints = 0;
		List<ActivityAssessmentData> activityAssessmentData = new ArrayList<>();
		for (ActivityData ad : cd.getActivities()) {
			ActivityAssessmentData assessmentData = ActivityAssessmentData.from(ad, compAssessment,
					credAssessment, encoder, userId);
			maxPoints += assessmentData.getGrade().getMaxGrade();
			assessmentData.setCompAssessment(data);
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

}
