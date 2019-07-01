package org.prosolo.web.courses.competence;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.AccessDeniedException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialIdData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "competenceViewBeanManager")
@Component("competenceViewBeanManager")
@Scope("view")
public class CompetenceViewBeanManager implements Serializable {

	private static final long serialVersionUID = 1186517158327288554L;

	private static Logger logger = Logger.getLogger(CompetenceViewBeanManager.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credManager;
	@Inject private Competence1Manager competenceManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CommentBean commentBean;

	private String credId;
	private long decodedCredId;
	private String compId;
	private long decodedCompId;
	private String commentId;
	
	private CompetenceData1 competenceData;
	private ResourceAccessData access;
	private CommentsData commentsData;

	private CredentialIdData credentialIdData;

	public void init() {	
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);

		if (decodedCompId > 0 && decodedCredId > 0) {
			try {
				// check if credential and competency are connected
				competenceManager.checkIfCompetenceIsPartOfACredential(decodedCredId, decodedCompId);

				ResourceAccessRequirements req = ResourceAccessRequirements
						.of(AccessMode.MANAGER)
						.addPrivilege(UserGroupPrivilege.Edit)
						.addPrivilege(UserGroupPrivilege.Instruct);

				RestrictedAccessResult<CompetenceData1> result = competenceManager
						.getCompetenceDataWithAccessRightsInfo(decodedCompId, true, false, true, true,
								loggedUser.getUserId(), req, false);

				unpackResult(result);
				/*
				 * if user does not have at least access to resource in read only mode throw access denied exception.
				 */
				if (!access.isCanRead()) {
					throw new AccessDeniedException();
				}

				/*
				 * check if user has instructor privilege and if has, we should mark his comments as
				 * instructor comments
				 */
				commentsData = CommentsData
						.builder()
						.resourceType(CommentedResourceType.Competence)
						.resourceId(competenceData.getCompetenceId())
						.isInstructor(access.isCanInstruct())
						.isManagerComment(true)
						.commentId(idEncoder.decodeId(commentId))
						.credentialId(decodedCredId)
						.build();
				commentBean.loadComments(commentsData);

				credentialIdData = credManager.getCredentialIdData(decodedCredId, null);
				competenceData.setCredentialId(decodedCredId);
			} catch (AccessDeniedException ade) {
				PageUtil.accessDenied();
			} catch (ResourceNotFoundException rnfe) {
				PageUtil.notFound();
			} catch(Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage(e.getMessage());
			}
		} else {
			PageUtil.notFound();
		}
	}
	
	private void unpackResult(RestrictedAccessResult<CompetenceData1> res) {
		competenceData = res.getResource();
		access = res.getAccess();
	}
	
	public boolean isCurrentUserCreator() {
		return competenceData == null || competenceData.getCreator() == null ? false : 
			competenceData.getCreator().getId() == loggedUser.getUserId();
	}
	
	public boolean hasMoreActivities(int index) {
		return competenceData.getActivities().size() != index + 1;
	}
	
	public String getLabelForCompetence() {
 		if(!competenceData.isPublished() && 
 				access.isCanEdit()) {
 			return "(Unpublished)";
 		} else {
 			return "";
 		}
 	}

	/*
	 * ACTIONS
	 */
	
	
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

	public ResourceAccessData getAccess() {
		return access;
	}

	public String getCredentialTitle() {
		return credentialIdData.getTitle();
	}

	public CredentialIdData getCredentialIdData() {
		return credentialIdData;
	}
}
