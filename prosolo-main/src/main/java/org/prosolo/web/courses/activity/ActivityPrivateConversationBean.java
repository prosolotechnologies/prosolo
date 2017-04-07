package org.prosolo.web.courses.activity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.ActivityDiscussionMessage;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.assessments.ActivityAssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Bojan
 *
 */

@ManagedBean(name = "activityPrivateConversationBean")
@Component("activityPrivateConversationBean")
@Scope("view")
public class ActivityPrivateConversationBean implements Serializable {

	private static final long serialVersionUID = 1241550556017187433L;

	private static Logger logger = Logger.getLogger(ActivityPrivateConversationBean.class);

	@Inject
	private Activity1Manager activityManager;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private EventFactory eventFactory;
	@Inject
	private ActivityResultBean actResultBean;

	private String actId;
	private long decodedActId;
	private String compId;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;
	private String targetActId;
	private long decodedTargetActId;
	private String commentId;

	private ActivityData activity;

	private boolean hasInstructorCapability;

	private ActivityResultData currentResult;
	// adding new comment
	private String newCommentValue;

	public void initIndividualResponse() {
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);
		decodedTargetActId = idEncoder.decodeId(targetActId);
		if (decodedActId > 0 && decodedCompId > 0 && decodedCredId > 0 && decodedTargetActId > 0) {
			hasInstructorCapability = loggedUserBean.hasCapability("BASIC.INSTRUCTOR.ACCESS");
			try {
				activity = activityManager.getActivityDataWithStudentResultsForManager(decodedCredId, decodedCompId,
						decodedActId, decodedTargetActId, hasInstructorCapability, true, false, 0, 0, null);
				if (activity.getStudentResults() != null && !activity.getStudentResults().isEmpty()) {
					currentResult = activity.getStudentResults().get(0);
					loadAdditionalData(currentResult);
					if (commentId != null) {
						currentResult.getResultComments().setCommentId(idEncoder.decodeId(commentId));
						initializeResultCommentsIfNotInitialized(currentResult);
					}
				} else {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				}
			} catch (Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error while loading activity results");
			}
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				logger.error(ioe);
			}
		}
	}

	public void initializeResultCommentsIfNotInitialized(ActivityResultData result) {
		try {
			actResultBean.initializeResultCommentsIfNotInitialized(result);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	// assessment begin
	public void loadActivityDiscussion(ActivityResultData result) {
		try {
			ActivityAssessmentData assessment = result.getAssessment();
			if (!assessment.isMessagesInitialized()) {
				if (assessment.getEncodedDiscussionId() != null && !assessment.getEncodedDiscussionId().isEmpty()) {
					assessment.setActivityDiscussionMessageData(assessmentManager.getActivityDiscussionMessages(
							idEncoder.decodeId(assessment.getEncodedDiscussionId()), assessment.getAssessorId()));
				}
				assessment.setMessagesInitialized(true);
			}
			this.currentResult = result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage("Error while trying to initialize private conversation messages");
		}
	}

	private void loadAdditionalData(ActivityResultData result) {
		ActivityAssessmentData assessment = result.getAssessment();
		CompetenceAssessment ca = assessmentManager.getDefaultCompetenceAssessment(decodedCredId, decodedCompId,
				result.getUser().getId());
		assessment.setCompAssessmentId(ca.getId());
		assessment.setCredAssessmentId(ca.getCredentialAssessment().getId());
		// we need to know if logged in user is assessor to determine if he can
		// participate in private conversation
		assessment.setAssessorId(assessmentManager.getAssessorIdForCompAssessment(assessment.getCompAssessmentId()));
	}

	public boolean isCurrentUserMessageSender(ActivityDiscussionMessageData messageData) {
		return idEncoder.encodeId(loggedUserBean.getUserId()).equals(messageData.getEncodedSenderId());
	}

	public boolean isCurrentUserAssessor(ActivityResultData result) {
		return loggedUserBean.getUserId() == result.getAssessment().getAssessorId();
	}

	public void editComment(String newContent, String activityMessageEncodedId) {
		long activityMessageId = idEncoder.decodeId(activityMessageEncodedId);
		try {
			assessmentManager.editCommentContent(activityMessageId, loggedUserBean.getUserId(), newContent);
			for (ActivityDiscussionMessageData messageData : currentResult.getAssessment()
					.getActivityDiscussionMessageData()) {
				if (messageData.getEncodedMessageId().equals(activityMessageEncodedId)) {
					messageData.setDateUpdated(new Date());
					messageData.setDateUpdatedFormat(DateUtil.createUpdateTime(messageData.getDateUpdated()));
					break;
				}
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error editing message with id : " + activityMessageId, e);
			PageUtil.fireErrorMessage("Error editing message");
		}
	}

	public void addCommentToActivityDiscussion() {
		long actualDiscussionId;
		if (StringUtils.isBlank(currentResult.getAssessment().getEncodedDiscussionId())) {
			LearningContextData lcd = new LearningContextData();
			lcd.setPage(PageUtil.getPostParameter("page"));
			lcd.setLearningContext(PageUtil.getPostParameter("learningContext"));
			lcd.setService(PageUtil.getPostParameter("service"));
			actualDiscussionId = createDiscussion(currentResult.getTargetActivityId(),
					currentResult.getAssessment().getCompAssessmentId(), lcd);

			// set discussionId in the appropriate ActivityAssessmentData
			String encodedDiscussionId = idEncoder.encodeId(actualDiscussionId);

			currentResult.getAssessment().setEncodedDiscussionId(encodedDiscussionId);
		} else {
			actualDiscussionId = idEncoder.decodeId(currentResult.getAssessment().getEncodedDiscussionId());
		}
		addComment(actualDiscussionId, currentResult.getAssessment().getCompAssessmentId());
		cleanupCommentData();
	}

	private long createDiscussion(long targetActivityId, long competenceAssessmentId, LearningContextData context) {
		try {
			// creating a set as there might be duplicates with ids
			Set<Long> participantIds = new HashSet<>();

			// adding the student as a participant
			participantIds.add(currentResult.getUser().getId());

			// adding the logged in user (the message poster) as a participant.
			// It can happen that some other user,
			// that is not the student or the assessor has started the thread
			// (i.e. any user with MANAGE priviledge)
			participantIds.add(loggedUserBean.getUserId());

			// if assessor is set, add him to the discussion
			if (currentResult.getAssessment().getAssessorId() > 0) {
				participantIds.add(currentResult.getAssessment().getAssessorId());
			}

			return assessmentManager.createActivityDiscussion(targetActivityId, competenceAssessmentId,
					new ArrayList<Long>(participantIds), loggedUserBean.getUserId(), true,
					currentResult.getAssessment().getGrade().getValue(), context).getId();
		} catch (ResourceCouldNotBeLoadedException | EventException e) {
			logger.error(e);
			return -1;
		}
	}

	private void addComment(long activityAssessmentId, long competenceAssessmentId) {
		try {
			ActivityDiscussionMessageData newComment = assessmentManager.addCommentToDiscussion(activityAssessmentId,
					loggedUserBean.getUserId(), newCommentValue);
			addNewCommentToAssessmentData(newComment, activityAssessmentId, competenceAssessmentId);

			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");

			notifyAssessmentCommentAsync(currentResult.getAssessment().getCredAssessmentId(), activityAssessmentId,
					idEncoder.decodeId(newComment.getEncodedMessageId()), page, lContext, service, decodedCredId);

		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error saving assessment message", e);
			PageUtil.fireErrorMessage("Error while adding new assessment message");
		}
	}

	private void notifyAssessmentCommentAsync(long credAssessmentId, long actAssessmentId, long assessmentCommentId,
			String page, String lContext, String service, long credentialId) {
		taskExecutor.execute(() -> {
			// User recipient = new User();
			// recipient.setId(recepientId);
			ActivityDiscussionMessage adm = new ActivityDiscussionMessage();
			adm.setId(assessmentCommentId);
			ActivityAssessment aa = new ActivityAssessment();
			aa.setId(actAssessmentId);
			Map<String, String> parameters = new HashMap<>();
			parameters.put("credentialId", credentialId + "");
			parameters.put("credentialAssessmentId", credAssessmentId + "");
			try {
				eventFactory.generateEvent(EventType.AssessmentComment, loggedUserBean.getUserId(), adm, aa, page,
						lContext, service, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});
	}

	private void addNewCommentToAssessmentData(ActivityDiscussionMessageData newComment, long actualDiscussionId,
			long competenceAssessmentId) {
		if (isCurrentUserAssessor(currentResult)) {
			newComment.setSenderInsructor(true);
		}
		currentResult.getAssessment().getActivityDiscussionMessageData().add(newComment);
		currentResult.getAssessment().setNumberOfMessages(currentResult.getAssessment().getNumberOfMessages() + 1);
	}

	private void cleanupCommentData() {
		newCommentValue = "";
	}

	/*
	 * GETTERS / SETTERS
	 */

	public String getActId() {
		return actId;
	}

	public void setActId(String actId) {
		this.actId = actId;
	}

	public long getDecodedActId() {
		return decodedActId;
	}

	public void setDecodedActId(long decodedActId) {
		this.decodedActId = decodedActId;
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

	public ActivityData getActivity() {
		return activity;
	}

	public void setActivity(ActivityData activity) {
		this.activity = activity;
	}

	public ActivityResultData getCurrentResult() {
		return currentResult;
	}

	public void setCurrentResult(ActivityResultData currentResult) {
		this.currentResult = currentResult;
	}

	public String getNewCommentValue() {
		return newCommentValue;
	}

	public void setNewCommentValue(String newCommentValue) {
		this.newCommentValue = newCommentValue;
	}
	
	public String getTargetActId() {
		return targetActId;
	}

	public void setTargetActId(String targetActId) {
		this.targetActId = targetActId;
	}

	public long getDecodedTargetActId() {
		return decodedTargetActId;
	}

	public void setDecodedTargetActId(long decodedTargetActId) {
		this.decodedTargetActId = decodedTargetActId;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

}
