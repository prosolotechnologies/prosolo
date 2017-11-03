package org.prosolo.web.courses.credential;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.credential.ActivityResultType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.assessments.*;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.activity.ActivityResultBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

@ManagedBean(name = "credentialActivityAssessmentsBeanManager")
@Component("credentialActivityAssessmentsBeanManager")
@Scope("view")
public class CredentialActivityAssessmentsBeanManager implements Serializable, Paginable {

	private static final long serialVersionUID = 6214124501846073070L;

	private static Logger logger = Logger.getLogger(CredentialActivityAssessmentsBeanManager.class);
	
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialManager credManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private AssessmentManager assessmentManager;
	@Inject private ActivityResultBean actResultBean;
	@Inject private RubricManager rubricManager;

	private String actId;
	private long decodedActId;
	private String credId;
	private long decodedCredId;
	private String targetActId;
	private long decodedTargetActId;
	private String commentId;
	
	private ActivityAssessmentsSummaryData assessmentsSummary;
	private String credentialTitle;

	private static final boolean paginate = false;
	private PaginationData paginationData = new PaginationData();
	
	private ActivityResultData currentResult;
	
	private ResourceAccessData access;

	public void init() {
		decodedActId = idEncoder.decodeId(actId);
		decodedCredId = idEncoder.decodeId(credId);
		if (decodedActId > 0 && decodedCredId > 0) {
			try {
				/*
				 * check if user has instructor privilege for this resource and if has, we should mark his comments as
				 * instructor comments
				 */
				access = compManager.getResourceAccessData(decodedCredId, loggedUserBean.getUserId(),
						ResourceAccessRequirements.of(AccessMode.MANAGER)
								.addPrivilege(UserGroupPrivilege.Edit)
								.addPrivilege(UserGroupPrivilege.Instruct));

				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					assessmentsSummary = activityManager
							.getActivityAssessmentsDataForDefaultCredentialAssessment(
									decodedCredId, decodedActId, access.isCanInstruct(), paginate,
									paginationData.getPage() - 1, paginationData.getLimit());

					if (paginate) {
						this.paginationData.update((int) assessmentsSummary.getNumberOfStudentsCompletedActivity());
					}
					if (assessmentsSummary == null) {
						PageUtil.notFound();
					} else {
						loadCredentialTitle();
					}
				}
			} catch (ResourceNotFoundException rnfe) {
				logger.error(rnfe);
				PageUtil.notFound();
			} catch(Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}

	public boolean canUserEditDelivery() {
		return access.isCanEdit();
	}

	public int getMinGrade() {
		if (!assessmentsSummary.getStudentResults().isEmpty()) {
			ActivityResultData res = assessmentsSummary.getStudentResults().get(0);
			return res.getAssessment().getGrade().getMinGrade();
		}
		return 0;
	}

	public int getMaxGrade() {
		if (!assessmentsSummary.getStudentResults().isEmpty()) {
			ActivityResultData res = assessmentsSummary.getStudentResults().get(0);
			return res.getAssessment().getGrade().getMaxGrade();
		}
		return 0;
	}

	private void initRubricIfNotInitialized() {
		try {
			if (currentResult.getAssessment().getGrade().getGradingMode() == GradingMode.MANUAL_RUBRIC && !currentResult.getAssessment().getGrade().isRubricInitialized()) {
				currentResult.getAssessment().getGrade().setRubricCriteria(rubricManager.getRubricDataForAssessment(
						idEncoder.decodeId(currentResult.getAssessment().getEncodedDiscussionId()),
						currentResult.getAssessment().getActivityId()));
				currentResult.getAssessment().getGrade().setRubricInitialized(true);
			}
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data. Please refresh the page and try again.");
		}
	}
	
	public void initIndividualResponse() {
		decodedActId = idEncoder.decodeId(actId);
		decodedCredId = idEncoder.decodeId(credId);
		decodedTargetActId = idEncoder.decodeId(targetActId);
		if (decodedCredId > 0 && decodedActId > 0 && decodedTargetActId > 0) {
			try {
				/*
				 * check if user has instructor privilege for this resource and if has, we should mark his comments as
				 * instructor comments
				 */
				access = compManager.getResourceAccessData(decodedCredId, loggedUserBean.getUserId(),
						ResourceAccessRequirements.of(AccessMode.MANAGER)
								.addPrivilege(UserGroupPrivilege.Edit)
								.addPrivilege(UserGroupPrivilege.Instruct));

				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					assessmentsSummary = activityManager
							.getActivityAssessmentDataForDefaultCredentialAssessment(
									decodedCredId, decodedActId, decodedTargetActId, access.isCanInstruct());
					if (assessmentsSummary.getStudentResults() != null && !assessmentsSummary.getStudentResults().isEmpty()) {
						currentResult = assessmentsSummary.getStudentResults().get(0);
						//loadAdditionalData(currentResult);
						if (assessmentsSummary.getResultType() != ActivityResultType.NONE && commentId != null) {
							currentResult.getResultComments().setCommentId(idEncoder.decodeId(commentId));
							initializeResultCommentsIfNotInitialized(currentResult);
						}

						if (assessmentsSummary == null || currentResult == null) {
							PageUtil.notFound();
						} else {
							loadCredentialTitle();
						}
					} else {
						PageUtil.notFound();
					}
				}
			} catch (ResourceNotFoundException rnfe) {
				logger.error(rnfe);
				PageUtil.notFound();
			} catch (Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error while loading assessmentsSummary results");
			}
		} else {
			PageUtil.notFound();
		}
	}

