package org.prosolo.web.assessments;

/**
 * @author stefanvuckovic
 * @date 2018-02-28
 * @since 1.2.0
 */
public class SelectableAssessmentFilter {

    private AssessmentFilter filter;
    private boolean selected;

    public SelectableAssessmentFilter(AssessmentFilter filter, boolean selected) {
        this.filter = filter;
        this.selected = selected;
    }

    public AssessmentFilter getFilter() {
        return filter;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
