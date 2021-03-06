package org.prosolo.web.courses.activity;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialIdData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.activity.util.ActivityUtil;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

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

	@Getter	@Setter	private String actId;
	@Getter	@Setter	private String compId;
	@Getter	@Setter	private String credId;
	@Getter	@Setter	private String commentId;

	private long decodedActId;
	@Getter private long decodedCompId;
	@Getter private long decodedCredId;

	@Getter private CompetenceData1 competenceData;
	@Getter private ResourceAccessData access;
	@Getter private CommentsData commentsData;

	@Getter private CredentialIdData credentialIdData;

	public void init() {	
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);

		if (decodedActId > 0 && decodedCompId > 0 && decodedCredId > 0) {
			try {
				ResourceAccessRequirements req = ResourceAccessRequirements
						.of(AccessMode.MANAGER)
						.addPrivilege(UserGroupPrivilege.Edit)
						.addPrivilege(UserGroupPrivilege.Instruct);
				access = compManager.getResourceAccessData(decodedCompId, loggedUser.getUserId(), req);
				
				/*
				 * if user does not have at least access to resource in read only mode show access denied page.
				 */
				if (!access.isCanRead()) {
					PageUtil.accessDenied();
				} else {
					// check if credential, competency and activity are mutually connected
					activityManager.checkIfActivityAndCompetenceArePartOfCredential(decodedCredId, decodedCompId, decodedActId);

					competenceData = activityManager.getCompetenceActivitiesWithSpecifiedActivityInFocus(
									decodedCredId, decodedCompId, decodedActId);
					commentsData = CommentsData
							.builder()
							.resourceType(CommentedResourceType.Activity)
							.resourceId(competenceData.getActivityToShowWithDetails().getActivityId())
							.isInstructor(access.isCanInstruct())
							.isManagerComment(true)
							.commentId(idEncoder.decodeId(commentId))
							.credentialId(decodedCredId)
							.build();

					commentBean.loadComments(commentsData);
					
					ActivityUtil.createTempFilesAndSetUrlsForCaptions(
							competenceData.getActivityToShowWithDetails().getCaptions(),
							loggedUser.getUserId());

					loadCompetenceAndCredentialTitle();
				}
			} catch(ResourceNotFoundException rnfe) {
				PageUtil.notFound();
			} catch(Exception e) {
				e.printStackTrace();
				logger.error(e);
				PageUtil.fireErrorMessage("Error loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}
	
	private void loadCompetenceAndCredentialTitle() {
		String compTitle = compManager.getCompetenceTitle(decodedCompId);
		competenceData.setTitle(compTitle);

		credentialIdData = credManager.getCredentialIdData(decodedCredId, null);
		competenceData.setCredentialId(decodedCredId);
		competenceData.setCredentialTitle(credentialIdData.getTitle());
	}

	public boolean isActivityActive(ActivityData act) {
		return decodedActId == act.getActivityId();
	}
	
	public boolean isCurrentUserCreator() {
		return competenceData.getActivityToShowWithDetails().getCreatorId() == loggedUser.getUserId();
	}
	
}
