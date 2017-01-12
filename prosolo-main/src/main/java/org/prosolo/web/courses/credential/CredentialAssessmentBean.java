package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.assessments.ActivityAssessmentData;
import org.prosolo.services.nodes.data.assessments.AssessmentData;
import org.prosolo.services.nodes.data.assessments.AssessmentDataFull;
import org.prosolo.services.nodes.data.assessments.CompetenceAssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialAssessmentBean")
@Component("credentialAssessmentBean")
@Scope("view")
public class CredentialAssessmentBean implements Serializable, Paginable {

	private static final long serialVersionUID = 7344090333263528353L;
	private static Logger logger = Logger.getLogger(CredentialAssessmentBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private CredentialManager credManager;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private EventFactory eventFactory;

	// PARAMETERS
	private String id;
	private long decodedId;

	// used when managing single assessment
	private String assessmentId;
	private long decodedAssessmentId;
	private AssessmentDataFull fullAssessmentData;
	private String reviewText;

	// used for managing multiple assessments
	private String credentialTitle;
	private String context;
	private List<AssessmentData> assessmentData;
	private List<AssessmentData> otherAssessments;
	private boolean searchForPending = true;
	private boolean searchForApproved = true;

	private PaginationData paginationData = new PaginationData(5);

	// adding new comment
	private String newCommentValue;
	
	private ActivityAssessmentData currentAssessment;

	public void init() {

		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedId;
			try {
				String title = credManager.getCredentialTitleForCredentialWithType(decodedId,
						LearningResourceType.UNIVERSITY_CREATED);
				if (title != null) {
					credentialTitle = title;
					paginationData.update(assessmentManager.countAssessmentsForAssessorAndCredential(
							decodedId, loggedUserBean.getUserId(), searchForPending, searchForApproved));
					assessmentData = assessmentManager.getAllAssessmentsForCredential(decodedId,
							loggedUserBean.getUserId(), searchForPending, searchForApproved, idEncoder,
							new SimpleDateFormat("MMMM dd, yyyy"));
				} else {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				}
			} catch (Exception e) {
				logger.error("Error while loading assessment data", e);
				PageUtil.fireErrorMessage("Error while loading assessment data");
			}
		}
	}

	public void initAssessment() {
		decodedAssessmentId = idEncoder.decodeId(assessmentId);
		
		if (decodedAssessmentId > 0) {
			try {
				fullAssessmentData = assessmentManager.getFullAssessmentData(decodedAssessmentId, idEncoder,
						loggedUserBean.getUserId(), new SimpleDateFormat("MMMM dd, yyyy"));
				credentialTitle = fullAssessmentData.getTitle();

				// for managers, load all other assessments
				if (isInManageSection()) {
					otherAssessments = assessmentManager.loadOtherAssessmentsForUserAndCredential(fullAssessmentData.getAssessedStrudentId(), fullAssessmentData.getCredentialId());
				}
			} catch (Exception e) {
				logger.error("Error while loading assessment data", e);
				PageUtil.fireErrorMessage("Error while loading assessment data");
			}
		}
	}
	
	public void approveCredential() {
		try {
			assessmentManager.approveCredential(idEncoder.decodeId(fullAssessmentData.getEncodedId()),
					fullAssessmentData.getTargetCredentialId(), reviewText);
			markCredentialApproved();

			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			notifyAssessmentApprovedAsync(decodedAssessmentId, page, lContext, service,
					fullAssessmentData.getAssessedStrudentId(), fullAssessmentData.getCredentialId());

			PageUtil.fireSuccessfulInfoMessage(
					"You have approved the credential for " + fullAssessmentData.getStudentFullName());
		} catch (Exception e) {
			logger.error("Error aproving assessment data", e);
			PageUtil.fireErrorMessage("Error while approving assessment data");
		}
	}

	private void markCredentialApproved() {
		fullAssessmentData.setApproved(true);
		for (CompetenceAssessmentData compAssessmentData : fullAssessmentData.getCompetenceAssessmentData()) {
			compAssessmentData.setApproved(true);
		}
	}

	public void approveCompetence(String encodedCompetenceAssessmentId) {
		try {
			assessmentManager.approveCompetence(idEncoder.decodeId(encodedCompetenceAssessmentId));
			markCompetenceApproved(encodedCompetenceAssessmentId);
			PageUtil.fireSuccessfulInfoMessage("assessCredentialFormGrowl",
					"You have sucessfully approved competence for " + fullAssessmentData.getStudentFullName());
		} catch (Exception e) {
			logger.error("Error aproving assessment data", e);
			PageUtil.fireErrorMessage("Error while approving assessment data");
		}
	}

