package org.prosolo.web.assessments;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.ActivityResultType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.assessment.data.ActivityAssessmentData;
import org.prosolo.services.assessment.data.ActivityAssessmentsSummaryData;
import org.prosolo.services.nodes.data.credential.CredentialIdData;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ManagedBean(name = "credentialActivityAssessmentsBeanManager")
@Component("credentialActivityAssessmentsBeanManager")
@Scope("view")
public class CredentialActivityAssessmentsBeanManager implements Serializable, Paginable {

	private static final long serialVersionUID = 6214124501846073070L;

	private static Logger logger = Logger.getLogger(CredentialActivityAssessmentsBeanManager.class);
	
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CredentialManager credManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private AssessmentManager assessmentManager;
	@Inject private ActivityResultBean actResultBean;
	@Inject private ActivityAssessmentBean activityAssessmentBean;

	private String actId;
	private long decodedActId;
	private String credId;
	private long decodedCredId;
	private String targetActId;
	private long decodedTargetActId;
	private String commentId;
	
	private ActivityAssessmentsSummaryData assessmentsSummary;
	private CredentialIdData credentialIdData;

	private static final boolean paginate = false;
	private PaginationData paginationData = new PaginationData();
	
	private ActivityResultData currentResult;

	// used for the component where instructor can see other student's comments on one's activity submission
	private ActivityResultData activityResultWithOtherComments;

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
				access = credManager.getResourceAccessData(decodedCredId, loggedUserBean.getUserId(),
						ResourceAccessRequirements.of(AccessMode.MANAGER)
								.addPrivilege(UserGroupPrivilege.Edit)
								.addPrivilege(UserGroupPrivilege.Instruct));

				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					// check if activity and credential are connected
					activityManager.checkIfActivityIsPartOfACredential(decodedCredId, decodedActId);

					assessmentsSummary = activityManager
							.getActivityAssessmentsDataForInstructorCredentialAssessment(
									decodedCredId, decodedActId, access, loggedUserBean.getUserId(), paginate,
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
				PageUtil.notFound();
			} catch(Exception e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}

	public boolean canUserEditDelivery() {
		return access.isCanEdit();
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
				access = credManager.getResourceAccessData(decodedCredId, loggedUserBean.getUserId(),
						ResourceAccessRequirements.of(AccessMode.MANAGER)
								.addPrivilege(UserGroupPrivilege.Edit)
								.addPrivilege(UserGroupPrivilege.Instruct));

				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					// check if activity and credential are connected
					activityManager.checkIfActivityIsPartOfACredential(decodedCredId, decodedActId);

					assessmentsSummary = activityManager
							.getActivityAssessmentDataForDefaultCredentialAssessment(
									decodedCredId, decodedActId, decodedTargetActId, access.isCanInstruct(), !access.isCanEdit(), loggedUserBean.getUserId());
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
				PageUtil.notFound();
			} catch (Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error loading assessmentsSummary results");
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

	private void searchStudentResults(boolean calculateNumberOfResults) {
		try {
			if (paginate) {
				this.paginationData.update(activityManager.countStudentsLearningCredentialThatCompletedActivity(decodedCredId, decodedActId, !access.isCanEdit(), loggedUserBean.getUserId()).intValue());
			}
			List<ActivityResultData> results = activityManager
					.getStudentsActivityAssessmentsData(decodedCredId, decodedActId, 0, access.isCanInstruct(),
							!access.isCanEdit(), loggedUserBean.getUserId(), paginate, paginationData.getPage() - 1, paginationData.getLimit());
			assessmentsSummary.setStudentResults(results);
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading assessmentsSummary results");
		}
	}
	
	private void loadCredentialTitle() {
		decodedCredId = idEncoder.decodeId(credId);
		credentialIdData = credManager.getCredentialIdData(decodedCredId, null);
	}
	
	public void initializeResultCommentsIfNotInitialized(ActivityResultData result) {
		try {
			actResultBean.initializeResultCommentsIfNotInitialized(result);
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	//assessment begin
	public void loadActivityAssessmentComments(long targetActivityId, ActivityResultData activityResultData, boolean loadDiscussion, boolean loadComments) {
		try {
			ActivityResultData result = activityManager.getActivityResultData(
					targetActivityId,
					loadComments,
					access.isCanInstruct(),
					true,
					loggedUserBean.getUserId());

			ActivityAssessmentData assessment = activityResultData.getAssessment();
			if (!assessment.isMessagesInitialized()) {
				if (assessment.getEncodedActivityAssessmentId() != null && !assessment.getEncodedActivityAssessmentId().isEmpty()) {
					assessment.populateDiscussionMessages(assessmentManager
							.getActivityAssessmentDiscussionMessages(idEncoder.decodeId(assessment.getEncodedActivityAssessmentId()),
									assessment.getAssessorId()));
				}
				assessment.setMessagesInitialized(true);
			}

			this.activityResultWithOtherComments = result;
			this.currentResult = activityResultData;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage("Error trying to initialize assessment comments");
		}
	}
	
	public boolean isCurrentUserMessageSender(AssessmentDiscussionMessageData messageData) {
		return idEncoder.encodeId(loggedUserBean.getUserId()).equals(messageData.getEncodedSenderId());
	}
	
	public boolean isCurrentUserAssessor(ActivityResultData result) {
		return loggedUserBean.getUserId() == result.getAssessment().getAssessorId();
	}

	public void updateGrade() {
		try {
			boolean assessedPreviously = activityAssessmentBean.getActivityAssessmentData().getGrade().isAssessed();
			activityAssessmentBean.updateGrade();
			if (!assessedPreviously) {
				//if student was not previously assessed number of assessed students should be increased by 1
				assessmentsSummary.setNumberOfAssessedStudents(assessmentsSummary.getNumberOfAssessedStudents() + 1);
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}
	
	public void markDiscussionRead() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String encodedActivityDiscussionId = params.get("encodedActivityDiscussionId");

		if (!StringUtils.isBlank(encodedActivityDiscussionId)) {
			assessmentManager.markActivityAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
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
				if (encodedActivityDiscussionId.equals(ard.getAssessment().getEncodedActivityAssessmentId())) {
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
		return credentialIdData.getTitle();
	}

	public CredentialIdData getCredentialIdData() {
		return credentialIdData;
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

	public ActivityResultData getActivityResultWithOtherComments() {
		return activityResultWithOtherComments;
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