package org.prosolo.web.rubrics;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.search.RubricTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
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

    private static final String rubricNameTextFieldId = "newRubricModal:formNewRubricModal:inputTextRubricName";

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
    private String searchTerm = "";
    private String rubricName = "";

    public void init() {
        loadRubrics();
    }

    public void loadRubrics() {
        this.rubrics = new ArrayList<>();
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
            Rubric rubric = rubricManager.createNewRubric(rubricName, loggedUser.getUserContext());

            logger.debug("New Rubric (" + rubric.getTitle() + ")");

            PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getMessage("label.rubric") + " has been created");
            loadRubrics();
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error(e);
            e.printStackTrace();

            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent(rubricNameTextFieldId);
            input.setValid(false);
            context.addMessage(rubricNameTextFieldId,
                    new FacesMessage(ResourceBundleUtil.getMessage("label.rubric") + " with this name already exists"));
            context.validationFailed();
        } catch (Exception e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error creating a " + ResourceBundleUtil.getMessage("label.rubric").toLowerCase());
        }
    }

    @Override
    public void changePage(int page) {
        if (this.paginationData.getPage() != page) {
            this.paginationData.setPage(page);
            searchRubrics();
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

    public String getRubricName() {
        return rubricName;
    }

    public void setRubricName(String rubricName) {
        this.rubricName = rubricName;
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
