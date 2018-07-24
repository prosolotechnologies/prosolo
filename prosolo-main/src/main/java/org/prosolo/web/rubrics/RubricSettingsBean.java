package org.prosolo.web.rubrics;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-24
 * @since 1.0.0
 */

@ManagedBean(name = "rubricSettingsBean")
@Component("rubricSettingsBean")
@Scope("view")
public class RubricSettingsBean implements Serializable {

    protected static Logger logger = Logger.getLogger(RubricSettingsBean.class);

    private static final String rubricNameTextFieldId = "formMainEditRubric:inputTextRubricName";

    @Inject
    private LoggedUserBean loggedUser;
    @Inject
    private UrlIdEncoder idEncoder;
    @Inject
    private RubricManager rubricManager;

    private RubricData rubric;
    private String id;
    private long decodedId;

    public void init() {
        try {
            this.decodedId = idEncoder.decodeId(id);
            if (decodedId > 0) {
                this.rubric = rubricManager.getRubricData(decodedId, true, false, loggedUser.getUserId(), false, true);
                if (this.rubric == null) {
                    PageUtil.notFound();
                }
            } else {
                PageUtil.notFound();
            }
        } catch (Exception e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error loading the page");
        }
    }

    public void updateRubric() {
        try {
            rubricManager.updateRubricName(this.rubric.getId(), this.rubric.getName(), loggedUser.getUserContext());

            logger.debug("Rubric (" + this.rubric.getId() + ") updated by the user " + loggedUser.getUserId());
            PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.rubric").toLowerCase() + " has been updated");
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error(e);
            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent(rubricNameTextFieldId);
            input.setValid(false);
            context.addMessage(rubricNameTextFieldId, new FacesMessage(ResourceBundleUtil.getMessage("label.rubric") + " with this name already exists"));
        } catch (Exception e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error updating the " + ResourceBundleUtil.getMessage("label.rubric").toLowerCase());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RubricData getRubric() {
        return rubric;
    }

    public void setRubric(RubricData rubric) {
        this.rubric = rubric;
    }
}
