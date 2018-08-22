package org.prosolo.services.assessment.data;

import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.util.Pair;
import org.prosolo.services.assessment.data.grading.AssessmentGradeSummary;
import org.prosolo.services.common.observable.StandardObservable;

/**
 * @author stefanvuckovic
 * @date 2018-01-23
 * @since 1.2.0
 */
public class AssessmentTypeConfig extends StandardObservable {

    private long id;
    private AssessmentType type;
    private boolean enabled;
    private boolean mandatory;
    private AssessmentGradeSummary gradeSummary;
    private BlindAssessmentMode blindAssessmentMode = BlindAssessmentMode.OFF;

    public AssessmentTypeConfig(long id, AssessmentType type, boolean enabled, boolean mandatory) {
        this.id = id;
        this.type = type;
        this.enabled = enabled;
        this.mandatory = mandatory;
    }

    public AssessmentTypeConfig(long id, AssessmentType type, boolean enabled, boolean mandatory, BlindAssessmentMode blindAssessmentMode) {
        this(id, type, enabled, mandatory);
        this.blindAssessmentMode = blindAssessmentMode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public AssessmentType getType() {
        return type;
    }

    public void setType(AssessmentType type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        observeAttributeChange("enabled", this.enabled, enabled);
        this.enabled = enabled;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public AssessmentGradeSummary getGradeSummary() {
        return gradeSummary;
    }

    public void setGradeSummary(AssessmentGradeSummary gradeSummary) {
        this.gradeSummary = gradeSummary;
    }

    public BlindAssessmentMode getBlindAssessmentMode() {
        return blindAssessmentMode;
    }

    public void setBlindAssessmentMode(BlindAssessmentMode blindAssessmentMode) {
        observeAttributeChange("blindAssessmentMode", this.blindAssessmentMode, blindAssessmentMode);
        this.blindAssessmentMode = blindAssessmentMode;
    }
}
