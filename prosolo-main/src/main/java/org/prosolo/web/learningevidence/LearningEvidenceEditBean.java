package org.prosolo.web.learningevidence;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
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
                submitEvidenceBean.init(learningEvidenceManager.getLearningEvidence(decodedEvidenceId, true, false));
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
            if (getEvidence().getId() > 0) {
                learningEvidenceManager.updateEvidence(getEvidence(), loggedUserBean.getUserContext());
                PageUtil.fireSuccessfulInfoMessage("Evidence saved");
            } else {
                learningEvidenceManager.postEvidence(getEvidence(), loggedUserBean.getUserContext());
                PageUtil.fireSuccessfulInfoMessageAcrossPages("Evidence successfully posted");
                PageUtil.redirect("/evidences");
            }
        } catch (ConstraintViolationException|DataIntegrityViolationException e) {
            logger.error("Error", e);
            //for now we don't check unique evidence name so this exception can't occur
        } catch (DbConnectionException e) {
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