	public ActivityResultData getIndividualActivityResponse() {
		return assessmentsSummary.getStudentResults().get(0);
	}

	public long getTargetActivityId() {
		return decodedTargetActId;
	}
	
	public String getResultOwnerFullName() {
		return currentResult.getUser().getFullName();
	}
	
	private int countStudentResults() {
		return activityManager.countStudentsLearningCredentialThatCompletedActivity(decodedCredId, decodedActId).intValue();
	}

	private void searchStudentResults(boolean calculateNumberOfResults) {
		try {
			if (paginate) {
				this.paginationData.update(activityManager.countStudentsLearningCredentialThatCompletedActivity(decodedCredId, decodedActId).intValue());
			}
			List<ActivityResultData> results = activityManager
					.getStudentsActivityAssessmentsData(decodedCredId, decodedActId, 0, access.isCanInstruct(),
							paginate, paginationData.getPage() - 1, paginationData.getLimit());
			assessmentsSummary.setStudentResults(results);
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading assessmentsSummary results");
		}
	}
	
	private void loadCredentialTitle() {
		decodedCredId = idEncoder.decodeId(credId);
		credentialTitle = credManager.getCredentialTitle(decodedCredId);
	}
	
	public void initializeResultCommentsIfNotInitialized(ActivityResultData result) {
		try {
			actResultBean.initializeResultCommentsIfNotInitialized(result);
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	//assessment begin
	public void loadActivityDiscussion(ActivityResultData result) {
		try {
			ActivityAssessmentData assessment = result.getAssessment();
			if (!assessment.isMessagesInitialized()) {
				if (assessment.getEncodedDiscussionId() != null && !assessment.getEncodedDiscussionId().isEmpty()) {
					assessment.populateDiscussionMessages(assessmentManager
							.getActivityDiscussionMessages(idEncoder.decodeId(assessment.getEncodedDiscussionId()),
									assessment.getAssessorId()));
				}
				assessment.setMessagesInitialized(true);
			}
			this.currentResult = result;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage("Error while trying to initialize private conversation messages");
		}
	}
	
	//assessment begin
	public void loadActivityDiscussionById(long targetActivityId, boolean loadDiscussion, boolean loadComments) {
		ActivityResultData result = activityManager.getActivityResultData(
				targetActivityId, 
				loadComments, 
				access.isCanInstruct(), 
				true, 
				loggedUserBean.getUserId());
		
//		if (result != null && loadDiscussion) {
			loadActivityDiscussion(result);
//		}
	}
	
	public boolean isCurrentUserMessageSender(ActivityDiscussionMessageData messageData) {
		return idEncoder.encodeId(loggedUserBean.getUserId()).equals(messageData.getEncodedSenderId());
	}
	
	public boolean isCurrentUserAssessor(ActivityResultData result) {
		return loggedUserBean.getUserId() == result.getAssessment().getAssessorId();
	}

	public void updateGrade() {
		updateGrade(true);
	}

	public void updateGrade(boolean retry) {
		try {
			if (StringUtils.isBlank(currentResult.getAssessment().getEncodedDiscussionId())) {
				createAssessment(currentResult.getTargetActivityId(),
						currentResult.getAssessment().getCompAssessmentId(),
						currentResult.getAssessment().getTargetCompId(), true);
			} else {
				int newGrade = assessmentManager.updateGradeForActivityAssessment(
						currentResult.getAssessment().getCredAssessmentId(),
						currentResult.getAssessment().getCompAssessmentId(),
						idEncoder.decodeId(currentResult.getAssessment().getEncodedDiscussionId()),
						currentResult.getAssessment().getGrade(), loggedUserBean.getUserContext());
				if (newGrade >= 0) {
					currentResult.getAssessment().getGrade().setValue(newGrade);
				}
			}

			currentResult.getAssessment().getGrade().setAssessed(true);

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
			logger.info(e);
		} catch (DbConnectionException e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating the grade");
		}
	}

	private void createAssessment(long targetActivityId, long competenceAssessmentId, long targetCompetenceId,
								  boolean updateGrade)
			throws DbConnectionException, IllegalDataStateException, EventException {
		GradeData grade = updateGrade
				? currentResult != null ? currentResult.getAssessment().getGrade() : null
				: null;

		// creating a set as there might be duplicates with ids
		Set<Long> participantIds = new HashSet<>();

		// adding the student as a participant
		participantIds.add(currentResult.getUser().getId());

		// adding the logged in user (the message poster) as a participant. It can happen that some other user,
		// that is not the student or the assessor has started the thread (i.e. any user with MANAGE priviledge)
		participantIds.add(loggedUserBean.getUserId());

		// if assessor is set, add him to the discussion
		if (currentResult.getAssessment().getAssessorId() > 0) {
			participantIds.add(currentResult.getAssessment().getAssessorId());
		}

		try {
			if (competenceAssessmentId > 0) {
				//if competence assessment exists create assessmentsSummary assessment only
				ActivityAssessment aa =
						assessmentManager.createActivityDiscussion(targetActivityId, competenceAssessmentId,
								currentResult.getAssessment().getCredAssessmentId(), new ArrayList<Long>(participantIds),
								loggedUserBean.getUserId(), true, grade, true,
								loggedUserBean.getUserContext());
				currentResult.getAssessment().setEncodedDiscussionId(idEncoder.encodeId(aa.getId()));
				currentResult.getAssessment().getGrade().setValue(aa.getPoints());
			} else {
				//if competence assessment does not exist create competence assessment and assessmentsSummary assessment
				AssessmentBasicData assessmentInfo = assessmentManager.createCompetenceAndActivityAssessment(
						currentResult.getAssessment().getCredAssessmentId(), targetCompetenceId, targetActivityId,
						new ArrayList<>(participantIds), loggedUserBean.getUserId(), grade,
						true, loggedUserBean.getUserContext());
				currentResult.getAssessment().setEncodedDiscussionId(idEncoder.encodeId(assessmentInfo.getActivityAssessmentId()));
				currentResult.getAssessment().setCompAssessmentId(assessmentInfo.getCompetenceAssessmentId());
				currentResult.getAssessment().getGrade().setValue(assessmentInfo.getGrade());
			}
		} catch (IllegalDataStateException e) {
			/*
				this means that assessment is created in the meantime - this should be handled better because this
				exception does not have to mean that this is the case. Return to this when exceptions are rethinked.
			 */
			/*
				if competence assessment is already set, get assessmentsSummary assessment id and set it, otherwise get both
				competence assessment and assessmentsSummary assessment ids.
			 */
			if (competenceAssessmentId > 0) {
				currentResult.getAssessment().setEncodedDiscussionId(idEncoder.encodeId(
						assessmentManager.getActivityAssessmentId(competenceAssessmentId, targetActivityId)));
			} else {
				AssessmentBasicData assessmentInfo = assessmentManager.getCompetenceAndActivityAssessmentIds(
						targetCompetenceId, targetActivityId, currentResult.getAssessment().getCredAssessmentId());
				currentResult.getAssessment().setEncodedDiscussionId(idEncoder.encodeId(assessmentInfo.getActivityAssessmentId()));
				currentResult.getAssessment().setCompAssessmentId(assessmentInfo.getCompetenceAssessmentId());
			}
			logger.error(e);
			//rethrow exception so caller of this method can react in appropriate way
			throw e;
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
		List<ActivityResultData> results = assessmentsSummary.getStudentResults();
		if (results != null) {
			for (ActivityResultData ard : results) {
				if (encodedActivityDiscussionId.equals(ard.getAssessment().getEncodedDiscussionId())) {
					return Optional.of(ard.getAssessment());
				}
			}
		}
		return Optional.empty();
	}
	
	//assessment end
	
	public ActivityResultType getActivityResultType() {
		return assessmentsSummary != null ? assessmentsSummary.getResultType() : ActivityResultType.NONE;
	}
	
	@Override
	public void changePage(int page) {
		if (this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			searchStudentResults(false);
		}
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

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public ActivityAssessmentsSummaryData getAssessmentsSummary() {
		return assessmentsSummary;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public ActivityResultData getCurrentResult() {
		return currentResult;
	}

	public void setCurrentResult(ActivityResultData currentResult) {
		this.currentResult = currentResult;
		initRubricIfNotInitialized();
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

	public ResourceAccessData getAccess() {
		return access;
	}
	
}