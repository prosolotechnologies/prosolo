package org.prosolo.web.learningevidence;

import lombok.Getter;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceLoadConfig;
import org.prosolo.services.nodes.data.organization.EvidenceRepositoryPluginData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * This bean displays a piece of evidence of a student. This page is accessed by the student creator of the piece of
 * the piece of evidence.
 *
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
    @Inject private OrganizationManager organizationManager;

    private String evidenceId;

    private LearningEvidenceData evidence;
    private ResourceAccessData access;

    @Getter
    private EvidenceRepositoryPluginData evidenceRepositoryPluginData;

    public void init() {
        try {
            long decodedEvId = idEncoder.decodeId(evidenceId);
            if (decodedEvId > 0) {
                loadEvidence(decodedEvId);

                if (evidence == null) {
                    PageUtil.notFound();
                } else {
                    // only student creator can access this page
                    if (evidence.getUserId() == loggedUserBean.getUserId()) {
                        access = new ResourceAccessData(true, true, true, false, false);
                    }
                    if (access == null || !access.isCanAccess()) {
                        PageUtil.accessDenied();
                    }

                    // load evidence repository plugin data
                    evidenceRepositoryPluginData = organizationManager.getOrganizationEvidenceRepositoryPluginData(loggedUserBean.getOrganizationId());
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
        evidence = learningEvidenceManager.getLearningEvidence(evidenceId, LearningEvidenceLoadConfig.builder().loadCompetences(true).loadTags(true).build());
    }

    public boolean isCurrentUserEvidenceOwner() {
        return evidence != null && evidence.getUserId() == loggedUserBean.getUserId();
    }

    /*
    ACTIONS
     */

    public void deleteEvidence() {
        try {
            learningEvidenceManager.deleteLearningEvidence(evidence.getId(), loggedUserBean.getUserContext());
            PageUtil.fireSuccessfulInfoMessageAcrossPages("Evidence successfully removed");
            PageUtil.redirect("/evidence");
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
