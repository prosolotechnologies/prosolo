package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.search.UserTextSearch;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentRequestData;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.data.UserAssessmentTokenExtendedData;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.AssessmentTokenSessionBean;

import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.PaginationData;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Holds data and logic for creating assessment request or notifying assessor to give
 * assessment when assessment has already been requested.
 *
 * {@link BlindAssessmentMode} passed to this bean should be blind assessment mode
 * configured for credential/competence if new assessment request is initiated and
 * mode configured for credential/competence assessment if assessor in existing assessment
 * is being notified.
 *
 * @author Bojan Trifkovic
 * @date 2017-10-10
 * @since 1.0.0
 */
public abstract class AskForAssessmentBean implements Serializable {

    private static final long serialVersionUID = -471373937560525469L;

    private static Logger logger = Logger.getLogger(AskForAssessmentBean.class);

    @Inject
    protected LoggedUserBean loggedUser;
    @Inject
    protected UserTextSearch userTextSearch;
    @Inject
    protected UrlIdEncoder idEncoder;
    @Inject
    protected AssessmentManager assessmentManager;
    @Inject protected AssessmentTokenSessionBean assessmentTokenSessionBean;


    protected long resourceId;
    protected AssessmentType assessmentType;
    protected List<UserData> peersForAssessment;
    protected String peerSearchTerm;
    protected Set<Long> existingPeerAssessors;
    protected List<Long> usersToExcludeFromPeerSearch;
    protected AssessmentRequestData assessmentRequestData = new AssessmentRequestData();
    protected BlindAssessmentMode blindAssessmentMode;
    protected boolean assessmentSubmitted;
    protected PaginationData paginationData = new PaginationData();
    protected boolean remindStudentToSubmitEvidenceSummary;
    private UserAssessmentTokenExtendedData userAssessmentTokenData;
    protected List<Long> assessorPoolUserIds;
    private boolean unassignedAssessmentExists;

    protected abstract void initInstructorAssessmentAssessor();
    public abstract void searchPeers();
    public abstract UserData getPeerAssessorFromAssessorPool();
    protected abstract LearningResourceType getResourceType();
    protected abstract void submitAssessmentRequest() throws IllegalDataStateException;
    protected abstract void notifyAssessorToAssessResource() throws IllegalDataStateException;
    protected abstract boolean shouldStudentBeRemindedToSubmitEvidenceSummary();

    private void initAssessorIfNeeded() {
        if (assessmentType == AssessmentType.INSTRUCTOR_ASSESSMENT) {
            initInstructorAssessmentAssessor();
        } else if (isAssessorPredetermined()) {
            //if it is blinded peer assessment or assessment tokens are enabled, assessor should be predetermined, student should not be able to choose assessor.
            setPeerAssessorFromThePool();
        } else if (canStudentChooseAssessor()) {
            //if student can choose assessor we do not set assessor but assessment is treated as new, at least until assessor is set
            assessmentRequestData.setNewAssessment(true);
        }
        setAssessmentSubmitted();
    }

    protected void setAssessmentSubmitted() {
        assessmentSubmitted = !assessmentRequestData.isNewAssessment() && assessmentRequestData.isAssessorSet() && checkIfAssessmentIsSubmitted();
    }

    protected abstract boolean checkIfAssessmentIsSubmitted();

    private boolean isAssessorPredetermined() {
        return assessmentType == AssessmentType.PEER_ASSESSMENT &&
                ((blindAssessmentMode == BlindAssessmentMode.BLIND || blindAssessmentMode == BlindAssessmentMode.DOUBLE_BLIND)
                        || userAssessmentTokenData.isAssessmentTokensEnabled());
    }

    public boolean canStudentChooseAssessor() {
        return assessmentType == AssessmentType.PEER_ASSESSMENT &&  !isAssessorPredetermined();
    }

    private void setOrInitAssessor(UserData assessor) {
        if (assessor != null) {
            setAssessor(assessor);
        } else {
            initAssessorIfNeeded();
        }
    }

    /**
     * Initializes initial data that need to be set before any other logic or initialization
     * takes place
     *
     * @param resourceId
     * @param targetResourceId
     * @param assessmentType
     * @param blindAssessmentMode
     */
    protected void initCommonInitialData(long resourceId, long targetResourceId, AssessmentType assessmentType, BlindAssessmentMode blindAssessmentMode) {
        this.resourceId = resourceId;
        this.assessmentType = assessmentType;
        usersToExcludeFromPeerSearch = Arrays.asList(loggedUser.getUserId());
        this.blindAssessmentMode = blindAssessmentMode;
        populateAssessmentRequestFields(targetResourceId);
    }

