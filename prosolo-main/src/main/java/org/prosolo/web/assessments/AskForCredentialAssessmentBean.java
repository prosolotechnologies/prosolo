package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.assessments.AssessmentNotificationData;
import org.prosolo.services.user.data.UserAssessmentTokenExtendedData;
import org.prosolo.services.user.data.UserData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

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

    public void init(long credentialId, long targetCredentialId, AssessmentType assessmentType, BlindAssessmentMode blindAssessmentMode) {
        init(credentialId, targetCredentialId, assessmentType, null, blindAssessmentMode);
    }

    public void init(long credentialId, long targetCredentialId, AssessmentType assessmentType, UserData assessor, BlindAssessmentMode blindAssessmentMode) {
        initCommonInitialData(credentialId, targetCredentialId, assessmentType, blindAssessmentMode);
        initOtherCommonData(assessor);
    }

    @Override
    public void initInstructorAssessmentAssessor() {
        Optional<UserData> assessor = assessmentManager.getActiveInstructorCredentialAssessmentAssessor(getResourceId(), loggedUser.getUserId());
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
                            .getPeerAssessorIdsForCredential(resourceId, loggedUser.getUserId()));
                }

                PaginatedResult<UserData> result = userTextSearch.searchCredentialPeers(
                        loggedUser.getOrganizationId(), peerSearchTerm, 3, resourceId, usersToExcludeFromPeerSearch);
                peersForAssessment = result.getFoundNodes();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    @Override
    public UserData getPeerAssessorFromAssessorPool() {
        return null;
    }

    @Override
    protected LearningResourceType getResourceType() {
        return LearningResourceType.CREDENTIAL;
    }

    @Override
    protected void submitAssessmentRequest() throws IllegalDataStateException {
        assessmentManager.requestCredentialAssessment(this.assessmentRequestData, loggedUser.getUserContext());
    }

    @Override
    protected void notifyAssessorToAssessResource() throws IllegalDataStateException {
        assessmentManager.notifyAssessorToAssessCredential(
                AssessmentNotificationData.of(
                        resourceId,
                        assessmentRequestData.getAssessorId(),
                        assessmentRequestData.getStudentId(),
                        assessmentType),
                loggedUser.getUserContext());
    }

    @Override
    protected boolean shouldStudentBeRemindedToSubmitEvidenceSummary() {
        return credManager.doesCredentialHaveAtLeastOneEvidenceBasedCompetence(resourceId);
    }

    @Override
    protected boolean isThereUnassignedAssessmentForThisUser() {
        return false;
    }

    @Override
    protected List<Long> loadAssessorPoolUserIds() {
        return new ArrayList<>();
    }

    @Override
    protected UserAssessmentTokenExtendedData loadUserAssessmentTokenDataAndRefreshInSession() {
        return new UserAssessmentTokenExtendedData(false, false, 0, 0);
    }

    @Override
    protected Set<Long> getExistingPeerAssessors() {
        return new HashSet<>(assessmentManager
                .getPeerAssessorIdsForCredential(resourceId, loggedUser.getUserId()));
    }
}
