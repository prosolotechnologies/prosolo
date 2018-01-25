package org.prosolo.services.nodes.data;

import org.prosolo.common.domainmodel.assessment.AssessmentType;
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

    public AssessmentTypeConfig(long id, AssessmentType type, boolean enabled, boolean mandatory) {
        this.id = id;
        this.type = type;
        this.enabled = enabled;
        this.mandatory = mandatory;
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
}
