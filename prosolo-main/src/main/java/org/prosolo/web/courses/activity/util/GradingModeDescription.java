package org.prosolo.web.courses.activity.util;

import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.domainmodel.credential.GradingMode;

/**
 * @author stefanvuckovic
 * @date 2017-10-09
 * @since 1.0.0
 */
public enum GradingModeDescription {

    NONGRADED(GradingMode.NONGRADED, "Nongraded"),
    AUTOMATIC(GradingMode.AUTOMATIC, "Automatic"),
    ALWAYS(GradingMode.MANUAL, "Manual");

    private GradingMode gradingMode;
    private String description;

    GradingModeDescription(GradingMode gradingMode, String description) {
        this.gradingMode = gradingMode;
        this.description = description;
    }

    public GradingMode getGradingMode() {
        return gradingMode;
    }

    public String getDescription() {
        return description;
    }
}
