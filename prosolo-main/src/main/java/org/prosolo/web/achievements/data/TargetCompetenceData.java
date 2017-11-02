package org.prosolo.web.achievements.data;

import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.services.nodes.data.CompetenceData1;

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
    private boolean hiddenFromProfile;
    private long duration;
    private LearningResourceType learningResourceType;
    private long competenceId;

    public TargetCompetenceData(Long id, String description, String title, boolean hiddenFromProfile,
            long duration, LearningResourceType learningResourceType, 
            long competenceId) {
        this.id = id;
        this.description = description;
        this.title = title;
        this.hiddenFromProfile = hiddenFromProfile;
        this.duration = duration;
        this.learningResourceType = learningResourceType;
        this.competenceId = competenceId;
    }

    public TargetCompetenceData(TargetCompetence1 targetCompetence1){
        this.id = targetCompetence1.getId();
        this.description = targetCompetence1.getCompetence().getDescription();
        this.title = targetCompetence1.getCompetence().getTitle();
        this.hiddenFromProfile = targetCompetence1.isHiddenFromProfile();
        this.duration = targetCompetence1.getCompetence().getDuration();
        this.learningResourceType = targetCompetence1.getCompetence().getType();
        this.competenceId = targetCompetence1.getCompetence().getId();
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

    public boolean isHiddenFromProfile() {
        return hiddenFromProfile;
    }

    public void setHiddenFromProfile(boolean hiddenFromProfile) {
        this.hiddenFromProfile = hiddenFromProfile;
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

}