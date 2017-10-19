package org.prosolo.web.courses.credential;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.assessments.*;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

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
	@Inject private RubricManager rubricManager;

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

	private ActivityAssessmentData currentActivityAssessment;

	public void init() {

		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedId;
			try {
				String title = credManager.getCredentialTitle(decodedId);
				if (title != null) {
					credentialTitle = title;
					paginationData.update(assessmentManager.countAssessmentsForAssessorAndCredential(
							decodedId, loggedUserBean.getUserId(), searchForPending, searchForApproved));
					assessmentData = assessmentManager.getAllAssessmentsForCredential(decodedId,
							loggedUserBean.getUserId(), searchForPending, searchForApproved, idEncoder,
							new SimpleDateFormat("MMMM dd, yyyy"));
				} else {
					PageUtil.notFound();
				}
			} catch (Exception e) {
				logger.error("Error while loading assessment data", e);
				PageUtil.fireErrorMessage("Error while loading assessment data");
			}
		}
	}

	public void initAssessment() {
		decodedId = idEncoder.decodeId(id);

		decodedAssessmentId = idEncoder.decodeId(assessmentId);
		
		if (decodedId > 0 && decodedAssessmentId > 0) {
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

	private void initRubricIfNotInitialized() {
		try {
			if (currentActivityAssessment.getGrade().getGradingMode() == GradingMode.MANUAL_RUBRIC && !currentActivityAssessment.getGrade().isRubricInitialized()) {
				currentActivityAssessment.getGrade().setRubricCategories(rubricManager.getRubricDataForActivity(
						currentActivityAssessment.getActivityId(),
						idEncoder.decodeId(currentActivityAssessment.getEncodedDiscussionId()),
						true));
				currentActivityAssessment.getGrade().setRubricInitialized(true);
			}
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data. Please refresh the page and try again.");
		}
	}

	public boolean allCompetencesStarted() {
		for (CompetenceAssessmentData cad : fullAssessmentData.getCompetenceAssessmentData()) {
			if (cad.isReadOnly()) {
				return false;
			}
		}
		return true;
	}
	
	public void approveCredential() {
		try {
			assessmentManager.approveCredential(idEncoder.decodeId(fullAssessmentData.getEncodedId()),
					fullAssessmentData.getTargetCredentialId(), reviewText);
			markCredentialApproved();

			notifyAssessmentApprovedAsync(decodedAssessmentId, fullAssessmentData.getAssessedStrudentId(),
					fullAssessmentData.getCredentialId());

			PageUtil.fireSuccessfulInfoMessage(
					"You have approved the credential for " + fullAssessmentData.getStudentFullName());
		} catch (Exception e) {
			logger.error("Error approving assessment data", e);
			PageUtil.fireErrorMessage("Error approving the assessment");
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
					"You have successfully approved the competence for " + fullAssessmentData.getStudentFullName());
		} catch (Exception e) {
			logger.error("Error approving the assessment", e);
			PageUtil.fireErrorMessage("Error approving the assessment");
		}
	}

	private void notifyAssessmentApprovedAsync(long decodedAssessmentId, long assessedStudentId, long credentialId) {
		UserContextData context = loggedUserBean.getUserContext();
		taskExecutor.execute(() -> {
			User student = new User();
			student.setId(assessedStudentId);
			CredentialAssessment assessment = new CredentialAssessment();
			assessment.setId(decodedAssessmentId);
			Map<String, String> parameters = new HashMap<>();
			parameters.put("credentialId", credentialId + "");
			try {
				eventFactory.generateEvent(EventType.AssessmentApproved, context, assessment, student, null, parameters);
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
	
//	private Optional<ActivityAssessmentData> getActivityAssessmentByActivityId(String encodedTargetActivityId) {
//		List<CompetenceAssessmentData> competenceAssessmentData = fullAssessmentData.getCompetenceAssessmentData();
//		if (CollectionUtils.isNotEmpty(competenceAssessmentData)) {
//			for (CompetenceAssessmentData comp : competenceAssessmentData) {
//				for (ActivityAssessmentData act : comp.getActivityAssessmentData()) {
//					if (encodedTargetActivityId.equals(act.getEncodedTargetActivityId())) {
//						return Optional.of(act);
//					}
//				}
//			}
//		}
//		return Optional.empty();
//	}

//	public void addCommentToActivityDiscussion() {
//		try {
//			if (StringUtils.isBlank(currentActivityAssessment.getEncodedDiscussionId())) {
//				PageContextData lcd = new PageContextData();
//				lcd.setPage(PageUtil.getPostParameter("page"));
//				lcd.setLearningContext(PageUtil.getPostParameter("learningContext"));
//				lcd.setService(PageUtil.getPostParameter("service"));
//				createAssessment(idEncoder.decodeId(currentActivityAssessment.getEncodedTargetActivityId()),
//						currentCompetenceAssessment.getCompetenceAssessmentId(),
//						currentCompetenceAssessment.getTargetCompetenceId(), false, lcd);
//			}
//			addComment();
//			cleanupCommentData();
//		} catch (EventException e) {
//			logger.error(e);
//		} catch (Exception e) {
//			logger.error(e);
//			PageUtil.fireErrorMessage("Error while saving a comment. Please try again.");
//		}
//	}

	public void updateGrade() {
		updateGrade(true);
	}

	public void updateGrade(boolean retry) {
		try {
			if (StringUtils.isBlank(currentActivityAssessment.getEncodedDiscussionId())) {
				createAssessment(currentActivityAssessment.getTargetActivityId(),
						currentActivityAssessment.getCompAssessmentId(),
						currentActivityAssessment.getTargetCompId(), true);
			} else {
				int newGrade = assessmentManager.updateGradeForActivityAssessment(
						fullAssessmentData.getCredAssessmentId(),
						currentActivityAssessment.getCompAssessmentId(),
						idEncoder.decodeId(currentActivityAssessment.getEncodedDiscussionId()),
						currentActivityAssessment.getGrade(), loggedUserBean.getUserContext());
				if (newGrade >= 0) {
					currentActivityAssessment.getGrade().setValue(newGrade);
				}
			}

			fullAssessmentData.setPoints(assessmentManager.getCredentialAssessmentScore(
					fullAssessmentData.getCredAssessmentId()));
			if (currentActivityAssessment.getCompAssessment() != null) {
				currentActivityAssessment.getCompAssessment().setPoints(
						assessmentManager.getCompetenceAssessmentScore(
								currentActivityAssessment.getCompAssessmentId()));
			}
			currentActivityAssessment.getGrade().setAssessed(true);

			PageUtil.fireSuccessfulInfoMessage("The grade has been updated");
		} catch (IllegalDataStateException e) {
			if (retry) {
				//if this exception is thrown, data is repopulated and we should retry updating grade
				updateGrade(false);
			} else {
				logger.error("Error after retry: " + e);
				PageUtil.fireErrorMessage("Error updating the grade. Please refresh the page and try again.");
			}
		} catch (EventException e) {
			logger.error(e);
		} catch (DbConnectionException e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error while updating grade");
		}
	}
	
//	public boolean isCurrentUserMessageSender(ActivityDiscussionMessageData messageData) {
//		return idEncoder.encodeId(loggedUserBean.getUserId()).equals(messageData.getEncodedSenderId());
//	}
//
//	public void editComment(String newContent, String activityMessageEncodedId) {
//		long activityMessageId = idEncoder.decodeId(activityMessageEncodedId);
//		try {
//			assessmentManager.editCommentContent(activityMessageId, loggedUserBean.getUserId(), newContent);
//		} catch (ResourceCouldNotBeLoadedException e) {
//			logger.error("Error editing message with id : " + activityMessageId, e);
//			PageUtil.fireErrorMessage("Error editing message");
//		}
//	}

//	private void addComment() {
//		try {
//			long activityAssessmentId = idEncoder.decodeId(currentActivityAssessment.getEncodedDiscussionId());
//			ActivityDiscussionMessageData newComment = assessmentManager.addCommentToDiscussion(
//					activityAssessmentId, loggedUserBean.getUserId(), newCommentValue);
//
//			addNewCommentToAssessmentData(newComment);
//
//			String page = PageUtil.getPostParameter("page");
//			String lContext = PageUtil.getPostParameter("learningContext");
//			String service = PageUtil.getPostParameter("service");
//
//			notifyAssessmentCommentAsync(decodedAssessmentId,
//					activityAssessmentId, idEncoder.decodeId(newComment.getEncodedMessageId()),
//					page, lContext, service, fullAssessmentData.getCredentialId());
//		} catch (ResourceCouldNotBeLoadedException e) {
//			logger.error("Error saving assessment message", e);
//			PageUtil.fireErrorMessage("Error while adding new assessment message");
//		}
//	}

//	private void addNewCommentToAssessmentData(ActivityDiscussionMessageData newComment) {
//		if (isCurrentUserAssessor()) {
//			newComment.setSenderInsructor(true);
//		}
//		currentActivityAssessment.getActivityDiscussionMessageData().add(newComment);
//
//	}

//	private void notifyAssessmentCommentAsync(long credAssessmentId, long actAssessmentId,
//			long assessmentCommentId, String page, String lContext, String service, long credentialId) {
//		taskExecutor.execute(() -> {
//			ActivityDiscussionMessage adm = new ActivityDiscussionMessage();
//			adm.setId(assessmentCommentId);
//			ActivityAssessment aa = new ActivityAssessment();
//			aa.setId(actAssessmentId);
//			Map<String, String> parameters = new HashMap<>();
//			parameters.put("credentialId", credentialId + "");
//			parameters.put("credentialAssessmentId", credAssessmentId + "");
//			try {
//				eventFactory.generateEvent(EventType.AssessmentComment, loggedUserBean.getUserId(),
//						adm, aa, page, lContext, service, parameters);
//			} catch (Exception e) {
//				logger.error("Eror sending notification for assessment request", e);
//			}
//		});
//	}

	public boolean isCurrentUserAssessor() {
		if (fullAssessmentData == null) {
			return false;
		} else
			return loggedUserBean.getUserId() == fullAssessmentData.getAssessorId();
	}

//	private long createDiscussion(long targetActivityId, long competenceAssessmentId,
//			PageContextData context) {
//		try {
//			Integer grade = currentAssessment != null ? currentAssessment.getGrade().getValue() : null;
//
//			// creating a set as there might be duplicates with ids
//			Set<Long> participantIds = new HashSet<>();
//
//			// adding the student as a participant
//			participantIds.add(fullAssessmentData.getAssessedStrudentId());
//
//			// adding the logged in user (the message poster) as a participant. It can happen that some other user,
//			// that is not the student or the assessor has started the thread (i.e. any user with MANAGE priviledge)
//			participantIds.add(loggedUserBean.getUserId());
//
//			// if assessor is set, add him to the discussion
//			if (fullAssessmentData.getAssessorId() > 0) {
//				participantIds.add(fullAssessmentData.getAssessorId());
//			}
//
//			return assessmentManager.createActivityDiscussion(targetActivityId, competenceAssessmentId,
//					new ArrayList<Long>(participantIds), loggedUserBean.getUserId(),
//					fullAssessmentData.isDefaultAssessment(), grade, context).getId();
//		} catch (ResourceCouldNotBeLoadedException | EventException e) {
//			logger.error(e);
//			return -1;
//		}
//	}

	private void createAssessment(long targetActivityId, long competenceAssessmentId, long targetCompetenceId,
								  boolean updateGrade)
			throws DbConnectionException, IllegalDataStateException, EventException {
		GradeData grade = updateGrade
				? currentActivityAssessment != null ? currentActivityAssessment.getGrade() : null
				: null;

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

		try {
			if (competenceAssessmentId > 0) {
				//if competence assessment exists create activity assessment only
				ActivityAssessment aa =
						assessmentManager.createActivityDiscussion(targetActivityId, competenceAssessmentId,
								fullAssessmentData.getCredAssessmentId(), new ArrayList<Long>(participantIds),
								loggedUserBean.getUserId(), fullAssessmentData.isDefaultAssessment(), grade, true,
								loggedUserBean.getUserContext());
				currentActivityAssessment.setEncodedDiscussionId(idEncoder.encodeId(aa.getId()));
				currentActivityAssessment.getGrade().setValue(aa.getPoints());
			} else {
				//if competence assessment does not exist create competence assessment and activity assessment
				AssessmentBasicData assessmentInfo = assessmentManager.createCompetenceAndActivityAssessment(
						fullAssessmentData.getCredAssessmentId(), targetCompetenceId, targetActivityId,
						new ArrayList<Long>(participantIds), loggedUserBean.getUserId(), grade,
						fullAssessmentData.isDefaultAssessment(), loggedUserBean.getUserContext());
				populateCompetenceAndActivityAssessmentIds(assessmentInfo);
			}
		} catch (IllegalDataStateException e) {
				/*
					this means that assessment is created in the meantime - this should be handled better because this
					exception does not have to mean that this is the case. Return to this when exceptions are rethinked.
				 */
				/*
					if competence assessment is already set, get activity assessment id and set it, otherwise get both
					competence assessment and activity assessment ids.
				 */
			if (competenceAssessmentId > 0) {
				currentActivityAssessment.setEncodedDiscussionId(idEncoder.encodeId(
						assessmentManager.getActivityAssessmentId(competenceAssessmentId, targetActivityId)));
			} else {
				AssessmentBasicData assessmentInfo = assessmentManager.getCompetenceAndActivityAssessmentIds(
						targetCompetenceId, targetActivityId, fullAssessmentData.getCredAssessmentId());
				populateCompetenceAndActivityAssessmentIds(assessmentInfo);
			}
			logger.error(e);
			//rethrow exception so caller of this method can react in appropriate way
			throw e;
		}
	}

	private void populateCompetenceAndActivityAssessmentIds(AssessmentBasicData assessmentInfo) {
		currentActivityAssessment.setEncodedDiscussionId(idEncoder.encodeId(
				assessmentInfo.getActivityAssessmentId()));
		currentActivityAssessment.getGrade().setValue(assessmentInfo.getGrade());
		currentActivityAssessment.setCompAssessmentId(assessmentInfo.getCompetenceAssessmentId());
		//if competence assessment data is set, set id there too
		if (currentActivityAssessment.getCompAssessment() != null) {
			currentActivityAssessment.getCompAssessment().setCompetenceAssessmentId(
					assessmentInfo.getCompetenceAssessmentId());
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

	public void setCurrentAssessment(ActivityAssessmentData actAssessment) {
		this.currentActivityAssessment = actAssessment;
		initRubricIfNotInitialized();
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

	public ActivityAssessmentData getCurrentActivityAssessment() {
		return currentActivityAssessment;
	}

	public void setCurrentActivityAssessment(ActivityAssessmentData currentActivityAssessment) {
		this.currentActivityAssessment = currentActivityAssessment;
	}
}
