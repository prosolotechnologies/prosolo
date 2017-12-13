package org.prosolo.web.learningevidence;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-12-12
 * @since 1.2.0
 */
@ManagedBean(name = "learningEvidencesBean")
@Component("learningEvidencesBean")
@Scope("view")
public class LearningEvidencesBean implements Serializable, Paginable {

    private static final long serialVersionUID = 7111934651137147635L;

    private static Logger logger = Logger.getLogger(LearningEvidencesBean.class);

    @Inject private LearningEvidenceManager learningEvidenceManager;
    @Inject private LoggedUserBean loggedUserBean;

    private String page;

    private List<LearningEvidenceData> evidences;

    private String searchTerm = "";
    private LearningEvidenceLabeledSearchFilter filter = LearningEvidenceLabeledSearchFilter.ALL;
    private LearningEvidenceLabeledSortOption sortOption = LearningEvidenceLabeledSortOption.NEWEST_FIRST;

    private PaginationData paginationData;

    private LearningEvidenceLabeledSearchFilter[] filters;
    private LearningEvidenceLabeledSortOption[] sortOptions;

    public void init() {
        int page = 1;
        if (this.page != null) {
            try {
                page = Integer.parseInt(this.page);
            } catch (NumberFormatException e) {
                logger.error("URL page param wrong format", e);
            }
        }

        paginationData = PaginationData.forPage(page);
        filters = LearningEvidenceLabeledSearchFilter.values();
        sortOptions = LearningEvidenceLabeledSortOption.values();
        try {
            loadEvidences();
        } catch (DbConnectionException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error loading the page");
        }
    }

    private void loadEvidences() {
        extractResult(learningEvidenceManager.getPaginatedUserEvidences(
                        loggedUserBean.getUserId(),
                        (paginationData.getPage() - 1) * paginationData.getLimit(),
                        paginationData.getLimit()));
    }

    private void getEvidenceSearchResults() {

    }

    public void searchEvidences() {
        paginationData.setPage(1);
        getEvidenceSearchResults();
    }

    private void extractResult(PaginatedResult<LearningEvidenceData> evidences) {
        this.evidences = evidences.getFoundNodes();
        this.paginationData.update((int) evidences.getHitsNumber());
    }

    public void applySearchFilter(LearningEvidenceLabeledSearchFilter filter) {
        this.filter = filter;
        paginationData.setPage(1);
        getEvidenceSearchResults();
    }

    public void applySortOption(LearningEvidenceLabeledSortOption sortOption) {
        this.sortOption = sortOption;
        paginationData.setPage(1);
        getEvidenceSearchResults();
    }

    @Override
    public void changePage(int page) {
        if (this.paginationData.getPage() != page) {
            this.paginationData.setPage(page);
            getEvidenceSearchResults();
        }
    }

    public List<LearningEvidenceData> getEvidences() {
        return evidences;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public LearningEvidenceLabeledSearchFilter getFilter() {
        return filter;
    }

    public void setFilter(LearningEvidenceLabeledSearchFilter filter) {
        this.filter = filter;
    }

    public LearningEvidenceLabeledSortOption getSortOption() {
        return sortOption;
    }

    public void setSortOption(LearningEvidenceLabeledSortOption sortOption) {
        this.sortOption = sortOption;
    }

    @Override
    public PaginationData getPaginationData() {
        return paginationData;
    }

    public LearningEvidenceLabeledSearchFilter[] getFilters() {
        return filters;
    }

    public LearningEvidenceLabeledSortOption[] getSortOptions() {
        return sortOptions;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }
}
