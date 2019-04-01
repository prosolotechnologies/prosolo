package org.prosolo.web;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.authentication.annotations.AuthenticationChangeType;
import org.prosolo.services.authentication.annotations.SessionAttributeScope;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.UserAssessmentTokenData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2019-03-25
 * @since 1.3
 */
@ManagedBean(name = "assessmentTokenSessionBean")
@Component("assessmentTokenSessionBean")
@Scope(value = "session")
@SessionAttributeScope(end = AuthenticationChangeType.USER_AUTHENTICATION_CHANGE)
public class AssessmentTokenSessionBean implements Serializable {

    private Logger logger = Logger.getLogger(AssessmentTokenSessionBean.class);

    @Inject private LoggedUserBean loggedUserBean;
    @Inject private UserManager userManager;

    private UserAssessmentTokenData assessmentTokenData;

    /*
    store user id to make sure this bean is in sync with user currently logged in.

    There is a small possibility for data in this bean to be out of sync when user makes two parallel
    requests (one of them being reauthentication request: LTI, Login as) where with some unlucky timing this bean
    could hold values for previously authenticated user but this is only theoretical possibility
    and will probably never happen in practice.
     */
    private long userId;

    @PostConstruct
    public void init() {
        userId = loggedUserBean.getUserId();
        try {
            assessmentTokenData = userManager.getUserAssessmentTokenData(userId);
        } catch (DbConnectionException e) {
            logger.error("error", e);
            assessmentTokenData = new UserAssessmentTokenData(false, false, 0);
        }
    }

    public void refreshData(UserAssessmentTokenData userAssessmentTokenData) {
        this.assessmentTokenData = new UserAssessmentTokenData(
                userAssessmentTokenData.isAssessmentTokensEnabled(),
                userAssessmentTokenData.isUserAvailableForAssessments(),
                userAssessmentTokenData.getNumberOfTokensAvailable());
    }

    private UserAssessmentTokenData getAssessmentTokenData() {
        if (userId != loggedUserBean.getUserId()) {
            init();
        }
        return assessmentTokenData;
    }

    public boolean isAssessmentTokensEnabled() {
        return getAssessmentTokenData().isAssessmentTokensEnabled();
    }

    public int getNumberOfTokensAvailable() {
        return getAssessmentTokenData().getNumberOfTokensAvailable();
    }
}
