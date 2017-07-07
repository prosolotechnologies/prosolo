package org.prosolo.web.courses.activity;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.prosolo.bigdata.common.exceptions.AccessDeniedException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "activityResultsBeanUser")
@Component("activityResultsBeanUser")
@Scope("view")
public class ActivityResultsBeanUser implements Serializable {

	private static final long serialVersionUID = -449306144620746707L;

	private static Logger logger = Logger.getLogger(ActivityResultsBeanUser.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialManager credManager;
	@Inject private CommentManager commentManager;
	@Inject private ActivityResultBean activityResultBean;

	private String targetActId;
	private long decodedTargetActId;
	private String actId;
	private long decodedActId;
	private String compId;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;
	private String commentId;
	
	private CompetenceData1 competenceData;

	private long nextCompToLearn;
	private long nextActivityToLearn;
	private boolean resultOwnerIsLookingThisPage;
	
	private boolean mandatoryOrder;
	
	private ResourceAccessData access;

	public void init() {
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);
		
		if (decodedActId > 0 && decodedCompId > 0) {
			try {
				competenceData = activityManager
						.getTargetCompetenceActivitiesWithResultsForSpecifiedActivity(
								decodedCredId, decodedCompId, decodedActId, loggedUser.getUserId(), false);
				if (competenceData == null) {
					PageUtil.forward("/notfound.xhtml");
				} else {
					//load result comments number
					ActivityData ad = competenceData.getActivityToShowWithDetails();
					
					if (ad.isEnrolled()) {
						int numberOfComments = (int) commentManager.getCommentsNumber(
								CommentedResourceType.ActivityResult, 
								ad.getTargetActivityId());
						CommentsData commData = ad.getResultData().getResultComments();
						commData.setNumberOfComments(numberOfComments);
						
						UserData ud = new UserData(loggedUser.getUserId(), loggedUser.getFullName(), 
								loggedUser.getAvatar(), null, null, true);
						ad.getResultData().setUser(ud);
					}
					
					loadCompetenceAndCredentialTitle();
					loadNextToLearnInfo();
					
					access = compManager.getResourceAccessData(decodedCompId, loggedUser.getUserId(), 
							ResourceAccessRequirements.of(AccessMode.USER));
				}
			} catch (AccessDeniedException ade) {
				PageUtil.accessDenied();
			} catch (ResourceNotFoundException rnfe) {
				PageUtil.notFound();
			} catch(Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error while loading activity results");
			}
		} else {
			PageUtil.notFound();
		}
	}
	
	public void initIndividualResponse() {
		logger.info("Init individual response");
		
		decodedTargetActId = idEncoder.decodeId(targetActId);
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);

		try {
			if (decodedActId > 0 && decodedCompId > 0 && decodedTargetActId > 0) {
				ActivityData activityWithDetails = activityManager.getActivityResponseForUserToView(
						decodedTargetActId, loggedUser.getUserId());

				if (activityWithDetails == null) {
					PageUtil.notFound();
				} else {
					competenceData = new CompetenceData1(false);
					competenceData.setActivityToShowWithDetails(activityWithDetails);
					resultOwnerIsLookingThisPage = competenceData.getActivityToShowWithDetails().getResultData()
							.getUser().getId() == loggedUser.getUserId();

					if (resultOwnerIsLookingThisPage) {
						loadNextToLearnInfo();
					}
					loadCompetenceAndCredentialTitle();
					if (commentId != null) {
						competenceData.getActivityToShowWithDetails().getResultData()
								.getResultComments().setCommentId(idEncoder.decodeId(commentId));
						initializeResultCommentsIfNotInitialized(
								competenceData.getActivityToShowWithDetails().getResultData());
					}
					access = compManager.getResourceAccessData(decodedCompId, loggedUser.getUserId(),
							ResourceAccessRequirements.of(AccessMode.USER));
				}
			} else {
				PageUtil.notFound();
			}
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading activity response");
		}
	}
	
	private void loadCompetenceAndCredentialTitle() {
		competenceData.setTitle(compManager.getCompetenceTitle(decodedCompId));
		
		if (decodedCredId > 0) {
			competenceData.setCredentialTitle(credManager.getCredentialTitle(decodedCredId));
		}
		competenceData.setCredentialId(decodedCredId);
	}
	
	private void loadNextToLearnInfo() {
		//TODO cred-redesign-07 - see what to do with mandatory order
		if (!mandatoryOrder) {
			nextCompToLearn = decodedCompId;
			if(competenceData.getActivities().isEmpty()) {
				LearningInfo li = compManager.getCompetenceLearningInfo(decodedCompId, loggedUser.getUserId());
				nextActivityToLearn = li.getNextActivityToLearn();
			} else {
				for(ActivityData ad : competenceData.getActivities()) {
					if(!ad.isCompleted()) {
						nextActivityToLearn = ad.getActivityId();
						break;
					}
				}
			}
		}
	}

	public void initializeResultCommentsIfNotInitialized(ActivityResultData res) {
		try {
			activityResultBean.initializeResultCommentsIfNotInitialized(res);
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public boolean isNextToLearn() {
		return decodedCompId == nextCompToLearn;
	}
	
	public boolean isActivityActive(ActivityData act) {
		return decodedActId == act.getActivityId();
	}
	
	public boolean isCurrentUserCreator() {
		return competenceData.getActivityToShowWithDetails().getCreatorId() == loggedUser.getUserId();
	}
	
	public String getResultOwnerFullName() {
		return competenceData != null
				? competenceData.getActivityToShowWithDetails().getResultData().getUser().getFullName()
				: null;
	}
	
	public long getTargetActivityId() {
		return decodedTargetActId;
	}

	/*
	 * ACTIONS
	 */
	
	public void completeActivity() {
		try {
			String page = PageUtil.getPostParameter("page");
			String learningContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData lcd = new LearningContextData(page, learningContext, service);
			
			activityManager.completeActivity(
					competenceData.getActivityToShowWithDetails().getTargetActivityId(), 
					competenceData.getActivityToShowWithDetails().getCompetenceId(), 
					loggedUser.getUserId(), lcd);
			competenceData.getActivityToShowWithDetails().setCompleted(true);
			
			for (ActivityData ad : competenceData.getActivities()) {
				if (ad.getActivityId() == competenceData.getActivityToShowWithDetails().getActivityId()) {
					ad.setCompleted(true);
				}
			}
			
			//TODO cred-redesign-07 - see what should be done with next comp and activity to learn id
			loadNextToLearnInfo();
		} catch (Exception e) {
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("Error while marking activity as completed");
		}
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		activityResultBean.uploadAssignment(event, 
				competenceData.getActivityToShowWithDetails().getResultData());
		
		PageUtil.fireSuccessfulInfoMessage("File uploaded");
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public CompetenceData1 getCompetenceData() {
		return competenceData;
	}

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

	public void setCompetenceData(CompetenceData1 competenceData) {
		this.competenceData = competenceData;
	}
	
	public long getNextCompToLearn() {
		return nextCompToLearn;
	}

	public void setNextCompToLearn(long nextCompToLearn) {
		this.nextCompToLearn = nextCompToLearn;
	}

	public long getNextActivityToLearn() {
		return nextActivityToLearn;
	}

	public void setNextActivityToLearn(long nextActivityToLearn) {
		this.nextActivityToLearn = nextActivityToLearn;
	}

	public boolean isResultOwnerIsLookingThisPage() {
		return resultOwnerIsLookingThisPage;
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
