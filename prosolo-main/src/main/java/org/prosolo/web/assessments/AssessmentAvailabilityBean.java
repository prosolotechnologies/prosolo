package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.UserAssessmentTokenData;
import org.prosolo.web.AssessmentTokenSessionBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2019-03-22
 * @since 1.3
 */
@ManagedBean(name = "assessmentAvailabilityBean")
@Component("assessmentAvailabilityBean")
@Scope("view")
public class AssessmentAvailabilityBean implements Serializable {

    private static Logger logger = Logger.getLogger(AssessmentAvailabilityBean.class);

    @Inject private UserManager userManager;
    @Inject private LoggedUserBean loggedUserBean;
    @Inject private AssessmentTokenSessionBean assessmentTokenSessionBean;

    private UserAssessmentTokenData assessmentTokenData;

    public void init() {
        try {
            assessmentTokenData = userManager.getUserAssessmentTokenData(loggedUserBean.getUserId());
            //refresh data in assessment token session bean
            assessmentTokenSessionBean.refreshData(assessmentTokenData);
        } catch (DbConnectionException e) {
            logger.error("error", e);
            PageUtil.fireErrorMessage("Error loading the page");
        }
    }

    public void updateAssessmentAvailability() {
        try {
            userManager.updateAssessmentAvailability(loggedUserBean.getUserId(), assessmentTokenData.isUserAvailableForAssessments());
            PageUtil.fireSuccessfulInfoMessage("Assessment availability has been successfully updated");
        } catch (DbConnectionException e) {
            logger.error("error", e);
            assessmentTokenData.setUserAvailableForAssessments(!assessmentTokenData.isUserAvailableForAssessments());
            PageUtil.fireErrorMessage("Error updating the assessment availability");
        }
    }

    public UserAssessmentTokenData getAssessmentTokenData() {
        return assessmentTokenData;
    }
}
