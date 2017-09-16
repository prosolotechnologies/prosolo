package org.prosolo.web.rubrics;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.common.domainmodel.rubric.Rubric;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
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
    private RubricData rubricData;

    public void init() {
        loadRubrics();
    }

    public void loadRubrics() {
        this.rubrics = new ArrayList<>();
        this.rubricData = new RubricData();
        try {
            PaginatedResult<RubricData> res = rubricManager.getRubrics(paginationData.getPage() - 1,
                    paginationData.getLimit(), loggedUser.getOrganizationId());

            extractResult(res);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public void createRubric() {
        try {
            Rubric rubric = rubricManager.createNewRubric(this.rubricData.getName(), loggedUser.getUserContext());

            this.rubricData.setId(rubric.getId());

            logger.debug("New Rubric (" + rubric.getTitle() + ")");

            PageUtil.fireSuccessfulInfoMessageAcrossPages( "Rubric has been created");
            PageUtil.redirect("/manage/rubrics");
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error(e);
            e.printStackTrace();

            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent(
                    "newRubricModal:formNewRubricModal:inputTextRubricName");
            input.setValid(false);
            context.addMessage("newRubricModal:formNewRubricModal:inputTextRubricName",
                    new FacesMessage("Rubric with this name already exists"));
            context.validationFailed();
        } catch (Exception e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error creating a rubric");
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

    public RubricData getRubricData() {
        return rubricData;
    }

    public void setRubricData(RubricData rubricData) {
        this.rubricData = rubricData;
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
