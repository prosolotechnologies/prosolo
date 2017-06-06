package org.prosolo.web.courses.activity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
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
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
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
	private String commentId;
	
	private CompetenceData1 competenceData;
	private ResourceAccessData access;
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
			
				RestrictedAccessResult<CompetenceData1> res = activityManager
						.getFullTargetActivityOrActivityData(decodedCredId,
								decodedCompId, decodedActId, loggedUser.getUserId(), false);
				
				unpackResult(res);
				
				//if user is enrolled he can always access the resource
				if (!access.isCanAccess()) {
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
					
					loadCompetenceAndCredentialTitleAndNextToLearnInfo();
					
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

	private void unpackResult(RestrictedAccessResult<CompetenceData1> res) {
		competenceData = res.getResource();
		access = res.getAccess();
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
			LearningContextData lcd = new LearningContextData();
			lcd.setPage(FacesContext.getCurrentInstance().getViewRoot().getViewId());
			lcd.setLearningContext(PageUtil.getPostParameter("context"));
			lcd.setService(PageUtil.getPostParameter("service"));
			compManager.enrollInCompetence(decodedCompId, loggedUser.getUserId(), lcd);
			//initializeActivityData();
			
			try {
				ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
				if(decodedCredId > 0) {
					extContext.redirect(extContext.getRequestContextPath() + "/credentials/" + 
						credId + "/" + compId + "/" + actId);
				} else {
					FacesContext.getCurrentInstance().getExternalContext().redirect(
							extContext.getRequestContextPath() +
							"/competences/" + compId + "/" + actId);
				}
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
// 		if(isPreview()) {
// 			return "(Preview)";
// 		} else if(isCurrentUserCreator() && !competenceData.getActivityToShowWithDetails().isEnrolled() 
// 				&& !competenceData.getActivityToShowWithDetails().isPublished()) {
// 			return "(Unpublished)";
// 		} else {
// 			return "";
// 		}
		return null;
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
	
}
