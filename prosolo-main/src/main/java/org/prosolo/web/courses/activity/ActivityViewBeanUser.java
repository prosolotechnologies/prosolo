package org.prosolo.web.courses.activity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.RoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.PageUtil;
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
	@Inject private UploadManager uploadManager;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialManager credManager;
	@Inject private RoleManager roleManager;

	private String actId;
	private long decodedActId;
	private String compId;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;
	private String mode;
	
	private CompetenceData1 competenceData;
	private CommentsData commentsData;

	private long nextCompToLearn;
	private String roles="Learner";
	
	public String getRoles() {
		return roles;
	}

	public void init() {
		List<String> roles = new ArrayList<>();
		roles.add(RoleNames.MANAGER);
		roles.add(RoleNames.INSTRUCTOR);
		boolean hasManagerOrInstructorRole = roleManager.hasAnyRole(loggedUser.getUserId(), roles);
		if (hasManagerOrInstructorRole) {
			this.roles="Instructor";
		}else{
			this.roles="Learner";
		}
		
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		if (decodedActId > 0 && decodedCompId > 0) {
			try {
				decodedCredId = idEncoder.decodeId(credId);
				boolean shouldReturnDraft = false;
				if("preview".equals(mode)) {
					shouldReturnDraft = true;
				}
				if(decodedCredId > 0) {
					competenceData = activityManager
							.getFullTargetActivityOrActivityData(decodedCredId,
									decodedCompId, decodedActId, loggedUser.getUserId(), shouldReturnDraft);
				} else { 
					competenceData = activityManager
							.getCompetenceActivitiesWithSpecifiedActivityInFocusForUser(
									0, decodedCompId, decodedActId,  loggedUser.getUserId(), 
									shouldReturnDraft);
				}
				if(competenceData == null) {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				} else {
					commentsData = new CommentsData(CommentedResourceType.Activity, 
							competenceData.getActivityToShowWithDetails().getActivityId(), false);
					commentBean.loadComments(commentsData);
//					commentBean.init(CommentedResourceType.Activity, 
//							competenceData.getActivityToShowWithDetails().getActivityId(), false);
					
					loadCompetenceAndCredentialTitle();
				}
			} catch(Exception e) {
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
						.getTargetCredentialTitleAndNextCompToLearn(decodedCredId, 
								loggedUser.getUserId());
				credTitle = cd.getTitle();
				nextCompToLearn = cd.getNextCompetenceToLearnId();
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
 			return "(Draft)";
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
			
			for (ActivityData ad : competenceData.getActivities()) {
				if (ad.getActivityId() == competenceData.getActivityToShowWithDetails().getActivityId()) {
					ad.setCompleted(true);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("Error while marking activity as completed");
		}
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		String page = (String) event.getComponent().getAttributes().get("page");
		String lContext = (String) event.getComponent().getAttributes().get("learningContext");
		String service = (String) event.getComponent().getAttributes().get("service");
		try {
			String fileName = uploadedFile.getFileName();
			String fullPath = uploadManager.storeFile(uploadedFile, fileName);
			activityManager.saveAssignment(competenceData.getActivityToShowWithDetails()
					.getTargetActivityId(), fileName, fullPath, loggedUser.getUserId(),
					new LearningContextData(page, lContext, service));
			competenceData.getActivityToShowWithDetails().setAssignmentTitle(fileName);
			competenceData.getActivityToShowWithDetails().setAssignmentLink(fullPath);
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while uploading assignment");
		}
	}
	
	public void deleteAssignment() {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			activityManager.deleteAssignment(competenceData.getActivityToShowWithDetails()
					.getTargetActivityId(), loggedUser.getUserId(), 
					new LearningContextData(page, lContext, service));
			competenceData.getActivityToShowWithDetails().setAssignmentTitle(null);
			competenceData.getActivityToShowWithDetails().setAssignmentLink(null);
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
	
}
