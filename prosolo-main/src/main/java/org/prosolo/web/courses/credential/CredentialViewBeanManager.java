package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "credentialViewBeanManager")
@Component("credentialViewBeanManager")
@Scope("view")
public class CredentialViewBeanManager implements Serializable {

	private static final long serialVersionUID = -8080252106493765017L;

	private static Logger logger = Logger.getLogger(CredentialViewBeanManager.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;

	private String id;
	private long decodedId;
	
	private CredentialData credentialData;
	private ResourceAccessData access;

	public void init() {	
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			try {
				access = credentialManager.getResourceAccessData(decodedId, loggedUser.getUserId(),
						ResourceAccessRequirements.of(AccessMode.MANAGER)
								.addPrivilege(UserGroupPrivilege.Edit)
								.addPrivilege(UserGroupPrivilege.Instruct));
				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					credentialData = credentialManager
							.getCredentialData(decodedId, true, false, false, true, loggedUser.getUserId(), AccessMode.MANAGER);
				}
			} catch (ResourceNotFoundException rnfe) {
				PageUtil.notFound();
			} catch (Exception e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error trying to retrieve credential data");
			}
		} else {
			PageUtil.notFound();
		}
	}
	
//	public boolean isCurrentUserCreator() {
//		return credentialData == null || credentialData.getCreator() == null ? false : 
//			credentialData.getCreator().getId() == loggedUser.getUserId();
//	}
	
	/*
	 * ACTIONS
	 */
	
	public void loadCompetenceActivitiesIfNotLoaded(CompetenceData1 cd) {
		if(!cd.isActivitiesInitialized()) {
			List<ActivityData> activities = activityManager
					.getCompetenceActivitiesData(cd.getCompetenceId());
			cd.setActivities(activities);
			cd.setActivitiesInitialized(true);
		}
	}
	
	public boolean hasMoreCompetences(int index) {
		return credentialData.getCompetences().size() != index + 1;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CredentialData getCredentialData() {
		return credentialData;
	}

	public void setCredentialData(CredentialData credentialData) {
		this.credentialData = credentialData;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public ResourceAccessData getAccess() {
		return access;
	}

}
