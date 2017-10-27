package org.prosolo.services.nodes.data.assessments;

import org.prosolo.services.nodes.data.ActivityType;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2017-10-26
 * @since 1.1.0
 */
public class ActivityAssessmentsSummaryData implements Serializable {

    private static final long serialVersionUID = 8695166412592369031L;

    private long id;
    private String title;
    private ActivityType activityType;
    private long numberOfStudentsCompletedActivity;
    private long numberOfAssessedStudents;
    private boolean gradingEnabled;

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

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public long getNumberOfStudentsCompletedActivity() {
        return numberOfStudentsCompletedActivity;
    }

    public void setNumberOfStudentsCompletedActivity(long numberOfStudentsCompletedActivity) {
        this.numberOfStudentsCompletedActivity = numberOfStudentsCompletedActivity;
    }

    public long getNumberOfAssessedStudents() {
        return numberOfAssessedStudents;
    }

    public void setNumberOfAssessedStudents(long numberOfAssessedStudents) {
        this.numberOfAssessedStudents = numberOfAssessedStudents;
    }

    public boolean isGradingEnabled() {
        return gradingEnabled;
    }

    public void setGradingEnabled(boolean gradingEnabled) {
        this.gradingEnabled = gradingEnabled;
    }
}
