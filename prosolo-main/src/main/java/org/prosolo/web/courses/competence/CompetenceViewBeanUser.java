package org.prosolo.web.courses.competence;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.AccessDeniedException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.LearningInfo;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "competenceViewBean")
@Component("competenceViewBean")
@Scope("view")
public class CompetenceViewBeanUser implements Serializable {

	private static final long serialVersionUID = 9208762722353804216L;

	private static Logger logger = Logger.getLogger(CompetenceViewBeanUser.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Competence1Manager competenceManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CommentBean commentBean;
	@Inject private CredentialManager credManager;

	private String credId;
	private long decodedCredId;
	private String compId;
	private long decodedCompId;
	private String commentId;
	
	private CompetenceData1 competenceData;
	private ResourceAccessData access;
	private CommentsData commentsData;

	private long nextCompToLearn;
	private boolean mandatoryOrder;

	public void init() {	
		decodedCompId = idEncoder.decodeId(compId);
		if (decodedCompId > 0) {
			try {
				decodedCredId = idEncoder.decodeId(credId);
				
				RestrictedAccessResult<CompetenceData1> res = competenceManager
						.getFullTargetCompetenceOrCompetenceData(decodedCredId, decodedCompId, 
								loggedUser.getUserId());
				unpackResult(res);
				
				/*
				 * if user does not have permission to access this competence and it is published, he should
				 * be able to see it in read only mode, but if it isn't published he should not be able to access
				 * it at all.
				 */
				if (!access.isCanAccess() && !competenceData.isPublished()) {
					throw new AccessDeniedException();
				}
				
				commentsData = new CommentsData(CommentedResourceType.Competence, 
						competenceData.getCompetenceId(), false, false);
				commentsData.setCommentId(idEncoder.decodeId(commentId));
				commentBean.loadComments(commentsData);
				
				if(decodedCredId > 0) {
					String credTitle = null;
					if(competenceData.isEnrolled()) {
						LearningInfo li = credManager.getCredentialLearningInfo(decodedCredId, 
								loggedUser.getUserId(), false);
						credTitle = li.getCredentialTitle();
						//TODO cred-redesign-07 what to do with mandatory order now when competence is independent resource
						//nextCompToLearn = li.getNextCompetenceToLearn();
						//mandatoryOrder = li.isMandatoryFlow();
					} else {
						credTitle = credManager.getCredentialTitle(decodedCredId);
					}
					competenceData.setCredentialId(decodedCredId);
					competenceData.setCredentialTitle(credTitle);
				}
			} catch (AccessDeniedException ade) {
				try {
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/accessDenied.xhtml");
				} catch (IOException e) {
					logger.error(e);
				}
			} catch (ResourceNotFoundException rnfe) {
				try {
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
				} catch (IOException e) {
					logger.error(e);
				}
			} catch (Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage(e.getMessage());
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
	
	public boolean isCompetenceNextToLearn() {
		return decodedCompId == nextCompToLearn;
	}
	
	public boolean isCurrentUserCreator() {
		return competenceData == null || competenceData.getCreator() == null ? false : 
			competenceData.getCreator().getId() == loggedUser.getUserId();
	}
	
	public boolean hasMoreActivities(int index) {
		return competenceData.getActivities().size() != index + 1;
	}
	
	public String getLabelForCompetence() {
 		if(access.isCanEdit() && !competenceData.isEnrolled() && !competenceData.isPublished()) {
 			return "(Unpublished)";
 		} else {
 			return "";
 		}
 	}

	/*
	 * ACTIONS
	 */
	
	public void enrollInCompetence() {
		try {
			LearningContextData context = PageUtil.extractLearningContextData();
			
			competenceData = competenceManager.enrollInCompetenceAndGetCompetenceData(
					competenceData.getCompetenceId(), loggedUser.getUserId(), context);
			access.userEnrolled();
			PageUtil.fireSuccessfulInfoMessage("Successfully enrolled in a competency");
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while enrolling in a competency");
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public CompetenceData1 getCompetenceData() {
		return competenceData;
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

	public void setCompetenceData(CompetenceData1 competenceData) {
		this.competenceData = competenceData;
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
