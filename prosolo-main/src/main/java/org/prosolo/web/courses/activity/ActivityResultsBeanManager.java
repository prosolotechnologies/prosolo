package org.prosolo.web.courses.activity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.assessments.ActivityAssessmentData;
import org.prosolo.services.nodes.data.assessments.StudentAssessedFilter;
import org.prosolo.services.nodes.data.assessments.StudentAssessedFilterState;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
	
	private boolean hasInstructorCapability;

	private ActivityResultData currentResult;
	
	private List<StudentAssessedFilterState> filters = new ArrayList<>();
	private List<StudentAssessedFilter> appliedFilters = new ArrayList<>();

	public void init() {
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);
		if (decodedActId > 0 && decodedCompId > 0 && decodedCredId > 0) {
			/*
			 * check if user has instructor capability and if has, we should mark his comments as
			 * instructor comments
			 */
			hasInstructorCapability = loggedUserBean.hasCapability("BASIC.INSTRUCTOR.ACCESS");
			try {
				for (StudentAssessedFilter filterEnum : StudentAssessedFilter.values()) {
					StudentAssessedFilterState f = new StudentAssessedFilterState(filterEnum, true);
					filters.add(f);
					appliedFilters.add(filterEnum);
				}
				activity = activityManager
						.getActivityDataWithStudentResultsForManager(
								decodedCredId, decodedCompId, decodedActId, 0, hasInstructorCapability,
								true, paginate, paginationData.getPage() - 1, paginationData.getLimit(), 
								null);
				for(ActivityResultData ard : activity.getStudentResults()) {
					loadAdditionalData(ard);
				}
				
				if (paginate) {
					updatePaginationData(countStudentResults(null));
				}
				if(activity == null) {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				} else {
					loadCompetenceAndCredentialTitle();
				}
			} catch(Exception e) {
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
	
	public void initIndividualResponse() {
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);
		decodedTargetActId = idEncoder.decodeId(targetActId);
		if (decodedActId > 0 && decodedCompId > 0 && decodedCredId > 0 && decodedTargetActId > 0) {
			/*
			 * check if user has instructor capability and if has, we should mark his comments as
			 * instructor comments
			 */
			hasInstructorCapability = loggedUserBean.hasCapability("BASIC.INSTRUCTOR.ACCESS");
			try {
				activity = activityManager
						.getActivityDataWithStudentResultsForManager(
								decodedCredId, decodedCompId, decodedActId, decodedTargetActId, 
								hasInstructorCapability, true, false, 0, 
								0, null);
				if(activity.getStudentResults() != null && !activity.getStudentResults().isEmpty()) {
					currentResult = activity.getStudentResults().get(0);
					loadAdditionalData(currentResult);
					if(commentId != null) {
						currentResult.getResultComments().setCommentId(idEncoder.decodeId(commentId));
						initializeResultCommentsIfNotInitialized(currentResult);
					}
					
					if(activity == null || currentResult == null) {
						try {
							FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
						} catch (IOException e) {
							logger.error(e);
						}
					} else {
						loadCompetenceAndCredentialTitle();
					}
				} else {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				}
			} catch(Exception e) {
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
								hasInstructorCapability, true, true, paginate, paginationData.getPage() - 1, paginationData.getLimit(), saFilter);
				for(ActivityResultData ard : results) {
					loadAdditionalData(ard);
				}
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
					assessment.setActivityDiscussionMessageData(assessmentManager
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
				loggedUserBean.hasCapability("BASIC.INSTRUCTOR.ACCESS"), 
				true, 
				loggedUserBean.getUserId());
		
//		if (result != null && loadDiscussion) {
			loadActivityDiscussion(result);
//		}
	}
	
	private void loadAdditionalData(ActivityResultData result) {
		ActivityAssessmentData assessment = result.getAssessment();
		CompetenceAssessment ca = assessmentManager.getDefaultCompetenceAssessment(
				decodedCredId, decodedCompId, result.getUser().getId());
		assessment.setCompAssessmentId(ca.getId());
		assessment.setCredAssessmentId(ca.getCredentialAssessment().getId());
		//we need to know if logged in user is assessor to determine if he can participate in private conversation
		assessment.setAssessorId(assessmentManager.getAssessorIdForCompAssessment(
				assessment.getCompAssessmentId()));
	}
	
	public boolean isCurrentUserMessageSender(ActivityDiscussionMessageData messageData) {
		return idEncoder.encodeId(loggedUserBean.getUserId()).equals(messageData.getEncodedSenderId());
	}
	
	public boolean isCurrentUserAssessor(ActivityResultData result) {
		return loggedUserBean.getUserId() == result.getAssessment().getAssessorId();
	}
	
	public void updateGrade() {
		try {
			long compAssessmentId = currentResult.getAssessment().getCompAssessmentId();
			LearningContextData lcd = new LearningContextData();
			lcd.setPage(PageUtil.getPostParameter("page"));
			lcd.setLearningContext(PageUtil.getPostParameter("learningContext"));
			lcd.setService(PageUtil.getPostParameter("service"));
			if (StringUtils.isBlank(currentResult.getAssessment().getEncodedDiscussionId())) {
				long actualDiscussionId = createDiscussion(currentResult.getTargetActivityId(), 
						compAssessmentId, lcd);
				
				// set discussionId in the appropriate ActivityAssessmentData
				String encodedDiscussionId = idEncoder.encodeId(actualDiscussionId);
				
				currentResult.getAssessment().setEncodedDiscussionId(encodedDiscussionId);
			} else {
				assessmentManager.updateGradeForActivityAssessment(
						idEncoder.decodeId(currentResult.getAssessment().getEncodedDiscussionId()),
						currentResult.getAssessment().getGrade().getValue(), 
						loggedUserBean.getUserId(), lcd);
			}
			
			long credAssessmentId = currentResult.getAssessment().getCredAssessmentId();
			
			// recalculate points of parent competence and credential assessments
			assessmentManager.recalculateScoreForCompetenceAssessment(compAssessmentId);
			assessmentManager.recalculateScoreForCredentialAssessment(credAssessmentId);
						
			currentResult.getAssessment().getGrade().setAssessed(true);
			PageUtil.fireSuccessfulInfoMessage("Grade updated");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error while updating grade");
		}
	}
	
	private long createDiscussion(long targetActivityId, long competenceAssessmentId, 
			LearningContextData context) {
		try {
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
			
			return assessmentManager.createActivityDiscussion(targetActivityId, competenceAssessmentId,
					new ArrayList<Long>(participantIds),
					loggedUserBean.getUserId(), true,  
					currentResult.getAssessment().getGrade().getValue(), context).getId();
		} catch (ResourceCouldNotBeLoadedException | EventException e) {
			logger.error(e);
			return -1;
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
	
}