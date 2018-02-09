package org.prosolo.services.assessment.data;

import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.services.common.observable.StandardObservable;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-01-23
 * @since 1.2.0
 */
public class LearningResourceAssessmentSettings extends StandardObservable implements Serializable {

    private static final long serialVersionUID = 59268515362688786L;

    private GradingMode gradingMode;
    private int maxPoints;
    private String maxPointsString = ""; // needed because field can also be empty on the html page
    private long rubricId;
    private String rubricName;
    private RubricType rubricType;

    public LearningResourceAssessmentSettings() {}

    public LearningResourceAssessmentSettings(GradingMode gradingMode, int maxPoints, long rubricId, String rubricName, RubricType rubricType) {
        this.gradingMode = gradingMode;
        this.maxPoints = maxPoints;
        this.rubricId = rubricId;
        this.rubricName = rubricName;
        this.rubricType = rubricType;
    }

    public GradingMode getGradingMode() {
        return gradingMode;
    }

    public void setGradingMode(GradingMode gradingMode) {
        observeAttributeChange("gradingMode", this.gradingMode, gradingMode);
        this.gradingMode = gradingMode;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public String getMaxPointsString() {
        return maxPointsString;
    }

    public void setMaxPointsString(String maxPointsString) {
        observeAttributeChange("maxPointsString", this.maxPointsString, maxPointsString);
        this.maxPointsString = maxPointsString;
    }

    public long getRubricId() {
        return rubricId;
    }

    public void setRubricId(long rubricId) {
        observeAttributeChange("rubricId", this.rubricId, rubricId);
        this.rubricId = rubricId;
    }

    public String getRubricName() {
        return rubricName;
    }

    public void setRubricName(String rubricName) {
        this.rubricName = rubricName;
    }

    public RubricType getRubricType() {
        return rubricType;
    }

    public void setRubricType(RubricType rubricType) {
        this.rubricType = rubricType;
    }

    public boolean isRubricChanged() {
        return changedAttributes.containsKey("rubricId");
    }
}
