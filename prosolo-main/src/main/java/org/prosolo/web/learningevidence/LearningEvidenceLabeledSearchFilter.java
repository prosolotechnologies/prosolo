package org.prosolo.web.learningevidence;

import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.search.util.learningevidence.LearningEvidenceSearchFilter;

/**
 * @author stefanvuckovic
 * @date 2017-12-12
 * @since 1.2.0
 */
public enum LearningEvidenceLabeledSearchFilter {

    ALL(LearningEvidenceSearchFilter.ALL, "All"),
    FILE(LearningEvidenceSearchFilter.FILE, "File"),
    URL(LearningEvidenceSearchFilter.URL, "URL"),
    TEXT(LearningEvidenceSearchFilter.TEXT, "Text");

    private LearningEvidenceSearchFilter filter;
    private String label;

    private LearningEvidenceLabeledSearchFilter(LearningEvidenceSearchFilter filter, String label) {
        this.filter = filter;
        this.label = label;
    }

    public LearningEvidenceSearchFilter getFilter() {
        return filter;
    }

    public String getLabel() {
        return label;
    }

}
