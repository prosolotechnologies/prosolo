package org.prosolo.web.courses.activity;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.activity.util.ActivityUtil;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "activityViewBeanManager")
@Component("activityViewBeanManager")
@Scope("view")
public class ActivityViewBeanManager implements Serializable {

	private static final long serialVersionUID = -4768101723720055132L;

	private static Logger logger = Logger.getLogger(ActivityViewBeanManager.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CommentBean commentBean;
	@Inject private CredentialManager credManager;
	@Inject private Competence1Manager compManager;

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

	public void init() {	
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		if (decodedActId > 0 && decodedCompId > 0) {
			if(credId != null) {
				decodedCredId = idEncoder.decodeId(credId);
			}
			try {
				UserGroupPrivilege priv = null;
				if("preview".equals(mode)) {
					priv = UserGroupPrivilege.Edit;
				} else {
					priv = UserGroupPrivilege.View;
				}
				competenceData = activityManager
						.getCompetenceActivitiesWithSpecifiedActivityInFocus(
								decodedCredId, decodedCompId, decodedActId, loggedUser.getUserId(), priv);
				
				if(competenceData == null || competenceData.getActivityToShowWithDetails() == null) {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				} else {
					if(!competenceData.getActivityToShowWithDetails().isCanAccess()) {
						try {
							FacesContext.getCurrentInstance().getExternalContext().dispatch("/accessDenied.xhtml");
						} catch (IOException e) {
							logger.error(e);
						}
					} else {
						/*
						 * check if user has instructor capability and if has, we should mark his comments as
						 * instructor comments
						 */
						boolean hasInstructorCapability = loggedUser.hasCapability("BASIC.INSTRUCTOR.ACCESS");
						commentsData = new CommentsData(CommentedResourceType.Activity, 
								competenceData.getActivityToShowWithDetails().getActivityId(), 
								hasInstructorCapability);
						commentsData.setCommentId(idEncoder.decodeId(commentId));
						commentBean.loadComments(commentsData);
		//					commentBean.init(CommentedResourceType.Activity, 
		//							competenceData.getActivityToShowWithDetails().getActivityId(), 
		//							hasInstructorCapability);
						
						ActivityUtil.createTempFilesAndSetUrlsForCaptions(
								competenceData.getActivityToShowWithDetails().getCaptions(), 
								loggedUser.getUserId());
						
						loadCompetenceAndCredentialTitle();
					}
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
		String compTitle = compManager.getCompetenceTitle(decodedCompId);
		competenceData.setTitle(compTitle);
		if(decodedCredId > 0) {
			String credTitle = credManager.getCredentialTitle(decodedCredId);
			competenceData.setCredentialId(decodedCredId);
			competenceData.setCredentialTitle(credTitle);
		}
		
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
 		} else if(!competenceData.getActivityToShowWithDetails().isPublished() && 
 				competenceData.getActivityToShowWithDetails().getType() 
 					== LearningResourceType.UNIVERSITY_CREATED) {
 			return "(Draft)";
 		} else {
 			return "";
 		}
 	}
	
	public boolean isPreview() {
		return "preview".equals(mode);
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

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

}
