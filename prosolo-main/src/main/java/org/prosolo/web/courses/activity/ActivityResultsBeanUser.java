package org.prosolo.web.courses.activity;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "activityResultsBeanUser")
@Component("activityResultsBeanUser")
@Scope("view")
public class ActivityResultsBeanUser implements Serializable {

	private static final long serialVersionUID = -449306144620746707L;

	private static Logger logger = Logger.getLogger(ActivityResultsBeanUser.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CommentBean commentBean;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialManager credManager;
	@Inject private CommentManager commentManager;
	@Inject private ActivityResultBean activityResultBean;

	private String actId;
	private long decodedActId;
	private String compId;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;
	
	private CompetenceData1 competenceData;

	private long nextCompToLearn;
	private long nextActivityToLearn;

	public void init() {
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);
		
		if (decodedActId > 0 && decodedCompId > 0 && decodedCredId > 0) {
			try {
				competenceData = activityManager
						.getTargetCompetenceActivitiesWithResultsForSpecifiedActivity(
								decodedCredId, decodedCompId, decodedActId, loggedUser.getUserId());
				if (competenceData == null) {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				} else {
					//load result comments number
					ActivityData ad = competenceData.getActivityToShowWithDetails();
					
					if (!ad.isStudentCanSeeOtherResponses()) {
						try {
							FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
						} catch (IOException ioe) {
							ioe.printStackTrace();
							logger.error(ioe);
						}
					}
					
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
	
	private void loadCompetenceAndCredentialTitle() {
		decodedCredId = idEncoder.decodeId(credId);
		competenceData.setTitle(compManager.getTargetCompetenceTitle(competenceData
				.getActivityToShowWithDetails().getCompetenceId()));
		if(decodedCredId > 0) {
			CredentialData cd = credManager
					.getTargetCredentialTitleAndNextCompAndActivityToLearn(decodedCredId, 
							loggedUser.getUserId());
			competenceData.setCredentialTitle(cd.getTitle());
			nextCompToLearn = cd.getNextCompetenceToLearnId();
			nextActivityToLearn = cd.getNextActivityToLearnId();
		}
		competenceData.setCredentialId(decodedCredId);
	}
	
	public void initializeResultCommentsIfNotInitialized(ActivityData activity) {
		try {
			CommentsData cd = activity.getResultData().getResultComments();
			if(!cd.isInitialized()) {
				cd.setInstructor(false);
				commentBean.loadComments(cd);
			}
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
			
			try {
				CredentialData cd = credManager.getTargetCredentialNextCompAndActivityToLearn(
						decodedCredId, loggedUser.getUserId());
				nextCompToLearn = cd.getNextCompetenceToLearnId();
				nextActivityToLearn = cd.getNextActivityToLearnId();
			} catch(DbConnectionException e) {
				logger.error(e);
			}
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
	
}