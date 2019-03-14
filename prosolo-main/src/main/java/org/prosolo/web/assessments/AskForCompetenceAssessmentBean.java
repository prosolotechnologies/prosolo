package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.AssessorAssignmentMethod;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.LearningPathType;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.nodes.data.assessments.AssessmentNotificationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
    @Inject private CredentialManager credManager;

    private long credentialId;
    private boolean studentCanChooseInstructor;

    public void init(long credentialId, long competenceId, long targetCompId, AssessmentType assessmentType, BlindAssessmentMode blindAssessmentMode) {
        init(credentialId, competenceId, targetCompId, assessmentType, null, blindAssessmentMode);
    }

    public void init(long credentialId, long competenceId, long targetCompId, AssessmentType assessmentType, UserData assessor, BlindAssessmentMode blindAssessmentMode) {
        initInitialData(credentialId, competenceId, targetCompId, assessmentType, blindAssessmentMode);
        initOtherCommonData(assessor);
        initStudentCanChooseInstructorFlag();
    }

    /**
     * Initializes all initial data that must be initialized before any other
     * logic or initialization takes place
     *
     * @param credentialId
     * @param competenceId
     * @param targetCompId
     * @param assessmentType
     * @param blindAssessmentMode
     */
    private void initInitialData(long credentialId, long competenceId, long targetCompId, AssessmentType assessmentType, BlindAssessmentMode blindAssessmentMode) {
        initCommonInitialData(competenceId, targetCompId, assessmentType, blindAssessmentMode);
        this.credentialId = credentialId;
        assessmentRequestData.setCredentialId(credentialId);
    }

    private void initStudentCanChooseInstructorFlag() {
        if (getAssessmentRequestData().getAssessorId() == 0) {
            /*
            if assessor is not assigned, get assessor assignment method info for credential to be able
            to provide student with more information
             */
            studentCanChooseInstructor = credManager.getAssessorAssignmentMethod(credentialId) == AssessorAssignmentMethod.BY_STUDENTS;
        }
    }

    @Override
    public void initInstructorAssessmentAssessor() {
        Optional<UserData> assessor = assessmentManager.getActiveInstructorCompetenceAssessmentAssessor(credentialId, getResourceId(), loggedUser.getUserId());
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
                            .getPeerAssessorIdsForCompetence(credentialId, resourceId, loggedUser.getUserId()));
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
    protected void notifyAssessorToAssessResource() throws IllegalDataStateException {
        assessmentManager.notifyAssessorToAssessCompetence(
                AssessmentNotificationData.of(
                        credentialId,
                        resourceId,
                        assessmentRequestData.getAssessorId(),
                        assessmentRequestData.getStudentId(),
                        assessmentType),
                loggedUser.getUserContext());
    }

    @Override
    protected boolean shouldStudentBeRemindedToSubmitEvidenceSummary() {
        //student should be reminded if competency is evidence based
        return compManager.getCompetenceLearningPathType(resourceId) == LearningPathType.EVIDENCE;
    }

    @Override
    protected Set<Long> getExistingPeerAssessors() {
        return new HashSet<>(assessmentManager
                .getPeerAssessorIdsForCompetence(credentialId, resourceId, loggedUser.getUserId()));
    }

    public long getCredentialId() {
        return credentialId;
    }

    public boolean isStudentCanChooseInstructor() {
        return studentCanChooseInstructor;
    }

}
