package org.prosolo.web.assessments;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.AssessmentStatus;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.ActivityAssessmentData;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.assessment.data.CompetenceAssessmentDataFull;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.RubricCriteriaGradeData;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.UserBasicData;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.AssessmentTokenSessionBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author stefanvuckovic
 */

@ManagedBean(name = "competenceAssessmentBean")
@Component("competenceAssessmentBean")
@Scope("view")
public class StudentCompetenceAssessmentBean extends CompetenceAssessmentBean implements AssessmentCommentsAware {

    private static final long serialVersionUID = 1614497321079210618L;

    private static Logger logger = Logger.getLogger(StudentCompetenceAssessmentBean.class);

    @Inject private RubricManager rubricManager;
    @Inject private ActivityAssessmentBean activityAssessmentBean;
    @Inject private AskForCompetenceAssessmentBean askForAssessmentBean;
    @Inject private AssessmentTokenSessionBean assessmentTokenSessionBean;
    @Inject private UserManager userManager;

    private LearningResourceType currentResType;

    @Override
    boolean canAccessPreLoad() {
        return true;
    }

    @Override
    boolean canAccessPostLoad() {
        return isUserAssessedStudentInCurrentContext() || isUserAssessorInCurrentContext();
    }

    public void markActivityAssessmentDiscussionRead() {
        String encodedActivityDiscussionId = getEncodedAssessmentIdFromRequest();

        if (!StringUtils.isBlank(encodedActivityDiscussionId)) {
            getAssessmentManager().markActivityAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
                    getIdEncoder().decodeId(encodedActivityDiscussionId));
            Optional<ActivityAssessmentData> seenActivityAssessment = getActivityAssessmentByEncodedId(
                    encodedActivityDiscussionId);
            seenActivityAssessment.ifPresent(data -> data.setAllRead(true));
        }
    }

    private Optional<ActivityAssessmentData> getActivityAssessmentByEncodedId(String encodedActivityDiscussionId) {
        if (getCompetenceAssessmentData().getActivityAssessmentData() != null) {
            for (ActivityAssessmentData act : getCompetenceAssessmentData().getActivityAssessmentData()) {
                if (encodedActivityDiscussionId.equals(act.getEncodedActivityAssessmentId())) {
                    return Optional.of(act);
                }
            }
        }
        return Optional.empty();
    }

    public void markCompetenceAssessmentDiscussionRead() {
        String encodedAssessmentId = getEncodedAssessmentIdFromRequest();

        if (!StringUtils.isBlank(encodedAssessmentId)) {
            long assessmentId = getIdEncoder().decodeId(encodedAssessmentId);
            getAssessmentManager().markCompetenceAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
                    assessmentId);
            getCompetenceAssessmentData().setAllRead(true);
        }
    }

    private String getEncodedAssessmentIdFromRequest() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        return params.get("assessmentEncId");
    }

    public void approveCompetence() {
        try {
            getAssessmentManager().approveCompetence(getCompetenceAssessmentData().getCompetenceAssessmentId(), loggedUserBean.getUserContext());
            getCompetenceAssessmentData().markAssessmentAsSubmitted();
            getCompetenceAssessmentData().setAssessorNotified(false);
            getCompetenceAssessmentData().setDateSubmitted(DateUtil.getMillisFromDate(new Date()));

            PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getMessage("label.competence") + " assessment is submitted");

            try {
                refreshTokenSessionData();
            } catch (Exception e) {
                logger.error("error", e);
                PageUtil.fireErrorMessage("Error refreshing the data");
            }
        } catch (Exception e) {
            logger.error("Error submitting the assessment", e);
            PageUtil.fireErrorMessage("Error submitting the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase() + " assessment");
        }
    }

    private void refreshTokenSessionData() {
        assessmentTokenSessionBean.refreshData(
                userManager.getUserAssessmentTokenData(loggedUserBean.getUserId()));
    }

    public void acceptAssessmentRequest() {
        try {
            getAssessmentManager().acceptCompetenceAssessmentRequest(getCompetenceAssessmentData().getCompetenceAssessmentId(), loggedUserBean.getUserContext());
            PageUtil.fireSuccessfulInfoMessageAcrossPages("Assessment request has been accepted");
            PageUtil.redirect("/assessments/my/competences/" + getCompetenceAssessmentId());
        } catch (Exception e) {
            logger.error("error", e);
            PageUtil.fireErrorMessage("Error accepting assessment request");
        }
    }

    public void declineAssessmentRequest() {
        try {
            getAssessmentManager().declineCompetenceAssessmentRequest(getCompetenceAssessmentData().getCompetenceAssessmentId(), loggedUserBean.getUserContext());
            PageUtil.fireSuccessfulInfoMessageAcrossPages("Assessment request has been declined");
            PageUtil.redirect("assessments/my/competences");
        } catch (Exception e) {
            logger.error("error", e);
            PageUtil.fireErrorMessage("Error declining assessment request");
        }
    }

    public void withdrawFromAssessment() {
        try {
            getAssessmentManager().declinePendingCompetenceAssessment(getCompetenceAssessmentData().getCompetenceAssessmentId(), loggedUserBean.getUserContext());
            PageUtil.fireSuccessfulInfoMessageAcrossPages("You have withdrawn from the assessment");
            PageUtil.redirect("assessments/my/competences");
        } catch (Exception e) {
            logger.error("error", e);
            PageUtil.fireErrorMessage("Error withdrawing from the assessment");
        }
    }

    @Override
    public GradeData getGradeData() {
        return getCompetenceAssessmentData() != null ? getCompetenceAssessmentData().getGradeData() : null;
    }

    @Override
    public RubricCriteriaGradeData getRubricForLearningResource() {
        return rubricManager.getRubricDataForCompetence(
                getCompetenceAssessmentData().getCompetenceId(),
                getCompetenceAssessmentData().getCompetenceAssessmentId(),
                true);
    }

    //prepare for grading
    public void prepareLearningResourceAssessmentForGrading(CompetenceAssessmentDataFull assessment) {
        setCompetenceAssessmentData(assessment);
        initializeGradeData();
        this.currentResType = LearningResourceType.COMPETENCE;
    }

    public void prepareLearningResourceAssessmentForCommenting() {
        prepareLearningResourceAssessmentForCommenting(getCompetenceAssessmentData());
    }

    public void prepareLearningResourceAssessmentForGrading(ActivityAssessmentData assessment) {
        activityAssessmentBean.prepareLearningResourceAssessmentForGrading(assessment);
        currentResType = LearningResourceType.ACTIVITY;
    }

    //prepare for commenting
    public void prepareLearningResourceAssessmentForCommenting(ActivityAssessmentData assessment) {
        activityAssessmentBean.prepareLearningResourceAssessmentForCommenting(assessment);
        currentResType = LearningResourceType.ACTIVITY;
    }

    //prepare for commenting
    public void prepareLearningResourceAssessmentForCommenting(CompetenceAssessmentDataFull assessment) {
        try {
            if (!assessment.isMessagesInitialized()) {
                assessment.populateDiscussionMessages(getAssessmentManager()
                        .getCompetenceAssessmentDiscussionMessages(assessment.getCompetenceAssessmentId()));
                assessment.setMessagesInitialized(true);
            }
            setCompetenceAssessmentData(assessment);
            currentResType = LearningResourceType.COMPETENCE;
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            PageUtil.fireErrorMessage("Error trying to initialize assessment comments");
        }
    }

    public void prepareLearningResourceAssessmentForApproving(CompetenceAssessmentDataFull assessment) {
        try {
            setCompetenceAssessmentData(assessment);
            currentResType = LearningResourceType.COMPETENCE;
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            PageUtil.fireErrorMessage("Error trying to initialize assessment comments");
        }
    }

    //actions based on currently selected resource type

    public long getCurrentCompetenceAssessmentId() {
        if (currentResType == null) {
            return 0;
        }
        switch (currentResType) {
            case ACTIVITY:
                return activityAssessmentBean.getActivityAssessmentData().getCompAssessmentId();
            case COMPETENCE:
                return getCompetenceAssessmentData().getCompetenceAssessmentId();
        }
        return 0;
    }

    @Override
    public List<AssessmentDiscussionMessageData> getCurrentAssessmentMessages() {
        if (currentResType == null) {
            return null;
        }
        switch (currentResType) {
            case ACTIVITY:
                return activityAssessmentBean.getActivityAssessmentData().getActivityDiscussionMessageData();
            case COMPETENCE:
                return getCompetenceAssessmentData().getMessages();
        }
        return null;
    }

    @Override
    public BlindAssessmentMode getCurrentBlindAssessmentMode() {
        if (currentResType == null) {
            return null;
        }
        switch (currentResType) {
            case ACTIVITY:
                return activityAssessmentBean.getActivityAssessmentData().getCompAssessment().getBlindAssessmentMode();
            case COMPETENCE:
                return getCompetenceAssessmentData().getBlindAssessmentMode();
        }
        return null;
    }

    @Override
    public LearningResourceAssessmentBean getCurrentAssessmentBean() {
        if (currentResType == null) {
            return null;
        }
        switch (currentResType) {
            case ACTIVITY:
                return activityAssessmentBean;
            case COMPETENCE:
                return this;
        }
        return null;
    }

    public void updateAssessmentGrade() {
        try {
            switch (currentResType) {
                case ACTIVITY:
                    activityAssessmentBean.updateGrade();
                    break;
                case COMPETENCE:
                    updateGrade();
                    break;
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    public long getCurrentAssessmentId() {
        if (currentResType == null) {
            return 0;
        }
        switch (currentResType) {
            case ACTIVITY:
                return getIdEncoder().decodeId(activityAssessmentBean.getActivityAssessmentData().getEncodedActivityAssessmentId());
            case COMPETENCE:
                return getCompetenceAssessmentData().getCompetenceAssessmentId();
        }
        return 0;
    }

    public boolean hasStudentCompletedCurrentResource() {
        if (currentResType == null) {
            return false;
        }
        switch (currentResType) {
            case ACTIVITY:
                return activityAssessmentBean.getActivityAssessmentData().isCompleted();
            case COMPETENCE:
                //for now
                return true;
        }
        return false;
    }

    public String getCurrentResTitle() {
        if (currentResType == null) {
            return null;
        }
        switch (currentResType) {
            case ACTIVITY:
                return activityAssessmentBean.getActivityAssessmentData().getTitle();
            case COMPETENCE:
                return getCompetenceAssessmentData().getTitle();
        }
        return null;
    }

    public GradeData getCurrentGradeData() {
        if (currentResType == null) {
            return null;
        }
        switch (currentResType) {
            case ACTIVITY:
                return activityAssessmentBean.getActivityAssessmentData().getGrade();
            case COMPETENCE:
                return getCompetenceAssessmentData().getGradeData();
        }
        return null;
    }

    public long getCurrentAssessorId() {
        if (currentResType == null) {
            return 0;
        }
        switch (currentResType) {
            case ACTIVITY:
                return activityAssessmentBean.getActivityAssessmentData().getAssessorId();
            case COMPETENCE:
                return getCompetenceAssessmentData().getAssessorId();
        }
        return 0;
    }

    public long getCurrentStudentId() {
        if (currentResType == null) {
            return 0;
        }
        switch (currentResType) {
            case ACTIVITY:
                return activityAssessmentBean.getActivityAssessmentData().getUserId();
            case COMPETENCE:
                return getCompetenceAssessmentData().getStudentId();
        }
        return 0;
    }

    public UserBasicData getStudentData() {
        return getCompetenceAssessmentData() != null
                ? new UserBasicData(getCompetenceAssessmentData().getStudentId(), getCompetenceAssessmentData().getStudentFullName(), getCompetenceAssessmentData().getStudentAvatarUrl())
                : null;
    }

    //actions based on currently selected resource type end

	/*
	ACTIONS
	 */

    //comment actions

    @Override
    public void editComment(String newContent, String messageEncodedId) {
        long messageId = getIdEncoder().decodeId(messageEncodedId);
        try {
            getAssessmentManager().editCompetenceAssessmentMessage(messageId, loggedUserBean.getUserId(), newContent);
            AssessmentDiscussionMessageData msg = null;
            for (AssessmentDiscussionMessageData messageData : getCompetenceAssessmentData().getMessages()) {
                if (messageData.getEncodedMessageId().equals(messageEncodedId)) {
                    msg = messageData;
                    break;
                }
            }
            msg.setDateUpdated(new Date());
            msg.setDateUpdatedFormat(DateUtil.createUpdateTime(msg.getDateUpdated()));
//			//because comment is edit now, it should be added as first in a list because list is sorted by last edit date
//			competenceAssessmentData.getMessages().remove(msg);
//			competenceAssessmentData.getMessages().add(0, msg);
        } catch (DbConnectionException e) {
            logger.error("Error editing message with id : " + messageId, e);
            PageUtil.fireErrorMessage("Error editing message");
        }
    }

    @Override
    protected void addComment() {
        try {
            long assessmentId = getCompetenceAssessmentData().getCompetenceAssessmentId();
            UserContextData userContext = loggedUserBean.getUserContext();

            AssessmentDiscussionMessageData newComment = getAssessmentManager().addCommentToCompetenceAssessmentDiscussion(
                    assessmentId, loggedUserBean.getUserId(), getNewCommentValue(), userContext);

            addNewCommentToAssessmentData(newComment);
        } catch (Exception e) {
            logger.error("Error submitting assessment data", e);
            PageUtil.fireErrorMessage("Error submitting the assessment");
        }
    }

    private void addNewCommentToAssessmentData(AssessmentDiscussionMessageData newComment) {
        if (loggedUserBean.getUserId() == getCompetenceAssessmentData().getAssessorId()) {
            newComment.setSenderAssessor(true);
        }
        getCompetenceAssessmentData().getMessages().add(newComment);
        getCompetenceAssessmentData().setNumberOfMessages(getCompetenceAssessmentData().getNumberOfMessages() + 1);
    }

    // grading actions

    @Override
    public void updateGrade() throws DbConnectionException, IllegalDataStateException {
        try {
            getCompetenceAssessmentData().setGradeData(getAssessmentManager().updateGradeForCompetenceAssessment(
                    getCompetenceAssessmentData().getCompetenceAssessmentId(),
                    getCompetenceAssessmentData().getGradeData(), loggedUserBean.getUserContext()));
            //when grade is updated assessor notification is removed
            getCompetenceAssessmentData().setAssessorNotified(false);

            PageUtil.fireSuccessfulInfoMessage("The grade has been updated");
        } catch (DbConnectionException | IllegalDataStateException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error updating the grade");
            throw e;
        }
    }

    @Override
    public AssessmentType getType() {
        return getCompetenceAssessmentData().getType();
    }

    //STUDENT ONLY CODE
    public void initAskForAssessment() {
        initAskForAssessment(getCompetenceAssessmentData());
    }

    public void initAskForAssessment(CompetenceAssessmentDataFull compAssessment) {
        UserData assessor = null;
        if (compAssessment.getAssessorId() > 0) {
            assessor = new UserData();
            assessor.setId(compAssessment.getAssessorId());
            assessor.setFullName(compAssessment.getAssessorFullName());
            assessor.setAvatarUrl(compAssessment.getAssessorAvatarUrl());
        }
		/*
		in this context we are always initiating assessor notification request and never
		new assessment request so blind assessment mode is retrieved from competence assessment
		 */
        askForAssessmentBean.init(compAssessment.getCredentialId(), compAssessment.getCompetenceId(), compAssessment.getTargetCompetenceId(), compAssessment.getType(), assessor, compAssessment.getBlindAssessmentMode());

        setCompetenceAssessmentData(compAssessment);
    }

    public void submitAssessment() {
        try {
            boolean success = askForAssessmentBean.submitAssessmentRequestAndReturnStatus();
            if (success) {
                getCompetenceAssessmentData().setAssessorNotified(true);
                getCompetenceAssessmentData().setLastAskedForAssessment(new Date().getTime());
            }
        } catch (Exception e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error sending the assessment request");
        }
    }
    //STUDENT ONLY CODE

    /*
     * GETTERS / SETTERS
     */

    public LearningResourceType getCurrentResType() {
        return currentResType;
    }

}
