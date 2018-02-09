package org.prosolo.web.learningevidence;

import org.prosolo.search.util.learningevidence.LearningEvidenceSearchFilter;
import org.prosolo.search.util.learningevidence.LearningEvidenceSortOption;

/**
 * @author stefanvuckovic
 * @date 2017-12-12
 * @since 1.2.0
 */
public enum LearningEvidenceLabeledSortOption {

    NEWEST_FIRST(LearningEvidenceSortOption.NEWEST_FIRST, "Newest first"),
    ALPHABETICALLY(LearningEvidenceSortOption.ALPHABETICALLY, "Alphabetically");

    private LearningEvidenceSortOption sortOption;
    private String label;

    private LearningEvidenceLabeledSortOption(LearningEvidenceSortOption sortOption, String label) {
        this.sortOption = sortOption;
        this.label = label;
    }

    public LearningEvidenceSortOption getSortOption() {
        return sortOption;
    }

    public String getLabel() {
        return label;
    }
}
