package org.prosolo.services.assessment.data;

import org.prosolo.common.domainmodel.credential.ActivityResultType;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.ActivityType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private ActivityResultType resultType;
    private long numberOfStudentsCompletedActivity;
    private long numberOfAssessedStudents;
    private boolean gradingEnabled;

    private List<ActivityResultData> studentResults = new ArrayList<>();

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

    public List<ActivityResultData> getStudentResults() {
        return studentResults;
    }

    public void setStudentResults(List<ActivityResultData> studentResults) {
        this.studentResults = studentResults;
    }

    public ActivityResultType getResultType() {
        return resultType;
    }

    public void setResultType(ActivityResultType resultType) {
        this.resultType = resultType;
    }
}