    /**
     * Initializes other data (other than initial data), should be called
     * after initial data is already initialized. This method is separated from
     * {@link #initCommonInitialData(long, long, AssessmentType, BlindAssessmentMode)}
     * to provide a way for classes inheriting this class to initialize their specific
     * 'initial' data before this method is called
     *
     * @param assessor
     */
    protected void initOtherCommonData(UserData assessor) {
        //init existing peer assessors if peer assessment
        if (assessmentType == AssessmentType.PEER_ASSESSMENT) {
            existingPeerAssessors = getExistingPeerAssessors();
        }
        initUserAssessmentTokenDataIfNeeded(assessor);
        setOrInitAssessor(assessor);
        determineWhetherStudentShouldBeRemindedToSubmitEvidenceSummary();
        initAssessorPoolUserIds();
        initUnassignedAssessmentExistsFlagIfNeeded();
    }

    private void initUnassignedAssessmentExistsFlagIfNeeded() {
        if (assessmentType == AssessmentType.PEER_ASSESSMENT) {
            unassignedAssessmentExists = isThereUnassignedAssessmentForThisUser();
        }
    }

    protected abstract boolean isThereUnassignedAssessmentForThisUser();

    private void initAssessorPoolUserIds() {
        if (canStudentChooseAssessor()) {
            assessorPoolUserIds = loadAssessorPoolUserIds();
        }
    }

    protected abstract List<Long> loadAssessorPoolUserIds();

    private void initUserAssessmentTokenDataIfNeeded(UserData assessor) {
        if (assessmentType == AssessmentType.PEER_ASSESSMENT && (assessor == null || !existingPeerAssessors.contains(assessor.getId()))) {
            //if new assessment load user assessment token data
            userAssessmentTokenData = loadUserAssessmentTokenDataAndRefreshInSession();
            assessmentRequestData.setNumberOfTokensToSpend(userAssessmentTokenData.getNumberOfTokensSpentPerRequest());
        }
    }

    /**
     * Loads user assessment token data, refreshes token data in session and returns loaded data
     *
     * @return
     */
    protected abstract UserAssessmentTokenExtendedData loadUserAssessmentTokenDataAndRefreshInSession();

    protected abstract Set<Long> getExistingPeerAssessors();

    public void resetAskForAssessmentModal() {
        assessmentRequestData.resetAssessorData();
        peersForAssessment = null;
        peerSearchTerm = null;
    }

    public void resetPeerAssessor() {
        resetAskForAssessmentModal();
        //after peer assessor is reset and there is no assessor assigned, assessment is treated as new
        assessmentRequestData.setNewAssessment(true);
    }

    public void setAssessor(UserData assessorData) {
        assessmentRequestData.setAssessorId(assessorData.getId());
        assessmentRequestData.setAssessorFullName(assessorData.getFullName());
        assessmentRequestData.setAssessorAvatarUrl(assessorData.getAvatarUrl());
        assessmentRequestData.setNewAssessment(assessmentType == AssessmentType.PEER_ASSESSMENT
                && !existingPeerAssessors.contains(assessmentRequestData.getAssessorId()));
        setAssessmentSubmitted();
    }

    public void setPeerAssessorFromThePool() {
        resetAskForAssessmentModal();

        UserData randomPeer = getPeerAssessorFromAssessorPool();

        if (randomPeer != null) {
            assessmentRequestData.setAssessorId(randomPeer.getId());
            assessmentRequestData.setAssessorFullName(randomPeer.getFullName());
            assessmentRequestData.setAssessorAvatarUrl(randomPeer.getAvatarUrl());
        }
        //when assessor is set from the pool assessment is new even if there is no available assessor
        assessmentRequestData.setNewAssessment(true);
    }

    public void submitAssessment() {
        try {
            submitAssessmentRequestAndReturnStatus();
        } catch (Exception e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error sending the assessment request");
        }
    }

    public boolean isValidRequest() {
        return
                //for assessment notification
                (!assessmentRequestData.isNewAssessment() && assessmentRequestData.isAssessorSet() && !assessmentSubmitted)
                        //for new assessment request when assessor is not set
                        || isValidAssessmentRequestWithoutAssessorSet()
                        //for new assessment request when assessor is set
                        || (assessmentRequestData.isNewAssessment() && assessmentRequestData.isAssessorSet() && (!userAssessmentTokenData.isAssessmentTokensEnabled() || userAssessmentTokenData.doesUserHaveEnoughTokensForOneRequest()));
    }

