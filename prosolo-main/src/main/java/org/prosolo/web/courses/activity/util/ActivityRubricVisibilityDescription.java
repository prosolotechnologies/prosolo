package org.prosolo.web.courses.activity.util;

import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;

/**
 * @author stefanvuckovic
 * @date 2017-09-29
 * @since 1.0.0
 */
public enum ActivityRubricVisibilityDescription {
    NEVER(ActivityRubricVisibility.NEVER, "Don't display rubrics to students"),
    AFTER_SUBMISSION(ActivityRubricVisibility.AFTER_SUBMISSION, "Display rubrics to students after their result submission"),
    ALWAYS(ActivityRubricVisibility.ALWAYS, "Display rubrics to students all the time");


    private ActivityRubricVisibility visibility;
    private String description;

    ActivityRubricVisibilityDescription(ActivityRubricVisibility visibility, String description) {
        this.visibility = visibility;
        this.description = description;
    }

    public ActivityRubricVisibility getVisibility() {
        return visibility;
    }

    public String getDescription() {
        return description;
    }
}
