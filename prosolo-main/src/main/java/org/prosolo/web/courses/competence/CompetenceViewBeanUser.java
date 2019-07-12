package org.prosolo.web.courses.competence;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.AccessDeniedException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.LearningPathType;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.data.BasicObjectInfo;
import org.prosolo.services.nodes.data.CompetencyBasicObjectInfo;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.assessments.AskForCompetenceAssessmentBean;
import org.prosolo.web.learningevidence.SubmitEvidenceBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.stream.Collectors;

@ManagedBean(name = "competenceViewBean")
@Component("competenceViewBean")
@Scope("view")
public class CompetenceViewBeanUser implements Serializable {

    private static final long serialVersionUID = 9208762722353804216L;

    private static Logger logger = Logger.getLogger(CompetenceViewBeanUser.class);

    @Inject private LoggedUserBean loggedUser;
    @Inject private Competence1Manager competenceManager;
    @Inject private UrlIdEncoder idEncoder;
    @Inject private CommentBean commentBean;
    @Inject private CredentialManager credManager;
    @Inject private LearningEvidenceManager learningEvidenceManager;
    @Inject private LearningEvidenceSearchBean learningEvidenceSearchBean;
    @Inject private SubmitEvidenceBean submitEvidenceBean;
    @Inject private AskForCompetenceAssessmentBean askForAssessmentBean;

    private String credId;
    private long decodedCredId;
    private String compId;
    private long decodedCompId;
    private String commentId;
    private boolean justEnrolled;

    private CompetenceData1 competenceData;
    private ResourceAccessData access;
    private CommentsData commentsData;

    private LearningEvidenceData evidenceToRemove;

	private long nextCompToLearn;
    private boolean mandatoryOrder;

    private String credentialTitle;

