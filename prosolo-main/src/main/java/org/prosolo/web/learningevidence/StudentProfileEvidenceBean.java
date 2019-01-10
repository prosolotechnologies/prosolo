package org.prosolo.web.learningevidence;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceLoadConfig;
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
 * Page that displays information about a piece of evidence for the given ID of an CompetenceEvidence instance and of a given user.
 * It loads the evidence data, along with the relation to competency text from the CompetenceEvidence instance.
 *
 * @author stefanvuckovic
 * @date 2018-12-04
 * @since 1.2.0
 */
@ManagedBean(name = "profileEvidenceBean")
@Component("profileEvidenceBean")
@Scope("view")
public class StudentProfileEvidenceBean implements Serializable {

    private static final long serialVersionUID = 1051617855061201834L;

    private static Logger logger = Logger.getLogger(StudentProfileEvidenceBean.class);

    @Inject private LearningEvidenceManager learningEvidenceManager;
    @Inject private LoggedUserBean loggedUserBean;
    @Inject private UrlIdEncoder idEncoder;

    private String studentId;
    private String competenceEvidenceId;

    private LearningEvidenceData evidence;

    public void initManager() {
        init(AccessMode.MANAGER);
    }

    public void initStudent() {
        init(AccessMode.USER);
    }

    private void init(AccessMode accessMode) {
        try {
            long decodedStudentId = idEncoder.decodeId(studentId);
            long decodedCompEvidenceId = idEncoder.decodeId(competenceEvidenceId);

            if (decodedStudentId > 0 && decodedCompEvidenceId > 0) {
                evidence = learningEvidenceManager.getCompetenceEvidenceData(
                        decodedCompEvidenceId,
                        LearningEvidenceLoadConfig.builder().loadTags(true).loadCompetenceTitle(true).loadUserName(true).build());

                if (evidence == null) {
                    PageUtil.notFound();
                } else {
                    if (evidence.getUserId() != decodedStudentId) {
                        //if a piece of evidence is not belonging to the user with ID passed in the URL, show Page Not Found page.
                        PageUtil.notFound();
                        return;
                    }

                    // check if there is a published competence with this evidence
                    boolean published = learningEvidenceManager.isCompetenceEvidencePublishedOnProfile(evidence.getCompetenceEvidenceId());

                    if (!published) {
                        // check if user is an evidence creator or assessor or manager
                        ResourceAccessData access = learningEvidenceManager.getResourceAccessRightsForEvidence(evidence.getId(), loggedUserBean.getUserId(), ResourceAccessRequirements.of(accessMode));

                        if (!access.isCanAccess()) {
                            PageUtil.accessDenied();
                        }
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


    /*
    GETTERS AND SETTERS
     */
    public LearningEvidenceData getEvidence() {
        return evidence;
    }

    public String getCompetenceEvidenceId() {
        return competenceEvidenceId;
    }

    public void setCompetenceEvidenceId(String competenceEvidenceId) {
        this.competenceEvidenceId = competenceEvidenceId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
