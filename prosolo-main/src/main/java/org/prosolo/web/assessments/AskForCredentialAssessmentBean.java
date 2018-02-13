package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.assessments.AssessmentNotificationData;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;

/**
 * @author stefanvuckovic
 * @date 2018-02-07
 * @since 1.2.0
 */

@ManagedBean(name = "askForCredentialAssessmentBean")
@Component("askForCredentialAssessmentBean")
@Scope("view")
public class AskForCredentialAssessmentBean extends AskForAssessmentBean implements Serializable {

    private static final long serialVersionUID = 7814652679542760210L;

    private static Logger logger = Logger.getLogger(AskForCredentialAssessmentBean.class);

    @Inject
    private CredentialManager credManager;
    @Inject
    private ThreadPoolTaskExecutor taskExecutor;

    @Override
    public void initInstructorAssessmentAssessor() {
        if (getAssessmentType() == AssessmentType.INSTRUCTOR_ASSESSMENT) {
            Optional<UserData> assessor = assessmentManager.getInstructorCredentialAssessmentAssessor(getResourceId(), loggedUser.getUserId());
            assessor.ifPresent(a -> {
                getAssessmentRequestData().setAssessorId(a.getId());
                getAssessmentRequestData().setAssessorFullName(a.getFullName());
                getAssessmentRequestData().setAssessorAvatarUrl(a.getAvatarUrl());
            });
        }
    }

    @Override
    public void searchPeers() {
        if (peerSearchTerm == null && peerSearchTerm.isEmpty()) {
            peersForAssessment = null;
        } else {
            try {
                if (existingPeerAssessors == null) {
                    existingPeerAssessors = new HashSet<>(credManager
                            .getPeerAssessorIdsForUserAndCredential(resourceId, loggedUser.getUserId()));
                }

                PaginatedResult<UserData> result = userTextSearch.searchPeersWithoutAssessmentRequest(
                        loggedUser.getOrganizationId(), peerSearchTerm, 3, resourceId, usersToExcludeFromPeerSearch);
                peersForAssessment = result.getFoundNodes();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    @Override
    public void chooseRandomPeerForAssessor() {
        resetAskForAssessmentModal();

        UserData randomPeer = credManager.chooseRandomPeer(resourceId, loggedUser.getUserId());

        if (randomPeer != null) {
            assessmentRequestData.setAssessorId(randomPeer.getId());
            assessmentRequestData.setAssessorFullName(randomPeer.getFullName());
            assessmentRequestData.setAssessorAvatarUrl(randomPeer.getAvatarUrl());
            noRandomAssessor = false;
        } else {
            noRandomAssessor = true;
        }
    }

    @Override
    protected LearningResourceType getResourceType() {
        return LearningResourceType.CREDENTIAL;
    }

    @Override
    protected void submitAssessmentRequest() throws IllegalDataStateException {
        assessmentManager.requestAssessment(this.assessmentRequestData, loggedUser.getUserContext());
    }

    @Override
    protected void notifyAssessorToAssessResource() {
        assessmentManager.notifyAssessorToAssessCredential(
                AssessmentNotificationData.of(
                        resourceId,
                        assessmentRequestData.getAssessorId(),
                        assessmentRequestData.getStudentId(),
                        assessmentType),
                loggedUser.getUserContext());
    }

}
