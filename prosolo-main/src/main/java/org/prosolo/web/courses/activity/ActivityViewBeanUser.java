package org.prosolo.web.courses.activity;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.grading.RubricCriteriaGradeData;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.activity.util.ActivityUtil;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ManagedBean(name = "activityViewBean")
@Component("activityViewBean")
@Scope("view")
public class ActivityViewBeanUser implements Serializable {

	private static final long serialVersionUID = -8910052333513137853L;

	private static Logger logger = Logger.getLogger(ActivityViewBeanUser.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CommentBean commentBean;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialManager credManager;
	@Inject private RoleManager roleManager;
	@Inject private CommentManager commentManager;
	@Inject private ActivityResultBean activityResultBean;
	@Inject private RubricManager rubricManager;

	private String actId;
	private long decodedActId;
	private String compId;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;
	private String commentId;
	
	private CompetenceData1 competenceData;
	//TODO grading refactor on a page rubricCriteria is expected, not rubricGradeData
	//private List<ActivityRubricCriterionData> rubricCriteria;
	private RubricCriteriaGradeData rubricGradeData;
	private ResourceAccessData access;
	private CommentsData commentsData;

	private long nextCompToLearn;
	
	private String roles="Learner";

	private long nextActivityToLearn;
	private boolean mandatoryOrder;

	public void init() {
		List<String> roles = new ArrayList<>();
		roles.add(SystemRoleNames.MANAGER);
		roles.add(SystemRoleNames.INSTRUCTOR);
		boolean hasManagerOrInstructorRole = roleManager.hasAnyRole(loggedUser.getUserId(), roles);
		if (hasManagerOrInstructorRole) {
			this.roles = "Instructor";
		} else {
			this.roles = "Learner";
		}
		
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		
		initializeActivityData();
	}
	
	private void initializeActivityData() {
		if (decodedActId > 0 && decodedCompId > 0) {
			try {
				decodedCredId = idEncoder.decodeId(credId);

				ResourceAccessRequirements req = ResourceAccessRequirements
						.of(AccessMode.USER)
						.addPrivilege(UserGroupPrivilege.Learn)
						.addPrivilege(UserGroupPrivilege.Edit);

				access = compManager.getResourceAccessData(decodedCompId, loggedUser.getUserId(), req);

				competenceData = activityManager
						.getFullTargetActivityOrActivityData(decodedCredId,
								decodedCompId, decodedActId, loggedUser.getUserId(), false);
				//if user is enrolled he can always access the resource
				if (!competenceData.getActivityToShowWithDetails().isEnrolled() && !access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					commentsData = new CommentsData(CommentedResourceType.Activity, 
							competenceData.getActivityToShowWithDetails().getActivityId(), false, false);
					commentsData.setCommentId(idEncoder.decodeId(commentId));
					commentBean.loadComments(commentsData);
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
					
					loadCompetenceAndCredentialTitleAndNextToLearnInfo();
					
					ActivityUtil.createTempFilesAndSetUrlsForCaptions(ad.getCaptions(), loggedUser.getUserId());
				}
			} catch(ResourceNotFoundException rnfe) {
				PageUtil.notFound();
			} catch(Exception e) {
				e.printStackTrace();
				logger.error(e);
				PageUtil.fireErrorMessage("Error loading activity");
			}
		} else {
			PageUtil.notFound();
		}
	}

	public boolean isUserAllowedToSeeRubric() {
		return competenceData.getActivityToShowWithDetails().getRubricVisibility() == ActivityRubricVisibility.ALWAYS;
	}

	public void initializeRubric() {
		try {
			if (rubricGradeData == null) {
				rubricGradeData = rubricManager.getRubricDataForActivity(
						competenceData.getActivityToShowWithDetails().getActivityId(),
						0,
						false);
			}
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data. Please refresh the page and try again.");
		}
	}
	
	private void loadCompetenceAndCredentialTitleAndNextToLearnInfo() {
		String compTitle = null;
		String credTitle = null;
		decodedCredId = idEncoder.decodeId(credId);
		if(competenceData.getActivityToShowWithDetails().isEnrolled()) {
			//LearningInfo li = compManager.getCompetenceLearningInfo(decodedCompId, loggedUser.getUserId());
			//compTitle = li.getCompetenceTitle();
			//TODO revisit when it is clear what mandatory order means in new design
			//nextActivityToLearn = li.getNextActivityToLearn();
			compTitle = compManager.getCompetenceTitle(decodedCompId);
			//TODO cred-redesign-07 what to do with mandatory order now when competence is independent resource
			//if(decodedCredId > 0) {
//					LearningInfo credLI = credManager
//							.getCredentialLearningInfo(decodedCredId, loggedUser.getUserId(), false);
//					credTitle = credLI.getCredentialTitle();
//					mandatoryOrder = credLI.isMandatoryFlow();
			if (decodedCredId > 0) {
				credTitle = credManager.getCredentialTitle(decodedCredId);
			}
			//}
			if (!mandatoryOrder) {
				nextCompToLearn = decodedCompId;
				for (ActivityData ad : competenceData.getActivities()) {
					if(!ad.isCompleted()) {
						nextActivityToLearn = ad.getActivityId();
						break;
					}
				}
			}
		} else {
			compTitle = compManager.getCompetenceTitle(decodedCompId);
			if(decodedCredId > 0) {
				credTitle = credManager.getCredentialTitle(decodedCredId);
			}
		}
		competenceData.setTitle(compTitle);
		competenceData.setCredentialId(decodedCredId);
		competenceData.setCredentialTitle(credTitle);
	}
	
	public void initializeResultCommentsIfNotInitialized(ActivityResultData resultData) {
		try {
			CommentsData cd = resultData.getResultComments();
			if(!cd.isInitialized()) {
				cd.setInstructor(false);
				commentBean.loadComments(resultData.getResultComments());
			}
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public void enrollInCompetence() {
		try {
			PageContextData lcd = new PageContextData();
			lcd.setPage(FacesContext.getCurrentInstance().getViewRoot().getViewId());
			lcd.setLearningContext(PageUtil.getPostParameter("context"));
			lcd.setService(PageUtil.getPostParameter("service"));
			compManager.enrollInCompetence(decodedCompId, loggedUser.getUserId(), loggedUser.getUserContext(lcd));
			//initializeActivityData();

			if (decodedCredId > 0) {
				PageUtil.redirect("/credentials/" + credId + "/" + compId + "/" + actId);
			} else {
				PageUtil.redirect("/competences/" + compId + "/" + actId);
			}
		} catch(DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage(e.getMessage());
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

	/*
	 * ACTIONS
	 */
	
	public void completeActivity() {
		try {
			activityManager.completeActivity(
					competenceData.getActivityToShowWithDetails().getTargetActivityId(), 
					competenceData.getActivityToShowWithDetails().getCompetenceId(),
					loggedUser.getUserContext());
			competenceData.getActivityToShowWithDetails().setCompleted(true);
			
			boolean localNextToLearn = false;
			for (ActivityData ad : competenceData.getActivities()) {
				if (ad.getActivityId() == competenceData.getActivityToShowWithDetails().getActivityId()) {
					ad.setCompleted(true);
				}
				if(!ad.isCompleted() && !mandatoryOrder && !localNextToLearn) {
					nextCompToLearn = decodedCompId;
					nextActivityToLearn = ad.getActivityId();
					localNextToLearn = true;
				}
			}
			
			try {
				/*
				 * if all activities from current competence are completed and id of a credential is passed,
				 * we retrieve the next competence and activity to learn from credential
				 */
				//TODO for now we don't want to navigate to another competence so if competence is completed we don't show continue button
//				if(!localNextToLearn && decodedCredId > 0) {
//					LearningInfo li = credManager.getCredentialLearningInfo(decodedCredId, 
//							loggedUser.getUserId(), true);
//					nextCompToLearn = li.getNextCompetenceToLearn();
//					nextActivityToLearn = li.getNextActivityToLearn();
//				}
				if(!localNextToLearn) {
					nextCompToLearn = 0;
				}
			} catch(DbConnectionException e) {
				logger.error(e);
			}
			
			PageUtil.fireSuccessfulInfoMessage("The activity has been completed");
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error marking the activity as completed");
		}
	}
	
	public void saveTextResponse() {
		try {
			Date postDate = new Date();
			
			// strip all tags except <br> and <a>
			ActivityResultData ard = competenceData.getActivityToShowWithDetails().getResultData();
			//ard.setResult(PostUtil.cleanHTMLTagsExceptBrA(ard.getResult()));
			
			activityManager.saveResponse(competenceData.getActivityToShowWithDetails().getTargetActivityId(), 
					ard.getResult(), postDate, ActivityResultType.TEXT, loggedUser.getUserContext());
			competenceData.getActivityToShowWithDetails().getResultData().setResultPostDate(postDate);
			
			completeActivity();
		} catch(Exception e) {
			logger.error(e);
			competenceData.getActivityToShowWithDetails().getResultData().setResult(null);
			PageUtil.fireErrorMessage("Error saving response");
		}
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		activityResultBean.uploadAssignment(event, 
				competenceData.getActivityToShowWithDetails().getResultData());
		
		completeActivity();
	}
	
	public void deleteAssignment() {
		try {
			activityManager.deleteAssignment(competenceData.getActivityToShowWithDetails()
					.getTargetActivityId(), loggedUser.getUserContext());
			competenceData.getActivityToShowWithDetails().getResultData().setAssignmentTitle(null);
			competenceData.getActivityToShowWithDetails().getResultData().setResult(null);
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
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

	public CommentsData getCommentsData() {
		return commentsData;
	}

	public void setCommentsData(CommentsData commentsData) {
		this.commentsData = commentsData;
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

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

	public boolean isMandatoryOrder() {
		return mandatoryOrder;
	}

	public void setMandatoryOrder(boolean mandatoryOrder) {
		this.mandatoryOrder = mandatoryOrder;
	}

	public ResourceAccessData getAccess() {
		return access;
	}

	public RubricCriteriaGradeData getRubricGradeData() {
		return rubricGradeData;
	}
}
