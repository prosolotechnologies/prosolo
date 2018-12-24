package org.prosolo.services.assessment.config;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-05-08
 * @since 1.2.0
 */
public class AssessmentLoadConfig implements Serializable {

    private static final long serialVersionUID = -2661949076610529865L;

    private final boolean loadDiscussion;
    private final boolean loadDataIfAssessmentNotApproved;

    private AssessmentLoadConfig(boolean loadDiscussion, boolean loadDataIfStudentDisabledAssessmentDisplay, boolean loadDataIfAssessmentNotApproved) {
        this.loadDiscussion = loadDiscussion;
        this.loadDataIfAssessmentNotApproved = loadDataIfAssessmentNotApproved;
    }

    public static AssessmentLoadConfig of(boolean loadDiscussion, boolean loadDataIfStudentDisabledAssessmentDisplay, boolean loadDataIfAssessmentNotApproved) {
        return new AssessmentLoadConfig(loadDiscussion, loadDataIfStudentDisabledAssessmentDisplay, loadDataIfAssessmentNotApproved);
    }

    public boolean isLoadDiscussion() {
        return loadDiscussion;
    }

    public boolean isLoadDataIfAssessmentNotApproved() {
        return loadDataIfAssessmentNotApproved;
    }
}
