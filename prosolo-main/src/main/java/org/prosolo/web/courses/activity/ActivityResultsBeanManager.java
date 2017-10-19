package org.prosolo.web.courses.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.assessments.*;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
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

@ManagedBean(name = "activityResultsBeanManager")
@Component("activityResultsBeanManager")
@Scope("view")
public class ActivityResultsBeanManager implements Serializable, Paginable {

	private static final long serialVersionUID = 8239391131153298750L;

	private static Logger logger = Logger.getLogger(ActivityResultsBeanManager.class);
	
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
	private String compId;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;
	private String targetActId;
	private long decodedTargetActId;
	private String commentId;
	
	private ActivityData activity;
	private String credentialTitle;
	private String competenceTitle;

	private static final boolean paginate = false;
	private PaginationData paginationData = new PaginationData();
	
	private ActivityResultData currentResult;
	
	private ResourceAccessData access;
	
	private List<StudentAssessedFilterState> filters = new ArrayList<>();
	private List<StudentAssessedFilter> appliedFilters = new ArrayList<>();

	public void init() {
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);
		if (decodedActId > 0 && decodedCompId > 0) {
			try {
				/*
				 * check if user has instructor privilege for this resource and if has, we should mark his comments as
				 * instructor comments
				 */
				access = compManager.getResourceAccessData(decodedCompId, loggedUserBean.getUserId(),
						ResourceAccessRequirements.of(AccessMode.MANAGER)
								.addPrivilege(UserGroupPrivilege.Edit)
								.addPrivilege(UserGroupPrivilege.Instruct));

				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					for (StudentAssessedFilter filterEnum : StudentAssessedFilter.values()) {
						StudentAssessedFilterState f = new StudentAssessedFilterState(filterEnum, true);
						filters.add(f);
						appliedFilters.add(filterEnum);
					}
					activity = activityManager
							.getActivityDataWithStudentResultsForManager(
									decodedCredId, decodedCompId, decodedActId, 0, access.isCanInstruct(),
									true, paginate, paginationData.getPage() - 1, paginationData.getLimit(),
									null);
					//				for(ActivityResultData ard : activity.getStudentResults()) {
					//					loadAdditionalData(ard);
					//				}

					if (paginate) {
						updatePaginationData(countStudentResults(null));
					}
					if (activity == null) {
						PageUtil.notFound();
					} else {
						loadCompetenceAndCredentialTitle();
					}
				}
			} catch (ResourceNotFoundException rnfe) {
				logger.error(rnfe);
				PageUtil.notFound();
			} catch(Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error while loading activity results");
			}
		} else {
			PageUtil.notFound();
		}
	}

	private void initRubricIfNotInitialized() {
		try {
			if (currentResult.getAssessment().getGrade().getGradingMode() == GradingMode.MANUAL_RUBRIC && !currentResult.getAssessment().getGrade().isRubricInitialized()) {
				currentResult.getAssessment().getGrade().setRubricCategories(rubricManager.getRubricDataForActivity(
						currentResult.getAssessment().getActivityId(),
						idEncoder.decodeId(currentResult.getAssessment().getEncodedDiscussionId()),
						true));
				currentResult.getAssessment().getGrade().setRubricInitialized(true);
			}
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data. Please refresh the page and try again.");
		}
	}
	
	public void initIndividualResponse() {
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);
		decodedTargetActId = idEncoder.decodeId(targetActId);
		if (decodedActId > 0 && decodedCompId > 0 && decodedTargetActId > 0) {
			try {
				/*
				 * check if user has instructor privilege for this resource and if has, we should mark his comments as
				 * instructor comments
				 */
				access = compManager.getResourceAccessData(decodedCompId, loggedUserBean.getUserId(),
						ResourceAccessRequirements.of(AccessMode.MANAGER)
								.addPrivilege(UserGroupPrivilege.Edit)
								.addPrivilege(UserGroupPrivilege.Instruct));

				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					activity = activityManager
							.getActivityDataWithStudentResultsForManager(
									decodedCredId, decodedCompId, decodedActId, decodedTargetActId,
									access.isCanInstruct(), true, false, 0,
									0, null);
					if (activity.getStudentResults() != null && !activity.getStudentResults().isEmpty()) {
						currentResult = activity.getStudentResults().get(0);
						//loadAdditionalData(currentResult);
						if (commentId != null) {
							currentResult.getResultComments().setCommentId(idEncoder.decodeId(commentId));
							initializeResultCommentsIfNotInitialized(currentResult);
						}

						if (activity == null || currentResult == null) {
							PageUtil.notFound();
						} else {
							loadCompetenceAndCredentialTitle();
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
				PageUtil.fireErrorMessage("Error while loading activity results");
			}
		} else {
			PageUtil.notFound();
		}
	}
	
	public long getCredentialId() {
		return decodedCredId;
	}
	
	public long getTargetActivityId() {
		return decodedTargetActId;
	}
	
	public String getResultOwnerFullName() {
		return currentResult.getUser().getFullName();
	}
	
	private int countStudentResults(StudentAssessedFilter filter) {
		return (activityManager.countStudentsResults(decodedCredId, decodedCompId, 
				decodedActId, filter)).intValue();
	}

	private void searchStudentResults(boolean calculateNumberOfResults) {
		try {
			if (appliedFilters.isEmpty()) {
				updatePaginationData(0);
				activity.setStudentResults(new ArrayList<>());
			} else {
				StudentAssessedFilter saFilter = getFilter();
				if (paginate) {
					updatePaginationData(countStudentResults(saFilter));
				}
				List<ActivityResultData> results = activityManager
						.getStudentsResults(decodedCredId, decodedCompId, decodedActId, 0, 0, 
								access.isCanInstruct(), true, true, true, paginate, paginationData.getPage() - 1,
								paginationData.getLimit(), saFilter);
//				for(ActivityResultData ard : results) {
//					loadAdditionalData(ard);
//				}
				activity.setStudentResults(results);
			}
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading activity results");
		}
	}
	
	private StudentAssessedFilter getFilter() {
		if(appliedFilters.isEmpty() || appliedFilters.size() == filters.size()) {
			return null;
		}
		return appliedFilters.get(0);
	}
	
	private void loadCompetenceAndCredentialTitle() {
		decodedCredId = idEncoder.decodeId(credId);
		competenceTitle = compManager.getCompetenceTitle(decodedCompId);
		credentialTitle = credManager.getCredentialTitle(decodedCredId);
	}
	
	public void initializeResultCommentsIfNotInitialized(ActivityResultData result) {
		try {
			actResultBean.initializeResultCommentsIfNotInitialized(result);
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public void filterChanged(StudentAssessedFilterState filter) {
		if(filter.isApplied()) {
			appliedFilters.add(filter.getFilter());
		} else {
			appliedFilters.remove(filter.getFilter());
		}
		paginationData.setPage(1);
		searchStudentResults(true);
	}
	
	public void checkAllFilters() {
		if (appliedFilters.size() != filters.size()) {
			for (StudentAssessedFilterState filter : filters) {
				if (!filter.isApplied()) {
					filter.setApplied(true);
					appliedFilters.add(filter.getFilter());
				}
			}
			paginationData.setPage(1);
			searchStudentResults(true);
		}
	}
	
	public void uncheckAllFilters() {
		if (!appliedFilters.isEmpty()) {
			appliedFilters.clear();
			for (StudentAssessedFilterState filter : filters) {
				filter.setApplied(false);
			}
			paginationData.setPage(1);
			searchStudentResults(true);
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
	
//	private void loadAdditionalData(ActivityResultData result) {
//		if (decodedCredId > 0) {
//			ActivityAssessmentData assessment = result.getAssessment();
//			AssessmentBasicData assessmentInfo = assessmentManager.getDefaultAssessmentBasicData(decodedCredId,
//					decodedCompId, 0, result.getUser().getId());
//			if (assessmentInfo != null) {
//				assessment.setCompAssessmentId(assessmentInfo.getCompetenceAssessmentId());
//				assessment.setCredAssessmentId(assessmentInfo.getCredentialAssessmentId());
//				//we need to know if logged in user is assessor to determine if he can participate in private conversation
//				assessment.setAssessorId(assessmentInfo.getAssessorId());
//			}
//		}
//	}
	
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
				//if competence assessment exists create activity assessment only
				ActivityAssessment aa =
						assessmentManager.createActivityDiscussion(targetActivityId, competenceAssessmentId,
								currentResult.getAssessment().getCredAssessmentId(), new ArrayList<Long>(participantIds),
								loggedUserBean.getUserId(), true, grade, true,
								loggedUserBean.getUserContext());
				currentResult.getAssessment().setEncodedDiscussionId(idEncoder.encodeId(aa.getId()));
				currentResult.getAssessment().getGrade().setValue(aa.getPoints());
			} else {
				//if competence assessment does not exist create competence assessment and activity assessment
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
				if competence assessment is already set, get activity assessment id and set it, otherwise get both
				competence assessment and activity assessment ids.
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
		List<ActivityResultData> results = activity.getStudentResults();
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
		if(activity == null) {
			return ActivityResultType.NONE;
		}
		List<ActivityResultData> results = activity.getStudentResults();
		if(results == null || results.isEmpty()) {
			return ActivityResultType.NONE;
		}
		return results.get(0).getResultType();
	}
	
	private void updatePaginationData(int numberOfResults) {
		this.paginationData.update(numberOfResults);
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

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public String getCompetenceTitle() {
		return competenceTitle;
	}

	public void setCompetenceTitle(String competenceTitle) {
		this.competenceTitle = competenceTitle;
	}

	public ActivityData getActivity() {
		return activity;
	}

	public void setActivity(ActivityData activity) {
		this.activity = activity;
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
	
	public List<StudentAssessedFilterState> getFilters() {
		return filters;
	}

	public void setFilters(List<StudentAssessedFilterState> filters) {
		this.filters = filters;
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