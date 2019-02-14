package org.prosolo.web.learningevidence;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceLoadConfig;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2017-12-14
 * @since 1.2.0
 */
@ManagedBean(name = "learningEvidenceEditBean")
@Component("learningEvidenceEditBean")
@Scope("view")
public class LearningEvidenceEditBean implements Serializable {

    private static final long serialVersionUID = 100355326064208444L;

    private static Logger logger = Logger.getLogger(LearningEvidenceEditBean.class);

    @Inject private LearningEvidenceManager learningEvidenceManager;
    @Inject private LoggedUserBean loggedUserBean;
    @Inject private UrlIdEncoder idEncoder;
    @Inject private SubmitEvidenceBean submitEvidenceBean;

    private String evidenceId;
    private long decodedEvidenceId;

    public void init() {
        try {
            if (evidenceId == null) {
                submitEvidenceBean.init(new LearningEvidenceData());
            } else {
                decodedEvidenceId = idEncoder.decodeId(evidenceId);
                submitEvidenceBean.init(learningEvidenceManager.getLearningEvidence(decodedEvidenceId, LearningEvidenceLoadConfig.builder().loadTags(true).build()));
            }
        } catch (DbConnectionException e) {
            logger.error("Error", e);
            submitEvidenceBean.init(new LearningEvidenceData());
            PageUtil.fireErrorMessage("Error loading the page");
        }
    }

    public boolean isCreateUseCase() {
        return getEvidence().getId() == 0;
    }

    /*
    ACTIONS
     */

    public void postFileEvidence() {
        submitEvidenceBean.preparePostFileEvidence();
        saveEvidence();
    }

    public void postUrlEvidence() {
        submitEvidenceBean.preparePostUrlEvidence();
        saveEvidence();
    }

    public void postTextEvidence() {
        submitEvidenceBean.preparePostTextEvidence();
        saveEvidence();
    }

    public void saveEvidence() {
        try {
            String pageToRedirect = "/evidence";
            String growlMessage = null;

            if (getEvidence().getId() > 0) {
                learningEvidenceManager.updateEvidence(getEvidence(), loggedUserBean.getUserContext());
                growlMessage = "Evidence saved";

                String sourcePage = PageUtil.getGetParameter("source");
                if (!StringUtils.isBlank(sourcePage)) {
                    pageToRedirect = sourcePage;
                }
            } else {
                learningEvidenceManager.postEvidence(getEvidence(), loggedUserBean.getUserContext());
                growlMessage ="Evidence added";
            }

            PageUtil.fireSuccessfulInfoMessageAcrossPages(growlMessage);
            PageUtil.redirect(pageToRedirect);
        } catch (Exception e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error saving the evidence");
        }
    }

    /*
    GETTERS AND SETTERS
     */
    public LearningEvidenceData getEvidence() {
        return submitEvidenceBean.getEvidence();
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }
}