    public void init() {
        decodedCompId = idEncoder.decodeId(compId);
        decodedCredId = idEncoder.decodeId(credId);

        if (decodedCompId > 0 && decodedCredId > 0) {
            try {
                // check if credential and competency are connected
                competenceManager.checkIfCompetenceIsPartOfACredential(decodedCredId, decodedCompId);

                RestrictedAccessResult<CompetenceData1> res = competenceManager
                        .getFullTargetCompetenceOrCompetenceData(decodedCredId, decodedCompId,
                                loggedUser.getUserId());
                unpackResult(res);

                /*
                 * if user does not have at least access to resource in read only mode throw access denied exception.
                 */
                if (!access.isCanRead()) {
                    throw new AccessDeniedException();
                }

                commentsData = CommentsData
                        .builder()
                        .resourceType(CommentedResourceType.Competence)
                        .resourceId(competenceData.getCompetenceId())
                        .isInstructor(false)
                        .isManagerComment(false)
                        .commentId(idEncoder.decodeId(commentId))
                        .credentialId(decodedCredId)
                        .build();
                commentBean.loadComments(commentsData);

                if (decodedCredId > 0) {
                    credentialTitle = credManager.getCredentialTitle(decodedCredId);
                }
                if (competenceData.getLearningPathType() == LearningPathType.EVIDENCE && competenceData.isEnrolled()) {
                    submitEvidenceBean.init(new LearningEvidenceData());
                }
                if (justEnrolled) {
                    PageUtil.fireSuccessfulInfoMessage(
                            "You have started the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase() + " " + competenceData.getTitle());
                }
            } catch (AccessDeniedException ade) {
                PageUtil.accessDenied();
            } catch (ResourceNotFoundException rnfe) {
                PageUtil.notFound();
            } catch (Exception e) {
                logger.error(e);
                PageUtil.fireErrorMessage(e.getMessage());
            }
        } else {
            PageUtil.notFound();
        }
    }

    public void initAskForAssessment(AssessmentType aType) {
		/*
		passing competence level blind assessment mode is fine in this context because if
		peer assessment request is initiated here it will always be new assessment request.
		For tutor assessment it does not matter where are we getting blind assessment mode from
		because blind assessment mode is of importance only for peer assessments.
		 */
        askForAssessmentBean.init(decodedCredId, decodedCompId, competenceData.getTargetCompId(), aType, competenceData.getAssessmentTypeConfig(aType).getBlindAssessmentMode());
    }

    private void unpackResult(RestrictedAccessResult<CompetenceData1> res) {
        competenceData = res.getResource();
        access = res.getAccess();
    }

    public boolean isCompetenceNextToLearn() {
        return decodedCompId == nextCompToLearn;
    }

    public boolean isCurrentUserCreator() {
        return competenceData == null || competenceData.getCreator() == null ? false :
                competenceData.getCreator().getId() == loggedUser.getUserId();
    }

    public boolean hasMoreActivities(int index) {
        return competenceData.getActivities().size() != index + 1;
    }

    public String getLabelForCompetence() {
        if(access.isCanEdit() && !competenceData.isEnrolled() && !competenceData.isPublished()) {
            return "(Unpublished)";
        } else {
            return "";
        }
    }

    /*
     * ACTIONS
     */

    public void enrollInCompetence() {
        try {
            competenceManager.enrollInCompetence(
                    decodedCredId, competenceData.getCompetenceId(), loggedUser.getUserId(), loggedUser.getUserContext());
            PageUtil.fireSuccessfulInfoMessage("You have started the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
            try {
                RestrictedAccessResult<CompetenceData1> res = competenceManager
                        .getFullTargetCompetenceOrCompetenceData(decodedCredId, decodedCompId,
                                loggedUser.getUserId());
                unpackResult(res);
                if (competenceData.getLearningPathType() == LearningPathType.EVIDENCE) {
                    //student enrolled so he can now upload/post evidence
                    submitEvidenceBean.init(new LearningEvidenceData());
                }
            } catch (Exception e) {
                logger.error("error", e);
                PageUtil.fireErrorMessage("Error loading the data, try to refresh the page");
            }
        } catch (DbConnectionException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error starting the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
        }
    }

    public void setEvidenceToRemove(LearningEvidenceData evidenceToRemove) {
        this.evidenceToRemove = evidenceToRemove;
    }

    public void prepareExistingEvidenceSearch() {
        if (!learningEvidenceSearchBean.isInitialized()) {
            learningEvidenceSearchBean.init(competenceData.getEvidences().stream().map(LearningEvidenceData::getId).collect(Collectors.toList()));
        }
        submitEvidenceBean.resetEvidence();
    }

    public void postFileEvidence() {
        submitEvidenceBean.preparePostFileEvidence();
        postEvidence();
    }

    public void postUrlEvidence() {

        submitEvidenceBean.preparePostUrlEvidence();
        postEvidence();

    }

    public void postTextEvidence() {
        submitEvidenceBean.preparePostTextEvidence();
        postEvidence();
    }

    public void postEvidence() {
        try {
            LearningEvidenceData newEvidence = learningEvidenceManager.postEvidenceAndAttachItToCompetence(
                    competenceData.getTargetCompId(), submitEvidenceBean.getEvidence(), loggedUser.getUserContext());
            competenceData.getEvidences().add(newEvidence);
            if (learningEvidenceSearchBean.isInitialized()) {
                //if evidence search bean is initialized exclude just added evidence from search and reset search
                learningEvidenceSearchBean.excludeEvidenceFromFutureSearches(newEvidence.getId());
                learningEvidenceSearchBean.resetAndSearch();
            }
            submitEvidenceBean.resetEvidence();
            PageUtil.fireSuccessfulInfoMessage("Evidence successfully added");
        } catch (RuntimeException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error posting the evidence");
        }
    }

    public void removeEvidenceFromCompetence() {
        try {
            if (evidenceToRemove != null) {
                learningEvidenceManager.removeEvidenceFromCompetence(evidenceToRemove.getCompetenceEvidenceId());
                competenceData.getEvidences().remove(evidenceToRemove);
                if (learningEvidenceSearchBean.isInitialized()) {
                    //if evidence search bean is initialized include removed evidence in search and reset search
                    learningEvidenceSearchBean.includeEvidenceInFutureSearches(evidenceToRemove.getId());
                    learningEvidenceSearchBean.resetAndSearch();
                }
                evidenceToRemove = null;
                PageUtil.fireSuccessfulInfoMessage("Evidence successfully removed");
            } else {
                logger.debug("Evidence to remove is null which means that user double-clicked the remove evidence button");
            }
        } catch (DbConnectionException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error removing the evidence");
        }
    }

    public void completeCompetence() {
        try {
            competenceManager.completeCompetence(
                    competenceData.getTargetCompId(),
                    loggedUser.getUserContext());
            competenceData.setProgress(100);

            PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getLabel("competence").toLowerCase() + " has been completed");
        } catch (Exception e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error marking the " + ResourceBundleUtil.getLabel("competence").toLowerCase() + " as completed");
        }
    }

    public void saveEvidenceSummary() {
        try {
            if (competenceData.isEvidenceSummaryChanged()) {
                competenceManager.saveEvidenceSummary(competenceData.getTargetCompId(), competenceData.getEvidenceSummary());
                competenceData.resetTrackingForEvidenceSummary();
            }
            PageUtil.fireSuccessfulInfoMessage("Evidence summary saved");
        } catch (Exception e) {
            logger.error("Error", e);
            //reset evidence summary to previous value
            competenceData.setEvidenceSummary(competenceData.getEvidenceSummaryBeforeUpdate());
            PageUtil.fireErrorMessage("Error saving the evidence summary");
        }
    }

	/*
	VALIDATORS
	 */

    /*
     * GETTERS / SETTERS
     */

    public CompetenceData1 getCompetenceData() {
        return competenceData;
    }

    public String getCredId() {
        return credId;
    }

    public void setCredId(String credId) {
        this.credId = credId;
    }

    public long getDecodedCredId() {
        return decodedCredId;
    }

    public void setDecodedCredId(long decodedCredId) {
        this.decodedCredId = decodedCredId;
    }

    public String getCompId() {
        return compId;
    }

    public void setCompId(String compId) {
        this.compId = compId;
    }

    public long getDecodedCompId() {
        return decodedCompId;
    }

    public void setDecodedCompId(long decodedCompId) {
        this.decodedCompId = decodedCompId;
    }

    public void setCompetenceData(CompetenceData1 competenceData) {
        this.competenceData = competenceData;
    }

    public CommentsData getCommentsData() {
        return commentsData;
    }

    public void setCommentsData(CommentsData commentsData) {
        this.commentsData = commentsData;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public boolean isMandatoryOrder() {
        return mandatoryOrder;
    }

    public void setMandatoryOrder(boolean mandatoryOrder) {
        this.mandatoryOrder = mandatoryOrder;
    }

    public ResourceAccessData getAccess() {
        return access;
    }

    public boolean isJustEnrolled() {
        return justEnrolled;
    }

    public void setJustEnrolled(boolean justEnrolled) {
        this.justEnrolled = justEnrolled;
    }

    public String getCredentialTitle() {
        return credentialTitle;
    }

}
