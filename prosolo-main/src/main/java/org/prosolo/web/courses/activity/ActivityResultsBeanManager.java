package org.prosolo.web.courses.activity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityAssessmentData;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.StudentAssessedFilter;
import org.prosolo.services.nodes.data.StudentAssessedFilterState;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
	@Inject private ThreadPoolTaskExecutor taskExecutor;
	@Inject private EventFactory eventFactory;

	private String actId;
	private long decodedActId;
	private String compId;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;
	
	private ActivityData activity;
	private String credentialTitle;
	private String competenceTitle;

	private int page = 1;
	private static final int LIMIT = 10;
	private static final boolean paginate = false;
	private int numberOfPages;
	private int resultsNumber;
	
	private boolean hasInstructorCapability;

	private List<PaginationLink> paginationLinks;

	private ActivityResultData currentResult;
	// adding new comment
	private String newCommentValue;
	
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
				for(StudentAssessedFilter filterEnum : StudentAssessedFilter.values()) {
					StudentAssessedFilterState f = new StudentAssessedFilterState(filterEnum, true);
					filters.add(f);
					appliedFilters.add(filterEnum);
				}
				if(paginate) {
					countStudentResults(null);
				}
				activity = activityManager
						.getActivityDataWithStudentResultsForManager(
								decodedCredId, decodedCompId, decodedActId, hasInstructorCapability,
								paginate, page - 1, LIMIT, null);
				for(ActivityResultData ard : activity.getStudentResults()) {
					loadAdditionalData(ard);
				}
				
				if(paginate) {
					generatePagination();
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
	
	private void searchStudentResults(boolean calculateNumberOfResults) {
		try {
			if(appliedFilters.isEmpty()) {
				resultsNumber = 0;
				activity.setStudentResults(new ArrayList<>());
			} else {
				StudentAssessedFilter saFilter = getFilter();
				if(paginate) {
					countStudentResults(saFilter);
				}
				List<ActivityResultData> results = activityManager
						.getStudentsResults(decodedCredId, decodedCompId, decodedActId, 0, 
								hasInstructorCapability, true, paginate, page - 1, LIMIT, saFilter);
				for(ActivityResultData ard : results) {
					loadAdditionalData(ard);
				}
				activity.setStudentResults(results);
			}
			generatePagination();
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading activity results");
		}
	}
	
	private void countStudentResults(StudentAssessedFilter filter) {
		resultsNumber = (activityManager.countStudentsResults(decodedCredId, decodedCompId, 
				decodedActId, filter)).intValue();
	}
	
	private StudentAssessedFilter getFilter() {
		if(appliedFilters.isEmpty() || appliedFilters.size() == filters.size()) {
			return null;
		}
		return appliedFilters.get(0);
	}
	
	private void generatePagination() {
		//if we don't want to generate all links
		Paginator paginator = new Paginator(resultsNumber, LIMIT, page, 
				1, "...");
		//if we want to genearte all links in paginator
//		Paginator paginator = new Paginator(courseMembersNumber, limit, page, 
//				true, "...");
		numberOfPages = paginator.getNumberOfPages();
		paginationLinks = paginator.generatePaginationLinks();
		logger.info("Number of pages for students results search: " + numberOfPages);
	}
	
	private void loadCompetenceAndCredentialTitle() {
		decodedCredId = idEncoder.decodeId(credId);
		competenceTitle = compManager.getCompetenceTitle(decodedCompId);
		credentialTitle = credManager.getCredentialTitle(decodedCredId);
	}
	
	public void filterChanged(StudentAssessedFilterState filter) {
		if(filter.isApplied()) {
			appliedFilters.add(filter.getFilter());
		} else {
			appliedFilters.remove(filter.getFilter());
		}
		page = 1;
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
			page = 1;
			searchStudentResults(true);
		}
	}
	
	public void uncheckAllFilters() {
		if (!appliedFilters.isEmpty()) {
			appliedFilters.clear();
			for (StudentAssessedFilterState filter : filters) {
				filter.setApplied(false);
			}
			page = 1;
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
		ActivityResultData result = activityManager.getActivityResultData(targetActivityId, loadComments, loggedUserBean.hasCapability("BASIC.INSTRUCTOR.ACCESS"), loggedUserBean.getUserId());
		
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
	
	public void editComment(String newContent, String activityMessageEncodedId) {
		long activityMessageId = idEncoder.decodeId(activityMessageEncodedId);
		try {
			assessmentManager.editCommentContent(activityMessageId, loggedUserBean.getUserId(), newContent);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error editing message with id : " + activityMessageId, e);
			PageUtil.fireErrorMessage("Error editing message");
		}
	}
	
	public void addCommentToActivityDiscussion() {
		long actualDiscussionId;
		if (StringUtils.isBlank(currentResult.getAssessment().getEncodedDiscussionId())) {
			actualDiscussionId = createDiscussion(currentResult.getTargetActivityId(), 
					currentResult.getAssessment().getCompAssessmentId());
			
			// set discussionId in the appropriate ActivityAssessmentData
			String encodedDiscussionId = idEncoder.encodeId(actualDiscussionId);
			
			currentResult.getAssessment().setEncodedDiscussionId(encodedDiscussionId);
		} else {
			actualDiscussionId = idEncoder.decodeId(currentResult.getAssessment().getEncodedDiscussionId());
		}
		addComment(actualDiscussionId, currentResult.getAssessment().getCompAssessmentId());
		cleanupCommentData();
	}
	
	public void updateGrade() {
		try {
			long compAssessmentId = currentResult.getAssessment().getCompAssessmentId();
			
			if (StringUtils.isBlank(currentResult.getAssessment().getEncodedDiscussionId())) {
				long actualDiscussionId = createDiscussion(currentResult.getTargetActivityId(), 
						compAssessmentId);
				
				// set discussionId in the appropriate ActivityAssessmentData
				String encodedDiscussionId = idEncoder.encodeId(actualDiscussionId);
				
				currentResult.getAssessment().setEncodedDiscussionId(encodedDiscussionId);
			} else {
				assessmentManager.updateGradeForActivityAssessment(
						idEncoder.decodeId(currentResult.getAssessment().getEncodedDiscussionId()),
						currentResult.getAssessment().getGrade().getValue());
			}
			
			long credAssessmentId = currentResult.getAssessment().getCredAssessmentId();
			
			// recalculate points of parent competence and credential assessments
			assessmentManager.recalculateScoreForCompetenceAssessment(compAssessmentId);
			assessmentManager.recalculateScoreForCredentialAssessment(credAssessmentId);
						
			PageUtil.fireSuccessfulInfoMessage("Grade updated");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error while updating grade");
		}
	}
	
	private long createDiscussion(long targetActivityId, long competenceAssessmentId) {
		try {
			return assessmentManager.createActivityDiscussion(targetActivityId, competenceAssessmentId,
					Arrays.asList(currentResult.getAssessment().getAssessorId(), 
							currentResult.getUser().getId()),
					loggedUserBean.getUserId(), true,  
					currentResult.getAssessment().getGrade().getValue());
		} catch (ResourceCouldNotBeLoadedException e) {
			return -1;
		}
	}
	
	private void addComment(long actualDiscussionId, long competenceAssessmentId) {
		try {
			ActivityDiscussionMessageData newComment = assessmentManager.addCommentToDiscussion(actualDiscussionId,
					loggedUserBean.getUserId(), newCommentValue);
			addNewCommentToAssessmentData(newComment, actualDiscussionId, competenceAssessmentId);

			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			notifyAssessmentCommentAsync(currentResult.getAssessment().getCredAssessmentId(), page, 
					lContext, service, getCommentRecepientId(), decodedCredId);

		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error saving assessment message", e);
			PageUtil.fireErrorMessage("Error while adding new assessment message");
		}
	}
	
	private void addNewCommentToAssessmentData(ActivityDiscussionMessageData newComment, long actualDiscussionId,
			long competenceAssessmentId) {
		if (isCurrentUserAssessor(currentResult)) {
			newComment.setSenderInsructor(true);
		}
		currentResult.getAssessment().getActivityDiscussionMessageData().add(newComment);
		currentResult.getAssessment().setNumberOfMessages(
				currentResult.getAssessment().getNumberOfMessages() + 1);
	}
	
	private void notifyAssessmentCommentAsync(long decodedAssessmentId, String page, String lContext, 
			String service, long recepientId, long credentialId) {
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
					((Boolean) (currentResult.getAssessment().getAssessorId() == recepientId)).toString());
			try {
				eventFactory.generateEvent(EventType.AssessmentComment, loggedUserBean.getUserId(), assessment, recipient,
						page, lContext, service, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});
	}
	
	private long getCommentRecepientId() {
		// logged user is either assessor or assessee
		long currentUserId = loggedUserBean.getUserId();
		if (currentResult.getAssessment().getAssessorId() == currentUserId) {
			// current user is assessor, get the other id
			return currentResult.getUser().getId();
		} else
			return currentResult.getAssessment().getAssessorId();

	}
	
	private void cleanupCommentData() {
		newCommentValue = "";
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
	
	@Override
	public boolean isCurrentPageFirst() {
		return page == 1 || numberOfPages == 0;
	}
	
	@Override
	public boolean isCurrentPageLast() {
		return page == numberOfPages || numberOfPages == 0;
	}
	
	@Override
	public void changePage(int page) {
		if(this.page != page) {
			this.page = page;
			searchStudentResults(false);
		}
	}

	@Override
	public void goToPreviousPage() {
		changePage(page - 1);
	}
	
	@Override
	public void goToNextPage() {
		changePage(page + 1);
	}
	
	@Override
	public boolean isResultSetEmpty() {
		return resultsNumber == 0;
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

	public List<PaginationLink> getPaginationLinks() {
		return paginationLinks;
	}

	public void setPaginationLinks(List<PaginationLink> paginationLinks) {
		this.paginationLinks = paginationLinks;
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

	public List<StudentAssessedFilterState> getFilters() {
		return filters;
	}

	public void setFilters(List<StudentAssessedFilterState> filters) {
		this.filters = filters;
	}
	
}
