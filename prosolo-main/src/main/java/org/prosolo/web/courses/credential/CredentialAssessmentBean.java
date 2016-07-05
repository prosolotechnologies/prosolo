package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.prosolo.services.nodes.data.ActivityAssessmentData;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.AssessmentData;
import org.prosolo.services.nodes.data.CompetenceAssessmentData;
import org.prosolo.services.nodes.data.FullAssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialAssessmentBean")
@Component("credentialAssessmentBean")
@Scope("view")
public class CredentialAssessmentBean implements Serializable {

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

	// PARAMETERS
	private String id;
	private long decodedId;

	// used when managing single assessment
	private String assessmentId;
	private long decodedAssessmentId;
	private FullAssessmentData fullAssessmentData;
	private String reviewText;

	// used for managing multiple assessments
	private String credentialTitle;
	private String context;
	private List<AssessmentData> assessmentData;
	private boolean searchForPending = true;
	private boolean searchForApproved = true;
	
	//adding new comment
	private String newCommentValue;
	
	@Inject
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private EventFactory eventFactory;

	public void init() {

		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedId;
			try {
				String title = credManager.getCredentialTitleForCredentialWithType(decodedId,
						LearningResourceType.UNIVERSITY_CREATED);
				if (title != null) {
					credentialTitle = title;
					assessmentData = assessmentManager.getAllAssessmentsForCredential(decodedId,
							loggedUserBean.getUser().getId(), searchForPending, searchForApproved, idEncoder,
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
					fullAssessmentData = assessmentManager.getFullAssessmentData(decodedAssessmentId, 
							idEncoder, loggedUserBean.getUser(), new SimpleDateFormat("MMMM dd, yyyy"));
					credentialTitle = fullAssessmentData.getTitle();

			} catch (Exception e) {
				logger.error("Error while loading assessment data", e);
				PageUtil.fireErrorMessage("Error while loading assessment data");
			}
		}

	}
	
	public void approveCredential() {
		try {
			assessmentManager.approveCredential(idEncoder.decodeId(fullAssessmentData.getEncodedId()),
					fullAssessmentData.getTargetCredentialId(),reviewText);
			markCredentialApproved();
			
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			notifyAssessmentApprovedAsync(decodedAssessmentId, page, lContext, service,
					fullAssessmentData.getAssessedStrudentId(),fullAssessmentData.getCredentialId());
			
			PageUtil.fireSuccessfulInfoMessage("You have sucessfully approved credential for "+fullAssessmentData.getStudentFullName());
		}
		catch (Exception e) {
			logger.error("Error aproving assessment data", e);
			PageUtil.fireErrorMessage("Error while approving assessment data");
		}
	}

	private void markCredentialApproved() {
		fullAssessmentData.setApproved(true);
		for(CompetenceAssessmentData compAssessmentData : fullAssessmentData.getCompetenceAssessmentData()) {
			compAssessmentData.setApproved(true);
		}
	}
	
	public void approveCompetence(String encodedCompetenceAssessmentId) {
		try {
			assessmentManager.approveCompetence(idEncoder.decodeId(encodedCompetenceAssessmentId));
			markCompetenceApproved(encodedCompetenceAssessmentId);
			PageUtil.fireSuccessfulInfoMessage("assessCredentialFormGrowl","You have sucessfully approved competence for "+fullAssessmentData.getStudentFullName());
		}
		catch (Exception e) {
			logger.error("Error aproving assessment data", e);
			PageUtil.fireErrorMessage("Error while approving assessment data");
		}
	}
	
	private void notifyAssessmentApprovedAsync(long decodedAssessmentId, String page, String lContext,
			String service, long assessedStudentId, long credentialId) {
		taskExecutor.execute(() -> {
			User student = new User();
			student.setId(assessedStudentId);
			CredentialAssessment assessment = new CredentialAssessment();
			assessment.setId(decodedAssessmentId);
			Map<String,String> parameters = new HashMap<>();
			parameters.put("credentialId", credentialId+""); 
			try {
				eventFactory.generateEvent(EventType.AssessmentApproved, loggedUserBean.getUser(), 
						student, assessment, 
						page, lContext, service, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});
	}

	private void markCompetenceApproved(String encodedCompetenceAssessmentId) {
		for(CompetenceAssessmentData competenceAssessment : fullAssessmentData.getCompetenceAssessmentData()) {
			if(competenceAssessment.getCompetenceAssessmentEncodedId().equals(encodedCompetenceAssessmentId)) {
				competenceAssessment.setApproved(true);
			}
		}
		
	}
	
	public void markDiscussionRead() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String encodedActivityDiscussionId = params.get("encodedActivityDiscussionId");
		if(StringUtils.isBlank(encodedActivityDiscussionId)){
			logger.error("User "+loggedUserBean.getUser().getId()+" tried to add comment without discussion id");
			PageUtil.fireErrorMessage("Unable to add comment");
		}
		else {
			assessmentManager.markDiscussionAsSeen(loggedUserBean.getUser().getId(), idEncoder.decodeId(encodedActivityDiscussionId));
			Optional<ActivityAssessmentData> seenActivityAssessment = getActivityAssessmentByEncodedId(encodedActivityDiscussionId);
			seenActivityAssessment.ifPresent(data -> data.setAllRead(true));
		}
	}

	private Optional<ActivityAssessmentData> getActivityAssessmentByEncodedId(String encodedActivityDiscussionId) {
		List<CompetenceAssessmentData> competenceAssessmentData = fullAssessmentData.getCompetenceAssessmentData();
		if(CollectionUtils.isNotEmpty(competenceAssessmentData)) {
			for(CompetenceAssessmentData comp : competenceAssessmentData) {
				for(ActivityAssessmentData act : comp.getActivityAssessmentData()) {
					if(act.getEncodedDiscussionId().equals(encodedActivityDiscussionId)) {
						return Optional.of(act);
					}
				}
			}
		}
		return Optional.empty();
	}

	public void addCommentToActivityDiscussion(String encodedActivityDiscussionId, String encodedTargetActivityId,String encodedCompetenceAssessmentId) {
		long actualDiscussionId;
		if(StringUtils.isBlank(encodedActivityDiscussionId)) {
			actualDiscussionId = createDiscussion(encodedTargetActivityId,encodedCompetenceAssessmentId);
		}
		else {
			actualDiscussionId = idEncoder.decodeId(encodedActivityDiscussionId);
		}
		addComment(actualDiscussionId,idEncoder.decodeId(encodedCompetenceAssessmentId));
		cleanupCommentData();
	}
	
	public boolean isCurrentUserMessageSender(ActivityDiscussionMessageData messageData) {
		return idEncoder.encodeId(loggedUserBean.getUser().getId()).equals(messageData.getEncodedSenderId());
	}
	
	public void editComment(String newContent, String activityMessageEncodedId) {
		long activityMessageId = idEncoder.decodeId(activityMessageEncodedId);
		try {
			assessmentManager.editCommentContent(activityMessageId, loggedUserBean.getUser().getId(), newContent);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error editing message with id : "+activityMessageId, e);
			PageUtil.fireErrorMessage("Error editing message");
		}
	}

	private void addComment(long actualDiscussionId, long competenceAssessmentId) {
		try {
			ActivityDiscussionMessageData newComment = assessmentManager.addCommentToDiscussion(actualDiscussionId, loggedUserBean.getUser().getId(), newCommentValue);
			addNewCommentToAssessmentData(newComment,actualDiscussionId,competenceAssessmentId);
			
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			notifyAssessmentCommentAsync(decodedAssessmentId, page, lContext, service,
					getCommentRecepientId(),fullAssessmentData.getCredentialId());
			
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error saving assessment message", e);
			PageUtil.fireErrorMessage("Error while adding new assessment message");
		}
	}

	private void addNewCommentToAssessmentData(ActivityDiscussionMessageData newComment, long actualDiscussionId, long competenceAssessmentId) {
		if(isCurrentUserAssessor()) {
			newComment.setSenderInsructor(true);
		}
		Optional<ActivityAssessmentData> newCommentActivity = getActivityAssessmentByEncodedId(idEncoder.encodeId(actualDiscussionId));
		newCommentActivity.ifPresent(data -> data.getActivityDiscussionMessageData().add(newComment));
		
	}
	
	private void notifyAssessmentCommentAsync(long decodedAssessmentId, String page, String lContext,
			String service, long recepientId, long credentialId) {
		taskExecutor.execute(() -> {
			User recipient = new User();
			recipient.setId(recepientId);
			CredentialAssessment assessment = new CredentialAssessment();
			assessment.setId(decodedAssessmentId);
			Map<String,String> parameters = new HashMap<>();
			parameters.put("credentialId", credentialId+"");
			//in order to construct a link, we will need info if the notification recipient is assessor (to prepend "manage")
			parameters.put("isRecepientAssessor", ((Boolean)(fullAssessmentData.getAssessorId() == recepientId)).toString());
			try {
				eventFactory.generateEvent(EventType.AssessmentComment, loggedUserBean.getUser(), 
						recipient, assessment, 
						page, lContext, service, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});
	}

	public boolean isCurrentUserAssessor() {
		if(fullAssessmentData == null) {
			return false;
		}
		else return loggedUserBean.getUser().getId() == fullAssessmentData.getAssessorId();
	}
	
	private long getCommentRecepientId() {
		//logged user is either assessor or assessee
		long currentUserId = loggedUserBean.getUser().getId();
		if(fullAssessmentData.getAssessorId() == currentUserId) {
			//current user is assessor, get the other id
			return fullAssessmentData.getAssessedStrudentId();
		}
		else return fullAssessmentData.getAssessorId();
		
	}

	private long createDiscussion(String encodedTargetActivityId, String encodedCompetenceAssessmentId) {
		long targetActivityId = idEncoder.decodeId(encodedTargetActivityId);
		long competenceAssessmentId = idEncoder.decodeId(encodedCompetenceAssessmentId);
		return assessmentManager.createActivityDiscussion(targetActivityId,competenceAssessmentId,
				Arrays.asList(fullAssessmentData.getAssessorId(),fullAssessmentData.getAssessedStrudentId()),
				loggedUserBean.getUser().getId());
	}

	private void cleanupCommentData() {
		newCommentValue = "";
		
	}

	public void searchForAll() {
		//only reinitialize when at least one is already false
		if(!searchForApproved || !searchForPending) {
			searchForApproved = true;
			searchForPending = true;
			init();
		}
	}

	public void searchForNone() {
		//only reinitialize when at least one is true
		if(searchForApproved || searchForPending) {
			searchForApproved = false;
			searchForPending = false;
			init();
		}
	}

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

	public boolean isSearchForPending() {
		return searchForPending;
	}

	public void setSearchForPending(boolean searchForPending) {
		this.searchForPending = searchForPending;
		init();
		RequestContext.getCurrentInstance().update("assessmentList:filterAssessmentsForm");
	}

	public boolean isSearchForApproved() {
		return searchForApproved;
	}

	public void setSearchForApproved(boolean searchForApproved) {
		this.searchForApproved = searchForApproved;
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

	public FullAssessmentData getFullAssessmentData() {
		return fullAssessmentData;
	}

	public void setFullAssessmentData(FullAssessmentData fullAssessmentData) {
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
	
	
	
}
