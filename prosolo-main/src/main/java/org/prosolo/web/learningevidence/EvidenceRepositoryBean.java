package org.prosolo.web.learningevidence;

import lombok.Getter;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.search.LearningEvidenceTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.organization.EvidenceRepositoryPluginData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-12-12
 * @since 1.2.0
 */
@ManagedBean(name = "evidenceRepositoryBean")
@Component("evidenceRepositoryBean")
@Scope("view")
public class EvidenceRepositoryBean implements Serializable, Paginable {

    private static final long serialVersionUID = 7111934651137147635L;

    private static Logger logger = Logger.getLogger(EvidenceRepositoryBean.class);

    @Inject private LearningEvidenceManager learningEvidenceManager;
    @Inject private LoggedUserBean loggedUserBean;
    @Inject private LearningEvidenceTextSearch learningEvidenceTextSearch;
    @Inject private OrganizationManager organizationManager;

    private String page;

    private List<LearningEvidenceData> evidences;

    private String searchTerm = "";
    private LearningEvidenceLabeledSearchFilter filter = LearningEvidenceLabeledSearchFilter.ALL;
    private LearningEvidenceLabeledSortOption sortOption = LearningEvidenceLabeledSortOption.NEWEST_FIRST;

    private PaginationData paginationData;

    private LearningEvidenceLabeledSearchFilter[] filters;
    private LearningEvidenceLabeledSortOption[] sortOptions;

    private List<String> keywords;

    @Getter
    private EvidenceRepositoryPluginData evidenceRepositoryPluginData;

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
        sortOptions = LearningEvidenceLabeledSortOption.values();
        try {
            loadEvidences();
            keywords = learningEvidenceManager.getKeywordsFromAllUserEvidences(loggedUserBean.getUserId());

            // load evidence repository plugin data
            evidenceRepositoryPluginData = organizationManager.getOrganizationEvidenceRepositoryPluginData(loggedUserBean.getOrganizationId());
            List<LearningEvidenceLabeledSearchFilter> searchFilters = new LinkedList<>();
            searchFilters.add(LearningEvidenceLabeledSearchFilter.ALL);
            if (evidenceRepositoryPluginData.isFileEvidenceEnabled()) {
                searchFilters.add(LearningEvidenceLabeledSearchFilter.FILE);
            }
            if (evidenceRepositoryPluginData.isUrlEvidenceEnabled()) {
                searchFilters.add(LearningEvidenceLabeledSearchFilter.URL);
            }
            if (evidenceRepositoryPluginData.isTextEvidenceEnabled()) {
                searchFilters.add(LearningEvidenceLabeledSearchFilter.TEXT);
            }
            filters = searchFilters.stream().toArray(LearningEvidenceLabeledSearchFilter[]::new);
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
        extractResult(learningEvidenceTextSearch.searchUserLearningEvidences(
                loggedUserBean.getOrganizationId(),
                loggedUserBean.getUserId(),
                searchTerm,
                paginationData.getPage() - 1,
                paginationData.getLimit(),
                filter.getFilter(),
                sortOption.getSortOption()));
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

    public List<String> getKeywords() {
        return keywords;
    }
}
