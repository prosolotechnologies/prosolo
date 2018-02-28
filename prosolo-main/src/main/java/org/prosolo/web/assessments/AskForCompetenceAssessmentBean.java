package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.assessments.AssessmentNotificationData;
import org.springframework.context.annotation.Scope;
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

@ManagedBean(name = "askForCompetenceAssessmentBean")
@Component("askForCompetenceAssessmentBean")
@Scope("view")
public class AskForCompetenceAssessmentBean extends AskForAssessmentBean implements Serializable {

    private static final long serialVersionUID = 8928389627379863993L;

    private static Logger logger = Logger.getLogger(AskForCompetenceAssessmentBean.class);

    @Inject private Competence1Manager compManager;

    private long credentialId;

    public void init(long credentialId, long competenceId, long targetCompId, AssessmentType assessmentType) {
        this.credentialId = credentialId;
        init(competenceId, targetCompId, assessmentType);
    }

    @Override
    public void initInstructorAssessmentAssessor() {
        Optional<UserData> assessor = assessmentManager.getInstructorCompetenceAssessmentAssessor(credentialId, getResourceId(), loggedUser.getUserId());
        assessor.ifPresent(a -> {
            getAssessmentRequestData().setAssessorId(a.getId());
            getAssessmentRequestData().setAssessorFullName(a.getFullName());
            getAssessmentRequestData().setAssessorAvatarUrl(a.getAvatarUrl());
        });
    }

    @Override
    public void searchPeers() {
        if (peerSearchTerm == null && peerSearchTerm.isEmpty()) {
            peersForAssessment = null;
        } else {
            try {
                if (existingPeerAssessors == null) {
                    existingPeerAssessors = new HashSet<>(assessmentManager
                            .getPeerAssessorIdsForUserAndCompetence(resourceId, loggedUser.getUserId()));
                }

                PaginatedResult<UserData> result = userTextSearch.searchUsersLearningCompetence(
                        loggedUser.getOrganizationId(), peerSearchTerm, 3, resourceId, usersToExcludeFromPeerSearch);
                peersForAssessment = result.getFoundNodes();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    @Override
    public UserData getRandomPeerForAssessor() {
        return compManager.chooseRandomPeer(resourceId, loggedUser.getUserId());
    }

    @Override
    protected LearningResourceType getResourceType() {
        return LearningResourceType.COMPETENCE;
    }

    @Override
    protected void submitAssessmentRequest() throws IllegalDataStateException {
        assessmentManager.requestCompetenceAssessment(this.assessmentRequestData, loggedUser.getUserContext());
    }

    @Override
    protected void notifyAssessorToAssessResource() {
        assessmentManager.notifyAssessorToAssessCompetence(
                AssessmentNotificationData.of(
                        credentialId,
                        resourceId,
                        assessmentRequestData.getAssessorId(),
                        assessmentRequestData.getStudentId(),
                        assessmentType),
                loggedUser.getUserContext());
    }

}