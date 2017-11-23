package org.prosolo.services.nodes.data;

import org.prosolo.services.nodes.data.organization.LearningStageData;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2017-11-16
 * @since 1.2.0
 */
public class LearningResourceLearningStage implements Serializable {

    private static final long serialVersionUID = -1899895007818344491L;

    private final LearningStageData learningStage;
    //if this id is greater than zero, it means that learning resource already exists for this learning stage
    private final long learningResourceId;
    //flag that indicates whether learning resource can be created for this learning stage
    private final boolean canBeCreated;

    public LearningResourceLearningStage(LearningStageData ls, long learningResourceId, boolean canBeCreated) {
        this.learningStage = ls;
        this.learningResourceId = learningResourceId;
        this.canBeCreated = canBeCreated;
    }

    public LearningStageData getLearningStage() {
        return learningStage;
    }

    public long getLearningResourceId() {
        return learningResourceId;
    }

    public boolean isCanBeCreated() {
        return canBeCreated;
    }

}