	private void notifyAssessmentApprovedAsync(long decodedAssessmentId, String page, String lContext, String service,
			long assessedStudentId, long credentialId) {
		taskExecutor.execute(() -> {
			User assessor = new User();
			assessor.setId(assessedStudentId);
			CredentialAssessment assessment = new CredentialAssessment();
			assessment.setId(decodedAssessmentId);
			Map<String, String> parameters = new HashMap<>();
			parameters.put("credentialId", credentialId + "");
			try {
				eventFactory.generateEvent(EventType.AssessmentApproved, loggedUserBean.getUserId(), assessment, assessor,
						page, lContext, service, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});
	}

	private void markCompetenceApproved(String encodedCompetenceAssessmentId) {
		for (CompetenceAssessmentData competenceAssessment : fullAssessmentData.getCompetenceAssessmentData()) {
			if (competenceAssessment.getCompetenceAssessmentEncodedId().equals(encodedCompetenceAssessmentId)) {
				competenceAssessment.setApproved(true);
			}
		}

	}

	public void markDiscussionRead() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String encodedActivityDiscussionId = params.get("encodedActivityDiscussionId");

		if (!StringUtils.isBlank(encodedActivityDiscussionId)) {
			assessmentManager.markDiscussionAsSeen(loggedUserBean.getUserId(),
					idEncoder.decodeId(encodedActivityDiscussionId));
			Optional<ActivityAssessmentData> seenActivityAssessment = getActivityAssessmentByEncodedId(
					encodedActivityDiscussionId);
			seenActivityAssessment.ifPresent(data -> data.setAllRead(true));
		} else {
//			logger.error("User " + loggedUserBean.getUserId() + " tried to add comment without discussion id");
//			PageUtil.fireErrorMessage("Unable to add comment");
		}
	}

	private Optional<ActivityAssessmentData> getActivityAssessmentByEncodedId(String encodedActivityDiscussionId) {
		List<CompetenceAssessmentData> competenceAssessmentData = fullAssessmentData.getCompetenceAssessmentData();
		if (CollectionUtils.isNotEmpty(competenceAssessmentData)) {
			for (CompetenceAssessmentData comp : competenceAssessmentData) {
				for (ActivityAssessmentData act : comp.getActivityAssessmentData()) {
					if (encodedActivityDiscussionId.equals(act.getEncodedDiscussionId())) {
						return Optional.of(act);
					}
				}
			}
		}
		return Optional.empty();
	}
	
	private Optional<ActivityAssessmentData> getActivityAssessmentByActivityId(String encodedTargetActivityId) {
		List<CompetenceAssessmentData> competenceAssessmentData = fullAssessmentData.getCompetenceAssessmentData();
		if (CollectionUtils.isNotEmpty(competenceAssessmentData)) {
			for (CompetenceAssessmentData comp : competenceAssessmentData) {
				for (ActivityAssessmentData act : comp.getActivityAssessmentData()) {
					if (encodedTargetActivityId.equals(act.getEncodedTargetActivityId())) {
						return Optional.of(act);
					}
				}
			}
		}
		return Optional.empty();
	}

	public void addCommentToActivityDiscussion(String encodedActivityDiscussionId, String encodedTargetActivityId, String encodedCompetenceAssessmentId) {
		long actualDiscussionId;
		if (StringUtils.isBlank(encodedActivityDiscussionId)) {
			actualDiscussionId = createDiscussion(idEncoder.decodeId(encodedTargetActivityId), idEncoder.decodeId(encodedCompetenceAssessmentId));
			
			// set discussionId in the appropriate ActivityAssessmentData
			String encodedDiscussionId = idEncoder.encodeId(actualDiscussionId);
			
			Optional<ActivityAssessmentData> actAssessmentData = getActivityAssessmentByActivityId(encodedTargetActivityId);
			actAssessmentData.ifPresent(data -> data.setEncodedDiscussionId(encodedDiscussionId));
		} else {
			actualDiscussionId = idEncoder.decodeId(encodedActivityDiscussionId);
		}
		addComment(actualDiscussionId, idEncoder.decodeId(encodedCompetenceAssessmentId));
		cleanupCommentData();
	}

	public void updateGrade() {
		try {
			long compAssessmentId = currentAssessment.getCompAssessmentId();
			long credAssessmentId = currentAssessment.getCredAssessmentId();
			
			if (StringUtils.isBlank(currentAssessment.getEncodedDiscussionId())) {
				long actualDiscussionId = createDiscussion(idEncoder.decodeId(currentAssessment.getEncodedTargetActivityId()), compAssessmentId);
				
				// set discussionId in the appropriate ActivityAssessmentData
				String encodedDiscussionId = idEncoder.encodeId(actualDiscussionId);
				
				currentAssessment.setEncodedDiscussionId(encodedDiscussionId);
			} else {
				assessmentManager.updateGradeForActivityAssessment(
						idEncoder.decodeId(currentAssessment.getEncodedDiscussionId()),
						currentAssessment.getGrade().getValue());
			}
			
			// recalculate points of parent competence and credential assessments
			int compPoints = assessmentManager.recalculateScoreForCompetenceAssessment(compAssessmentId);
			
			CompetenceAssessmentData compAssessmentData = fullAssessmentData.findCompetenceAssessmentData(compAssessmentId);
			if (compAssessmentData != null) {
				compAssessmentData.setPoints(compPoints);
			} else {
				logger.error("Could not fin competence assessment data for id: " + compAssessmentId);
			}
			
			int credPoints = assessmentManager.recalculateScoreForCredentialAssessment(credAssessmentId);
			fullAssessmentData.setPoints(credPoints);
			
			currentAssessment.getGrade().setAssessed(true);
			PageUtil.fireSuccessfulInfoMessage("Grade updated");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error while updating grade");
		}
	}
	
	public boolean isCurrentUserMessageSender(ActivityDiscussionMessageData messageData) {
		return idEncoder.encodeId(loggedUserBean.getUserId()).equals(messageData.getEncodedSenderId());
	}

	public void editComment(String newContent, String activityMessageEncodedId) {
		long activityMessageId = idEncoder.decodeId(activityMessageEncodedId);
		try {
			assessmentManager.editCommentContent(activityMessageId, loggedUserBean.getUserId(), newContent);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error editing message with id : " + activityMessageId, e);
			PageUtil.fireErrorMessage("Error editing message");
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
			
			List<Long> participantIds = assessmentManager.getParticipantIds(activityAssessmentId);
			for (Long userId : participantIds) {
				if (userId != loggedUserBean.getUserId()) {
					notifyAssessmentCommentAsync(decodedAssessmentId, page, lContext, service, userId, fullAssessmentData.getCredentialId());
				}
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error saving assessment message", e);
			PageUtil.fireErrorMessage("Error while adding new assessment message");
		}
	}

	private void addNewCommentToAssessmentData(ActivityDiscussionMessageData newComment, long actualDiscussionId,
			long competenceAssessmentId) {
		if (isCurrentUserAssessor()) {
			newComment.setSenderInsructor(true);
		}
		Optional<ActivityAssessmentData> newCommentActivity = getActivityAssessmentByEncodedId(
				idEncoder.encodeId(actualDiscussionId));
		newCommentActivity.ifPresent(data -> data.getActivityDiscussionMessageData().add(newComment));

	}

	private void notifyAssessmentCommentAsync(long decodedAssessmentId, String page, String lContext, String service,
			long recepientId, long credentialId) {
		taskExecutor.execute(() -> {
			User recipient = new User();
			recipient.setId(recepientId);
			CredentialAssessment assessment = new CredentialAssessment();
			assessment.setId(decodedAssessmentId);
			Map<String, String> parameters = new HashMap<>();
			parameters.put("credentialId", credentialId + "");
			// in order to construct a link, we will need info if the
			// notification recipient is assessor (to prepend "manage")
			parameters.put("isRecepientAssessor",
					((Boolean) (fullAssessmentData.getAssessorId() == recepientId)).toString());
			try {
				eventFactory.generateEvent(EventType.AssessmentComment, loggedUserBean.getUserId(), assessment, recipient,
						page, lContext, service, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});
	}

	public boolean isCurrentUserAssessor() {
		if (fullAssessmentData == null) {
			return false;
		} else
			return loggedUserBean.getUserId() == fullAssessmentData.getAssessorId();
	}

	private long createDiscussion(long targetActivityId, long competenceAssessmentId) {
		try {
			Integer grade = currentAssessment != null ? currentAssessment.getGrade().getValue() : null;
			
			// creating a set as there might be duplicates with ids
			Set<Long> participantIds = new HashSet<>();
			
			// adding the student as a participant
			participantIds.add(fullAssessmentData.getAssessedStrudentId());
			
			// adding the logged in user (the message poster) as a participant. It can happen that some other user, 
			// that is not the student or the assessor has started the thread (i.e. any user with MANAGE priviledge)
			participantIds.add(loggedUserBean.getUserId());
			
			// if assessor is set, add him to the discussion
			if (fullAssessmentData.getAssessorId() > 0) {
				participantIds.add(fullAssessmentData.getAssessorId());
			}
			
			return assessmentManager.createActivityDiscussion(targetActivityId, competenceAssessmentId,
					new ArrayList<Long>(participantIds),
					loggedUserBean.getUserId(), fullAssessmentData.isDefaultAssessment(), grade).getId();
		} catch (ResourceCouldNotBeLoadedException e) {
			return -1;
		}
	}

	private void cleanupCommentData() {
		newCommentValue = "";

	}

	public void searchForAll() {
		// only reinitialize when at least one is already false
		if (!searchForApproved || !searchForPending) {
			searchForApproved = true;
			searchForPending = true;
			paginationData.setPage(1);
			init();
		}
	}

	public void searchForNone() {
		// only reinitialize when at least one is true
		if (searchForApproved || searchForPending) {
			searchForApproved = false;
			searchForPending = false;
			paginationData.setPage(1);
			init();
		}
	}
	
	private boolean isInManageSection() {
		String currentUrl = PageUtil.getRewriteURL();
		return currentUrl.contains("/manage/");
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}

	public void setIdEncoder(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public CredentialManager getCredManager() {
		return credManager;
	}

	public void setCredManager(CredentialManager credManager) {
		this.credManager = credManager;
	}

	public AssessmentManager getAssessmentManager() {
		return assessmentManager;
	}

	public void setAssessmentManager(AssessmentManager assessmentManager) {
		this.assessmentManager = assessmentManager;
	}

	public LoggedUserBean getLoggedUserBean() {
		return loggedUserBean;
	}

	public void setLoggedUserBean(LoggedUserBean loggedUserBean) {
		this.loggedUserBean = loggedUserBean;
	}

	public List<AssessmentData> getAssessmentData() {
		return assessmentData;
	}

	public void setAssessmentData(List<AssessmentData> assessmentData) {
		this.assessmentData = assessmentData;
	}
	
	public List<AssessmentData> getOtherAssessments() {
		return otherAssessments;
	}

	public void setOtherAssessments(List<AssessmentData> otherAssessments) {
		this.otherAssessments = otherAssessments;
	}

	public boolean isSearchForPending() {
		return searchForPending;
	}

	public void setSearchForPending(boolean searchForPending) {
		this.searchForPending = searchForPending;
		paginationData.setPage(1);
		init();
		RequestContext.getCurrentInstance().update("assessmentList:filterAssessmentsForm");
	}

	public boolean isSearchForApproved() {
		return searchForApproved;
	}

	public void setSearchForApproved(boolean searchForApproved) {
		this.searchForApproved = searchForApproved;
		paginationData.setPage(1);
		init();
		RequestContext.getCurrentInstance().update("assessmentList:filterAssessmentsForm");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAssessmentId() {
		return assessmentId;
	}

	public void setAssessmentId(String assessmentId) {
		this.assessmentId = assessmentId;
	}

	public AssessmentDataFull getFullAssessmentData() {
		return fullAssessmentData;
	}

	public void setFullAssessmentData(AssessmentDataFull fullAssessmentData) {
		this.fullAssessmentData = fullAssessmentData;
	}

	public String getReviewText() {
		return reviewText;
	}

	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}

	public String getNewCommentValue() {
		return newCommentValue;
	}

	public void setNewCommentValue(String newCommentValue) {
		this.newCommentValue = newCommentValue;
	}

	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public void setEventFactory(EventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

	@Override
	public void changePage(int page) {
		if (paginationData.getPage() != page) {
			paginationData.setPage(page);
			init();
		}
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public ActivityAssessmentData getCurrentAssessment() {
		return currentAssessment;
	}

	public void setCurrentAssessment(ActivityAssessmentData currentAssessment) {
		this.currentAssessment = currentAssessment;
	}
	
	
}
