package org.prosolo.web.courses.competence;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.search.LearningEvidenceTextSearch;
import org.prosolo.search.impl.PaginatedResult;
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
import java.util.*;

/**
 * @author stefanvuckovic
 * @date 2017-12-07
 * @since 1.2.0
 */
@ManagedBean(name = "learningEvidenceSearchBean")
@Component("learningEvidenceSearchBean")
@Scope("view")
public class LearningEvidenceSearchBean implements Serializable, Paginable {

    private static final long serialVersionUID = -8214781154286690736L;

    @Inject private LearningEvidenceTextSearch learningEvidenceTextSearch;
    @Inject private LoggedUserBean loggedUserBean;

    private boolean initialized;
    private List<LearningEvidenceData> evidences;
    private List<Long> evidencesToExcludeFromSearch;

    private String searchTerm = "";
    private PaginationData paginationData = new PaginationData();

    public void init(List<Long> evidencesToExcludeFromSearch) {
        try {
            this.initialized = true;
            this.evidencesToExcludeFromSearch = new ArrayList<>();
            this.evidencesToExcludeFromSearch.addAll(evidencesToExcludeFromSearch);

            searchEvidences();
        } catch (DbConnectionException e) {
            PageUtil.fireErrorMessage("Error loading the page");
        }
    }

    public void search() {
        paginationData.setPage(1);
        searchEvidences();
    }

    public void resetAndSearch() {
        searchTerm = "";
        search();
    }

    public void searchEvidences() {
        PaginatedResult<LearningEvidenceData> response = learningEvidenceTextSearch.searchLearningEvidences(
                loggedUserBean.getOrganizationId(), loggedUserBean.getUserId(), evidencesToExcludeFromSearch, searchTerm, paginationData.getPage() - 1,
                paginationData.getLimit());

        paginationData.update((int) response.getHitsNumber());
        evidences = response.getFoundNodes();
    }

    public void excludeEvidenceFromFutureSearches(long evidenceId) {
        this.evidencesToExcludeFromSearch.add(evidenceId);
    }

    public void includeEvidenceInFutureSearches(long evidenceId) {
        this.evidencesToExcludeFromSearch.remove(evidenceId);
    }

    @Override
    public void changePage(int page) {
        if(this.paginationData.getPage() != page) {
            this.paginationData.setPage(page);
            searchEvidences();
        }
    }

    @Override
    public PaginationData getPaginationData() {
        return paginationData;
    }

    public boolean isInitialized() {
        return initialized;
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
}
