package org.prosolo.web.courses.competence.util;

import org.prosolo.common.domainmodel.credential.LearningPathType;

/**
 * @author stefanvuckovic
 * @date 2017-11-23
 * @since 1.2.0
 */
public enum LearningPathDescription {

    ACTIVITY(LearningPathType.ACTIVITY, "By completing activities"),
    EVIDENCE(LearningPathType.EVIDENCE, "By providing pieces of evidence");

    private LearningPathType learningPathType;
    private String description;

    LearningPathDescription(LearningPathType learningPathType, String description) {
        this.learningPathType = learningPathType;
        this.description = description;
    }

    public LearningPathType getLearningPathType() {
        return learningPathType;
    }

    public String getDescription() {
        return description;
    }
}
