package org.prosolo.services.nodes.data.competence;

import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.services.nodes.util.TimeUtil;

import java.io.Serializable;

/**
 * 
 * @author "Musa Paljos"
 *
 */

public class TargetCompetenceData implements Serializable {

    private static final long serialVersionUID = 3744828592870425737L;

    private Long id;
    private String description;
    private String title;
    private long duration;
    private String durationString;
    private LearningResourceType learningResourceType;
    private long competenceId;
    private int numberOfAssessments;

    public void calculateDurationString() {
        durationString = TimeUtil.getHoursAndMinutesInString(this.duration);
    }

    public TargetCompetenceData(TargetCompetence1 targetCompetence1, int numberOfAssessments) {
        this.id = targetCompetence1.getId();
        this.description = targetCompetence1.getCompetence().getDescription();
        this.title = targetCompetence1.getCompetence().getTitle();
        this.duration = targetCompetence1.getCompetence().getDuration();
        this.learningResourceType = targetCompetence1.getCompetence().getType();
        this.competenceId = targetCompetence1.getCompetence().getId();
        calculateDurationString();
        this.numberOfAssessments = numberOfAssessments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public LearningResourceType getLearningResourceType() {
        return learningResourceType;
    }

    public void setLearningResourceType(LearningResourceType learningResourceType) {
        this.learningResourceType = learningResourceType;
    }
    
    public boolean madeByUniversity() {
        return learningResourceType.equals(LearningResourceType.UNIVERSITY_CREATED);
    }

    public long getCompetenceId() {
        return competenceId;
    }

    public void setCompetenceId(long competenceId) {
        this.competenceId = competenceId;
    }

    public String getDurationString() {
        return durationString;
    }

    public void setDurationString(String durationString) {
        this.durationString = durationString;
    }

    public int getNumberOfAssessments() {
        return numberOfAssessments;
    }
}