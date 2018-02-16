package org.prosolo.web.courses.credential;

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
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.assessments.ActivityAssessmentBean;
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
				access = credManager.getResourceAccessData(decodedCredId, loggedUserBean.getUserId(),
						ResourceAccessRequirements.of(AccessMode.MANAGER)
								.addPrivilege(UserGroupPrivilege.Edit)
								.addPrivilege(UserGroupPrivilege.Instruct));

				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					assessmentsSummary = activityManager
							.getActivityAssessmentsDataForInstructorCredentialAssessment(
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
							.getActivityAssessmentDiscussionMessages(idEncoder.decodeId(assessment.getEncodedDiscussionId()),
									assessment.getAssessorId()));
				}
				assessment.setMessagesInitialized(true);
			}
			this.currentResult = result;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage("Error while trying to initialize assessment comments");
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