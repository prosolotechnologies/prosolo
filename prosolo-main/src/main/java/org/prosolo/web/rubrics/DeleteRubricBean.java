package org.prosolo.web.rubrics;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Bojan Trifkovic
 * @date 2017-09-20
 * @since 1.0.0
 */

@ManagedBean(name = "deleteRubricBean")
@Component("deleteRubricBean")
@Scope("view")
public class DeleteRubricBean implements Serializable {

    protected static Logger logger = Logger.getLogger(DeleteRubricBean.class);

    @Inject
    private RubricManager rubricManager;
    @Inject
    private LoggedUserBean loggedUser;

    private RubricData rubricToDelete;

    public void setRubricForDelete(RubricData rubric) {
        this.rubricToDelete = rubric;
    }

    public void delete() {
        if (rubricToDelete != null) {
            try {
                rubricManager.deleteRubric(this.rubricToDelete.getId(), loggedUser.getUserContext());

                PageUtil.fireSuccessfulInfoMessageAcrossPages(ResourceBundleUtil.getMessage("label.rubric") + " " +
                        rubricToDelete.getName() + " has been deleted");
                PageUtil.redirect("/manage/rubrics");
            } catch (Exception ex) {
                logger.error(ex);
                PageUtil.fireErrorMessage("Error deleting the " + ResourceBundleUtil.getMessage("label.rubric").toLowerCase());
            }
        }
    }
}