    public boolean submitAssessmentRequestAndReturnStatus() throws Exception {
        if (assessmentRequestData.isNewAssessment()) {
            submitAssessmentRequest();
            if (existingPeerAssessors != null) {
                existingPeerAssessors.add(assessmentRequestData.getAssessorId());
            }
            //refresh number of tokens in session
            if (userAssessmentTokenData.isAssessmentTokensEnabled()) {
                assessmentTokenSessionBean.tokensSpent(assessmentRequestData.getNumberOfTokensToSpend());
            }
            PageUtil.fireSuccessfulInfoMessage("Your assessment request is sent");
        } else {
            //notify
            notifyAssessorToAssessResource();
            PageUtil.fireSuccessfulInfoMessage("Assessor is notified");
        }

        resetAskForAssessmentModal();
        return true;
    }

    public void determineWhetherStudentShouldBeRemindedToSubmitEvidenceSummary() {
        this.remindStudentToSubmitEvidenceSummary = shouldStudentBeRemindedToSubmitEvidenceSummary();
    }

    private void populateAssessmentRequestFields(long targetResourceId) {
        this.assessmentRequestData.setStudentId(loggedUser.getUserId());
        this.assessmentRequestData.setResourceId(resourceId);
        this.assessmentRequestData.setTargetResourceId(targetResourceId);
    }

    public boolean isValidAssessmentRequestWithoutAssessorSet() {
             return isNewValidAssessmentRequestWithoutAvailableAssessor() && !unassignedAssessmentExists;
    }

    public boolean isNewAssessmentRequestWithoutAvailableAssessorWithExistingUnassignedAssessment() {
        return isNewValidAssessmentRequestWithoutAvailableAssessor() && unassignedAssessmentExists;
    }

    public boolean isNewValidAssessmentRequestWithoutAvailableAssessor() {
        return isNewPeerAssessmentWithoutAssessorSet() &&
                //tokens enabled and user has enough tokens
                ((userAssessmentTokenData.isAssessmentTokensEnabled() && userAssessmentTokenData.doesUserHaveEnoughTokensForOneRequest())
                        //blinded assessment is on
                        || (!userAssessmentTokenData.isAssessmentTokensEnabled() && isBlindAssessment())
                        //user can choose assessor but there is no available peer
                        || (canStudentChooseAssessor() && assessorPoolUserIds.isEmpty()));

    }

    public boolean isAssessorAvailableThroughSearch() {
        return canStudentChooseAssessor() && !assessorPoolUserIds.isEmpty();
    }

    public boolean isAssessorAvailable() {
        return assessmentRequestData.isAssessorSet() || isAssessorAvailableThroughSearch();
    }

    public boolean isNewAssessmentWithAvailableAssessorsNotYetChosen() {
        return isNewPeerAssessmentWithoutAssessorSet() && canStudentChooseAssessor() && !assessorPoolUserIds.isEmpty();
    }

    private boolean isNewPeerAssessmentWithoutAssessorSet() {
        return assessmentType == AssessmentType.PEER_ASSESSMENT && assessmentRequestData.isNewAssessment() && !assessmentRequestData.isAssessorSet();
    }

    public boolean isPeerBlindAssessment() {
        return assessmentType == AssessmentType.PEER_ASSESSMENT && isBlindAssessment();
    }

    private boolean isBlindAssessment() {
        return blindAssessmentMode == BlindAssessmentMode.BLIND || blindAssessmentMode == BlindAssessmentMode.DOUBLE_BLIND;
    }

    public List<UserData> getPeersForAssessment() {
        return peersForAssessment;
    }

    public String getPeerSearchTerm() {
        return peerSearchTerm;
    }

    public void setPeerSearchTerm(String peerSearchTerm) {
        this.peerSearchTerm = peerSearchTerm;
    }

    public AssessmentRequestData getAssessmentRequestData() {
        return assessmentRequestData;
    }

    public PaginationData getPaginationData() {
        return paginationData;
    }

    public void setPaginationData(PaginationData paginationData) {
        this.paginationData = paginationData;
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public long getResourceId() {
        return resourceId;
    }

    public BlindAssessmentMode getBlindAssessmentMode() {
        return blindAssessmentMode;
    }

    public boolean isRemindStudentToSubmitEvidenceSummary() {
        return remindStudentToSubmitEvidenceSummary;
    }

    public UserAssessmentTokenExtendedData getUserAssessmentTokenData() {
        return userAssessmentTokenData;
    }

    public boolean isAssessmentSubmitted() {
        return assessmentSubmitted;
    }
}