package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "credentialPreviewBean")
@Component("credentialPreviewBean")
@Scope("view")
public class CredentialPreviewBean implements Serializable {

	private static final long serialVersionUID = -5011729831412985935L;

	private static Logger logger = Logger.getLogger(CredentialPreviewBean.class);

	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private Activity1Manager activityManager;

	private String id;
	private long decodedId;

	private CredentialData credentialData;

	public void init() {
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			try {
				/*
				access mode Manager is passed because we don't want to load student data
				with progress
				 */
				credentialData = credentialManager
						.getCredentialData(decodedId, true, false, false, true, loggedUser.getUserId(), AccessMode.MANAGER);
			} catch (Exception e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}

	public void loadCompetenceActivitiesIfNotLoaded(CompetenceData1 cd) {
		if (!cd.isActivitiesInitialized()) {
			try {
				cd.setActivities(activityManager.getCompetenceActivitiesData(cd.getCompetenceId()));
				cd.setActivitiesInitialized(true);
			} catch (Exception e) {
				logger.error("error", e);
				PageUtil.fireErrorMessage("Error loading activities");
			}
		}
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

	public long getDecodedId() {
		return decodedId;
	}
}
