package org.prosolo.search.util.learningevidence;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-12-13
 * @since 1.2.0
 */
public class LearningEvidenceSearchConfig {

    private final LearningEvidenceSearchFilter filter;
    private final LearningEvidenceSortOption sortOption;
    private final boolean searchKeywords;
    private final List<Long> evidencesToExclude;
    private final String[] includeFields;

    private LearningEvidenceSearchConfig(LearningEvidenceSearchFilter filter, LearningEvidenceSortOption sortOption, boolean searchKeywords, List<Long> evidencesToExclude, String[] includeFields) {
        this.filter = filter;
        this.sortOption = sortOption;
        this.searchKeywords = searchKeywords;
        this.evidencesToExclude = evidencesToExclude;
        this.includeFields = includeFields;
    }

    public static LearningEvidenceSearchConfig configure(LearningEvidenceSearchFilter filter, LearningEvidenceSortOption sortOption, boolean searchKeywords, List<Long> evidencesToExclude, String[] includeFields) {
        return new LearningEvidenceSearchConfig(filter, sortOption, searchKeywords, evidencesToExclude, includeFields);
    }

    public LearningEvidenceSearchFilter getFilter() {
        return filter;
    }

    public LearningEvidenceSortOption getSortOption() {
        return sortOption;
    }

    public boolean isSearchKeywords() {
        return searchKeywords;
    }

    public List<Long> getEvidencesToExclude() {
        return evidencesToExclude;
    }

    public String[] getIncludeFields() {
        return includeFields;
    }
}
