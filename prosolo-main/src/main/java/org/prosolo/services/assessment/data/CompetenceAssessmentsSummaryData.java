package org.prosolo.services.assessment.data;

import org.prosolo.search.impl.PaginatedResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-10-26
 * @since 1.1.0
 */
public class CompetenceAssessmentsSummaryData implements Serializable {

    private static final long serialVersionUID = 8332256225462068436L;

    private long id;
    private String title;
    private long numberOfEnrolledStudents;
    private long numberOfAssessedStudents;
    private long numberOfNotifications;
    private boolean gradingEnabled;
    private List<ActivityAssessmentsSummaryData> activitiesAssessmentSummaryData;
    private PaginatedResult<CompetenceAssessmentData> assessments;

    public CompetenceAssessmentsSummaryData() {
        this.activitiesAssessmentSummaryData = new ArrayList<>();
    }

    public void addActivitySummary(ActivityAssessmentsSummaryData actSummary) {
        activitiesAssessmentSummaryData.add(actSummary);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ActivityAssessmentsSummaryData> getActivitiesAssessmentSummaryData() {
        return activitiesAssessmentSummaryData;
    }

    public void setActivitiesAssessmentSummaryData(List<ActivityAssessmentsSummaryData> activitiesAssessmentSummaryData) {
        this.activitiesAssessmentSummaryData = activitiesAssessmentSummaryData;
    }

    public long getNumberOfAssessedStudents() {
        return numberOfAssessedStudents;
    }

    public void setNumberOfAssessedStudents(long numberOfAssessedStudents) {
        this.numberOfAssessedStudents = numberOfAssessedStudents;
    }

    public long getNumberOfEnrolledStudents() {
        return numberOfEnrolledStudents;
    }

    public void setNumberOfEnrolledStudents(long numberOfEnrolledStudents) {
        this.numberOfEnrolledStudents = numberOfEnrolledStudents;
    }

    public long getNumberOfNotifications() {
        return numberOfNotifications;
    }

    public void setNumberOfNotifications(long numberOfNotifications) {
        this.numberOfNotifications = numberOfNotifications;
    }

    public void setGradingEnabled(boolean gradingEnabled) {
        this.gradingEnabled = gradingEnabled;
    }

    public boolean isGradingEnabled() {
        return gradingEnabled;
    }

    public PaginatedResult<CompetenceAssessmentData> getAssessments() {
        return assessments;
    }

    public void setAssessments(PaginatedResult<CompetenceAssessmentData> assessments) {
        this.assessments = assessments;
    }
}
