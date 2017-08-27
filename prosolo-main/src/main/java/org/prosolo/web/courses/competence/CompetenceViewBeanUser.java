package org.prosolo.web.courses.competence;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.AccessDeniedException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;

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
	private boolean justEnrolled;
	
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
				 * if user does not have at least access to resource in read only mode throw access denied exception.
				 */
				if (!access.isCanRead()) {
					throw new AccessDeniedException();
				}
				
				commentsData = new CommentsData(CommentedResourceType.Competence, 
						competenceData.getCompetenceId(), false, false);
				commentsData.setCommentId(idEncoder.decodeId(commentId));
				commentBean.loadComments(commentsData);
				
				if(decodedCredId > 0) {
					String credTitle = credManager.getCredentialTitle(decodedCredId);
//					if(competenceData.isEnrolled()) {
////						LearningInfo li = credManager.getCredentialLearningInfo(decodedCredId, 
////								loggedUser.getUserId(), false);
//						//credTitle = li.getCredentialTitle();
//						//TODO cred-redesign-07 what to do with mandatory order now when competence is independent resource
//						//nextCompToLearn = li.getNextCompetenceToLearn();
//						//mandatoryOrder = li.isMandatoryFlow();
//					} else {
//						credTitle = credManager.getCredentialTitle(decodedCredId);
//					}
					competenceData.setCredentialId(decodedCredId);
					competenceData.setCredentialTitle(credTitle);
				}
				if (justEnrolled) {
					PageUtil.fireSuccessfulInfoMessage(
							"You have started the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase() + " " + competenceData.getTitle());
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
			competenceData = competenceManager.enrollInCompetenceAndGetCompetenceData(
					competenceData.getCompetenceId(), loggedUser.getUserId(), loggedUser.getUserContext());
			access.userEnrolled();
			PageUtil.fireSuccessfulInfoMessage("You have started the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error starting the " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
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

	public boolean isJustEnrolled() {
		return justEnrolled;
	}

	public void setJustEnrolled(boolean justEnrolled) {
		this.justEnrolled = justEnrolled;
	}


}
