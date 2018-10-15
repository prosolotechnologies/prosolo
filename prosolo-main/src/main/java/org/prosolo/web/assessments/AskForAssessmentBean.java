package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.search.UserTextSearch;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentRequestData;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.PaginationData;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
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

    protected long resourceId;
    protected AssessmentType assessmentType;
    protected List<UserData> peersForAssessment;
    protected String peerSearchTerm;
    protected Set<Long> existingPeerAssessors;
    protected List<Long> usersToExcludeFromPeerSearch;
    protected boolean noRandomAssessor = false;
    protected AssessmentRequestData assessmentRequestData = new AssessmentRequestData();
    protected BlindAssessmentMode blindAssessmentMode;
    protected PaginationData paginationData = new PaginationData();
    protected boolean remindStudentToSubmitEvidenceSummary;

    protected abstract void initInstructorAssessmentAssessor();
    public abstract void searchPeers();
    public abstract UserData getRandomPeerForAssessor();
    protected abstract LearningResourceType getResourceType();
    protected abstract void submitAssessmentRequest() throws IllegalDataStateException;
    protected abstract void notifyAssessorToAssessResource();
    protected abstract boolean shouldStudentBeRemindedToSubmitEvidenceSummary();

    public void init(long resourceId, long targetResourceId, AssessmentType assessmentType, BlindAssessmentMode blindAssessmentMode) {
        initAssessmentBasicInfo(resourceId, targetResourceId, assessmentType, blindAssessmentMode);
        if (assessmentType == AssessmentType.INSTRUCTOR_ASSESSMENT) {
            initInstructorAssessmentAssessor();
        } else if (assessmentType == AssessmentType.PEER_ASSESSMENT && (blindAssessmentMode == BlindAssessmentMode.BLIND || blindAssessmentMode == BlindAssessmentMode.DOUBLE_BLIND)) {
            chooseRandomPeerForAssessor();
        }
        determineWhetherStudentShouldBeRemindedToSubmitEvidenceSummary();
    }

    /**
     * init ask for assessment with assessor info
     *
     * @param resourceId
     * @param targetResourceId
     * @param assessmentType
     * @param assessor
     */
    public void init(long resourceId, long targetResourceId, AssessmentType assessmentType, UserData assessor, BlindAssessmentMode blindAssessmentMode) {
        initAssessmentBasicInfo(resourceId, targetResourceId, assessmentType, blindAssessmentMode);
        if (assessor != null) {
            assessmentRequestData.setAssessorId(assessor.getId());
            assessmentRequestData.setAssessorFullName(assessor.getFullName());
            assessmentRequestData.setAssessorAvatarUrl(assessor.getAvatarUrl());
        }
        determineWhetherStudentShouldBeRemindedToSubmitEvidenceSummary();
    }

    private void initAssessmentBasicInfo(long resourceId, long targetResourceId, AssessmentType assessmentType, BlindAssessmentMode blindAssessmentMode) {
        this.resourceId = resourceId;
        this.assessmentType = assessmentType;
        usersToExcludeFromPeerSearch = Arrays.asList(loggedUser.getUserId());
        this.blindAssessmentMode = blindAssessmentMode;
        populateAssessmentRequestFields(targetResourceId);
    }

    /**
     * new assessment request is when assessment request is submitted to new peer
     */
    private boolean isNewAssessmentRequest() {
        return assessmentType == AssessmentType.PEER_ASSESSMENT
                && !existingPeerAssessors.contains(assessmentRequestData.getAssessorId());
    }

    public void resetAskForAssessmentModal() {
        noRandomAssessor = false;
        assessmentRequestData.resetAssessorData();
        peersForAssessment = null;
        peerSearchTerm = null;
    }

    public void setAssessor(UserData assessorData) {
        assessmentRequestData.setAssessorId(assessorData.getId());
        assessmentRequestData.setAssessorFullName(assessorData.getFullName());
        assessmentRequestData.setAssessorAvatarUrl(assessorData.getAvatarUrl());
        assessmentRequestData.setNewAssessment(isNewAssessmentRequest());

        noRandomAssessor = false;
    }

    public void chooseRandomPeerForAssessor() {
        resetAskForAssessmentModal();

        UserData randomPeer = getRandomPeerForAssessor();

        if (randomPeer != null) {
            assessmentRequestData.setAssessorId(randomPeer.getId());
            assessmentRequestData.setAssessorFullName(randomPeer.getFullName());
            assessmentRequestData.setAssessorAvatarUrl(randomPeer.getAvatarUrl());
            assessmentRequestData.setNewAssessment(true);
            noRandomAssessor = false;
        } else {
            noRandomAssessor = true;
        }
    }

    public void submitAssessment() {
        try {
            submitAssessmentRequestAndReturnStatus();
        } catch (Exception e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error sending the assessment request");
        }
    }

    public boolean submitAssessmentRequestAndReturnStatus() throws Exception {
        boolean status;
        if (this.assessmentRequestData.isAssessorSet()) {
            if (assessmentRequestData.isNewAssessment()) {
                submitAssessmentRequest();
                if (existingPeerAssessors != null) {
                    existingPeerAssessors.add(assessmentRequestData.getAssessorId());
                }
                PageUtil.fireSuccessfulInfoMessage("Your assessment request is sent");
            } else {
                //notify
                notifyAssessorToAssessResource();
                PageUtil.fireSuccessfulInfoMessage("Assessor is notified");
            }
            status = true;
        } else {
            logger.error("Student " + loggedUser.getFullName() + " tried to submit assessment request for " + getResourceType().name().toLowerCase() + " : "
                    + resourceId + ", but " + getResourceType().name().toLowerCase() + " has no assessor/instructor set!");
            PageUtil.fireErrorMessage("No assessor set");
            status = false;
        }
        resetAskForAssessmentModal();
        return status;
    }

    public void determineWhetherStudentShouldBeRemindedToSubmitEvidenceSummary() {
        this.remindStudentToSubmitEvidenceSummary = shouldStudentBeRemindedToSubmitEvidenceSummary();
    }

    private void populateAssessmentRequestFields(long targetResourceId) {
        this.assessmentRequestData.setStudentId(loggedUser.getUserId());
        this.assessmentRequestData.setResourceId(resourceId);
        this.assessmentRequestData.setTargetResourceId(targetResourceId);
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

    public boolean isNoRandomAssessor() {
        return noRandomAssessor;
    }

    public void setNoRandomAssessor(boolean noRandomAssessor) {
        this.noRandomAssessor = noRandomAssessor;
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
}