package org.prosolo.web.assessments;

/**
 * @author stefanvuckovic
 * @date 2018-02-28
 * @since 1.2.0
 */
public enum AssessmentFilter {

    NOTIFIED(org.prosolo.services.assessment.data.AssessmentFilter.NOTIFIED, "Requested"),
    NOT_ASSESSED(org.prosolo.services.assessment.data.AssessmentFilter.NOT_ASSESSED, "Not Assessed"),
    ASSESSED(org.prosolo.services.assessment.data.AssessmentFilter.ASSESSED, "Assessed");

    private org.prosolo.services.assessment.data.AssessmentFilter filter;
    private String label;

    AssessmentFilter(org.prosolo.services.assessment.data.AssessmentFilter filter, String label) {
        this.filter = filter;
        this.label = label;
    }

    public org.prosolo.services.assessment.data.AssessmentFilter getFilter() {
        return filter;
    }

    public String getLabel() {
        return label;
    }
}
