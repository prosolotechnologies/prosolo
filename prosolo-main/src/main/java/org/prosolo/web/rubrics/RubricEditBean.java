package org.prosolo.web.rubrics;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.RubricData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-24
 * @since 1.0.0
 */

@ManagedBean(name = "rubricEditBean")
@Component("rubricEditBean")
@Scope("view")
public class RubricEditBean implements Serializable {

    protected static Logger logger = Logger.getLogger(RubricEditBean.class);

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
            this.rubric = new RubricData();
            this.decodedId = idEncoder.decodeId(id);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public void createRubric() {
        try {
            Rubric rubric = rubricManager.createNewRubric(this.rubric.getName(), loggedUser.getUserId(),
                    loggedUser.getUserContext(decodedId));

            this.rubric.setId(rubric.getId());

            logger.debug("New Rubric (" + rubric.getTitle() + ")");

            PageUtil.fireSuccessfulInfoMessageAcrossPages("Rubric successfully saved");
            PageUtil.redirect("/manage/rubrics");
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error(e);
            e.printStackTrace();
            FacesContext.getCurrentInstance().validationFailed();
            PageUtil.fireErrorMessage("Rubric with this name already exists");
        } catch (Exception e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error while trying to save rubric data");
        }
    }


    public RubricData getRubric() {
        return rubric;
    }

    public void setRubric(RubricData rubric) {
        this.rubric = rubric;
    }
}
