package org.prosolo.web.courses.activity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.RoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.activity.util.ActivityUtil;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

	private String actId;
	private long decodedActId;
	private String compId;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;
	private String mode;
	private String commentId;
	
	private CompetenceData1 competenceData;
	private CommentsData commentsData;

	private long nextCompToLearn;
	
	private String roles="Learner";
	
	/*public String getRoles() {
		return roles;
	}*/
	public String getRoles(){
		System.out.println("GET ROLES IN ACTIVITY VIEW BEAN USER...");
		String selectedRole=loggedUser.getSessionData().getSelectedRole();
		System.out.println("Selected role for LTI:"+selectedRole);
		String role="Learner";
		if(selectedRole!=null){
			if( selectedRole.equalsIgnoreCase("manager")){
				role= "Instructor";
			}else if(selectedRole.equalsIgnoreCase("admin")){
				role= "Administrator";
			}
		}

		return role;
		//return this.roles;
	}

	private long nextActivityToLearn;
	private boolean mandatoryOrder;

	public void init() {
		List<String> roles = new ArrayList<>();
		roles.add(RoleNames.MANAGER);
		roles.add(RoleNames.INSTRUCTOR);
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
			
				UserGroupPrivilege priv = null;
				if ("preview".equals(mode)) {
					priv = UserGroupPrivilege.Edit;
				} else {
					priv = UserGroupPrivilege.View;
				}
				if (decodedCredId > 0) {
					competenceData = activityManager
							.getFullTargetActivityOrActivityData(decodedCredId,
									decodedCompId, decodedActId, loggedUser.getUserId(), priv, false);
				} else { 
					competenceData = activityManager
							.getCompetenceActivitiesWithSpecifiedActivityInFocus(
									0, decodedCompId, decodedActId,  loggedUser.getUserId(), priv);
				}
				if (competenceData == null || competenceData.getActivityToShowWithDetails() == null) {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch(
								"/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				} else if(!competenceData.getActivityToShowWithDetails().isCanAccess()){
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch(
								"/accessDenied.xhtml");
					} catch (IOException e) {
						e.printStackTrace();
						logger.error(e);
					}
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
//					commentBean.init(CommentedResourceType.Activity, 
//							competenceData.getActivityToShowWithDetails().getActivityId(), false);
					
					loadCompetenceAndCredentialTitle();
					
					ActivityUtil.createTempFilesAndSetUrlsForCaptions(ad.getCaptions(), loggedUser.getUserId());
				}
			} catch(ResourceNotFoundException rnfe) {
				try {
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
				} catch (IOException e) {
					logger.error(e);
				}
			} catch(Exception e) {
				e.printStackTrace();
				logger.error(e);
				PageUtil.fireErrorMessage("Error while loading activity");
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

	private void loadCompetenceAndCredentialTitle() {
		String compTitle = null;
		String credTitle = null;
		decodedCredId = idEncoder.decodeId(credId);
		if(competenceData.getActivityToShowWithDetails().isEnrolled()) {
			compTitle = compManager.getTargetCompetenceTitle(competenceData
					.getActivityToShowWithDetails().getCompetenceId());
			if(decodedCredId > 0) {
//				credTitle = credManager.getTargetCredentialTitle(decodedCredId, loggedUser
//						.getUser().getId());
					CredentialData cd = credManager
							.getTargetCredentialTitleAndLearningOrderInfo(decodedCredId, 
									loggedUser.getUserId());
					credTitle = cd.getTitle();
					nextCompToLearn = cd.getNextCompetenceToLearnId();
					nextActivityToLearn = cd.getNextActivityToLearnId();
					mandatoryOrder = cd.isMandatoryFlow();
			}
			if(!mandatoryOrder) {
				for (ActivityData ad : competenceData.getActivities()) {
					if(!ad.isCompleted()) {
						nextCompToLearn = decodedCompId;
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
	
	public void enrollInCredential() {
		try {
			LearningContextData lcd = new LearningContextData();
			lcd.setPage(FacesContext.getCurrentInstance().getViewRoot().getViewId());
			lcd.setLearningContext(PageUtil.getPostParameter("context"));
			lcd.setService(PageUtil.getPostParameter("service"));
			credManager.enrollInCredential(decodedCredId, 
					loggedUser.getUserId(), lcd);
			initializeActivityData();
			
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect("/credentials/" + 
					credId + "/" + compId + "/" + actId);
			} catch (IOException e) {
				logger.error(e);
				e.printStackTrace();
			}
		} catch(DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
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
	
	public String getLabelForActivity() {
 		if(isPreview()) {
 			return "(Preview)";
 		} else if(isCurrentUserCreator() && !competenceData.getActivityToShowWithDetails().isEnrolled() 
 				&& !competenceData.getActivityToShowWithDetails().isPublished()) {
 			return "(Unpublished)";
 		} else {
 			return "";
 		}
 	}
	
	public boolean isPreview() {
		return "preview".equals(mode);
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
					decodedCredId, 
					loggedUser.getUserId(), lcd);
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
				if(!localNextToLearn) {
					CredentialData cd = credManager.getTargetCredentialNextCompAndActivityToLearn(
							decodedCredId, loggedUser.getUserId());
					nextCompToLearn = cd.getNextCompetenceToLearnId();
					nextActivityToLearn = cd.getNextActivityToLearnId();
				}
			} catch(DbConnectionException e) {
				logger.error(e);
			}
			
			PageUtil.fireSuccessfulInfoMessage("Activity Completed");
		} catch (Exception e) {
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("Error while marking activity as completed");
		}
	}
	
	public void saveTextResponse() {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			Date postDate = new Date();
			
			// strip all tags except <br> and <a>
			ActivityResultData ard = competenceData.getActivityToShowWithDetails().getResultData();
			//ard.setResult(PostUtil.cleanHTMLTagsExceptBrA(ard.getResult()));
			
			activityManager.saveResponse(competenceData.getActivityToShowWithDetails().getTargetActivityId(), 
					ard.getResult(), 
					postDate, loggedUser.getUserId(), ActivityResultType.TEXT, 
					new LearningContextData(page, lContext, service));
			competenceData.getActivityToShowWithDetails().getResultData().setResultPostDate(postDate);
			
			completeActivity();
		} catch(Exception e) {
			logger.error(e);
			competenceData.getActivityToShowWithDetails().getResultData().setResult(null);
			PageUtil.fireErrorMessage("Error while saving response");
		}
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		activityResultBean.uploadAssignment(event, 
				competenceData.getActivityToShowWithDetails().getResultData());
		
		completeActivity();
	}
	
	public void deleteAssignment() {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			activityManager.deleteAssignment(competenceData.getActivityToShowWithDetails()
					.getTargetActivityId(), loggedUser.getUserId(), 
					new LearningContextData(page, lContext, service));
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

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
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
	
}
