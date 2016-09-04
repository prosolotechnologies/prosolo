package org.prosolo.services.nodes.data;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class CompetenceAssessmentData {
	
	private String title;
	private boolean approved;
	private long competenceAssessmentId;
	private String competenceAssessmentEncodedId;
	private List<ActivityAssessmentData> activityAssessmentData;

	public static CompetenceAssessmentData from(CompetenceAssessment compAssessment, UrlIdEncoder encoder,
			long userId, DateFormat dateFormat) {
		
		CompetenceAssessmentData data = new CompetenceAssessmentData();
		data.setTitle(compAssessment.getTitle());
		data.setCompetenceAssessmentId(compAssessment.getId());
		data.setCompetenceAssessmentEncodedId(encoder.encodeId(compAssessment.getId()));
		data.setApproved(compAssessment.isApproved());
		List<TargetActivity1> targetActivities = compAssessment.getTargetCompetence().getTargetActivities();
		List<ActivityAssessmentData> activityAssessmentData = new ArrayList<>();
		for (TargetActivity1 targetActivity : targetActivities) {
			ActivityAssessmentData assessmentData = ActivityAssessmentData.from(targetActivity, compAssessment, encoder, userId);
			activityAssessmentData.add(assessmentData);
		}
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
	
	

}
