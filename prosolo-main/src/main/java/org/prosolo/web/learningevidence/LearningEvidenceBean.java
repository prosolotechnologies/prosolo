package org.prosolo.web.learningevidence;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
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
 * @date 2017-12-13
 * @since 1.2.0
 */
@ManagedBean(name = "learningEvidenceBean")
@Component("learningEvidenceBean")
@Scope("view")
public class LearningEvidenceBean implements Serializable {

    private static final long serialVersionUID = 1316185395776326289L;

    private static Logger logger = Logger.getLogger(LearningEvidenceBean.class);

    @Inject private LearningEvidenceManager learningEvidenceManager;
    @Inject private LoggedUserBean loggedUserBean;
    @Inject private UrlIdEncoder idEncoder;

    private String evidenceId;

    private LearningEvidenceData evidence;
    private ResourceAccessData access;

    public void initManager() {
        init(AccessMode.MANAGER);
    }

    public void initStudent() {
        init(AccessMode.USER);
    }

    private void init(AccessMode accessMode) {
        try {
            long decodedEvId = idEncoder.decodeId(evidenceId);
            if (decodedEvId > 0) {
                loadEvidence(decodedEvId);
                if (evidence == null) {
                    PageUtil.notFound();
                } else {
                    if (accessMode == AccessMode.USER && evidence.getUserId() == loggedUserBean.getUserId()) {
                        access = new ResourceAccessData(true, true, true, false, false);
                    } else {
                        access = learningEvidenceManager.getResourceAccessRightsForEvidence(
                                evidence.getId(), loggedUserBean.getUserId(), ResourceAccessRequirements.of(accessMode));
                    }
                    if (!access.isCanAccess()) {
                        PageUtil.accessDenied();
                    }
                }
            } else {
                PageUtil.notFound();
            }
        } catch (DbConnectionException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error loading the page");
        }
    }

    private void loadEvidence(long evidenceId) {
        evidence = learningEvidenceManager.getLearningEvidence(evidenceId, true, true);
    }

    /*
    ACTIONS
     */

    public void deleteEvidence() {
        try {
            learningEvidenceManager.deleteLearningEvidence(evidence.getId(), loggedUserBean.getUserContext());
            PageUtil.fireSuccessfulInfoMessageAcrossPages("Evidence successfully removed");
            PageUtil.redirect("/evidences");
        } catch (DbConnectionException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error deleting the evidence");
        }
    }

    /*
    GETTERS AND SETTERS
     */
    public LearningEvidenceData getEvidence() {
        return evidence;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public ResourceAccessData getAccess() {
        return access;
    }
}
