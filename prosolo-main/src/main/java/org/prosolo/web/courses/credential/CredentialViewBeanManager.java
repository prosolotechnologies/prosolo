package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialViewBeanManager")
@Component("credentialViewBeanManager")
@Scope("view")
public class CredentialViewBeanManager implements Serializable {

	private static final long serialVersionUID = -1885207938203658902L;

	private static Logger logger = Logger.getLogger(CredentialViewBeanManager.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;

	private String id;
	private long decodedId;
	private String mode;
	
	private CredentialData credentialData;

	public void init() {	
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			try {
				if("preview".equals(mode)) {
					credentialData = credentialManager
							.getCurrentVersionOfCredentialForManager(decodedId, true, true);
				} else {
					credentialData = credentialManager
							.getCredentialData(decodedId, true, true);
				}
				
				if(credentialData == null) {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				}
			} catch(Exception e) {
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
	
	public boolean isCurrentUserCreator() {
		return credentialData == null || credentialData.getCreator() == null ? false : 
			credentialData.getCreator().getId() == loggedUser.getUser().getId();
	}
	
	public String getLabelForCredential() {
 		if(isPreview()) {
 			return "(Preview)";
 		} else if(!credentialData.isPublished() && 
 				credentialData.getType() == LearningResourceType.UNIVERSITY_CREATED) {
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

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}