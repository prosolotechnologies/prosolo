package org.prosolo.web.rubrics;

import org.apache.log4j.Logger;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.RubricTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.RubricData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
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
import java.util.List;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-24
 * @since 1.0.0
 */

@ManagedBean(name = "rubricsBean")
@Component("rubricsBean")
@Scope("view")
public class RubricsBean implements Serializable, Paginable {

    protected static Logger logger = Logger.getLogger(RubricsBean.class);

    @Inject
    private UrlIdEncoder idEncoder;
    @Inject
    private RubricManager rubricManager;
    @Inject
    private LoggedUserBean loggedUser;
    @Inject
    private RubricTextSearch rubricTextSearch;

    private List<RubricData> rubrics;
    private PaginationData paginationData = new PaginationData();
    private RubricData rubricToDelete;
    private String searchTerm = "";

    public void init() {
        loadRubrics();
    }

    public void loadRubrics() {
        this.rubrics = new ArrayList<>();
        try {
            PaginatedResult<RubricData> res = rubricManager.getRubrics(paginationData.getPage() - 1,
                    paginationData.getLimit(), loggedUser.getOrganizationId());
            rubrics = res.getFoundNodes();
            this.paginationData.update((int) res.getHitsNumber());
            extractResult(res);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    @Override
    public void changePage(int page) {
        if (this.paginationData.getPage() != page) {
            this.paginationData.setPage(page);
            searchRubrics();
        }
    }

    public void setRubricForDelete(RubricData rubric) {
        this.rubricToDelete = rubric;
        searchTerm = "";
    }

    public void delete() {
        if (rubricToDelete != null) {
            try {
                rubricManager.deleteRubric(this.rubricToDelete.getId(), loggedUser.getUserContext());

                PageUtil.fireSuccessfulInfoMessageAcrossPages("Rubric " + rubricToDelete.getName() + " has been deleted");
                PageUtil.redirect("/manage/rubrics");
            } catch (Exception ex) {
                logger.error(ex);
                PageUtil.fireErrorMessage("Error deleting the rubric");
            }
        }
    }

    public void resetAndSearch() {
        this.paginationData.setPage(1);
        searchRubrics();
    }

    private void searchRubrics() {
        try {
            PaginatedResult<RubricData> res = rubricTextSearch.searchRubrics(loggedUser.getUserContext().getOrganizationId(),
                    searchTerm, paginationData.getPage() - 1, paginationData.getLimit());

            rubrics = res.getFoundNodes();
            extractResult(res);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void extractResult(PaginatedResult<RubricData> result) {
        rubrics = result.getFoundNodes();
        this.paginationData.update((int) result.getHitsNumber());
    }


    @Override
    public PaginationData getPaginationData() {
        return this.paginationData;
    }

    public List<RubricData> getRubrics() {
        return rubrics;
    }

    public void setRubrics(List<RubricData> rubrics) {
        this.rubrics = rubrics;
    }

    public UrlIdEncoder getIdEncoder() {
        return idEncoder;
    }

    public void setIdEncoder(UrlIdEncoder idEncoder) {
        this.idEncoder = idEncoder;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
}
