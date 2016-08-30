package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.AnnouncementManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "announcementStudentBean")
@Component("announcementStudentBean")
@Scope("view")
public class AnnouncementStudentBean implements Serializable {

	private static final long serialVersionUID = 6993318770664557315L;
	private static Logger logger = Logger.getLogger(AnnouncementStudentBean.class);

	private String credentialId;
	private String announcementId;
	private String credentialTitle;
	private String credentialDurationString;
	private boolean credentialMandatoryFlow;
	private AnnouncementData announcementData;
	private CredentialData credentialData;
	
	@Inject
	private AnnouncementManager announcementManager;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private CredentialManager credManager;
	
	
	/**
	 * Invoked when user navigates to credential/{id}/announcements/{announcementsId} page
	 */
	public void markAnnouncementRead() {
		try {
			announcementManager.readAnnouncement(idEncoder.decodeId(announcementId), loggedUser.getUserId());
			announcementData = announcementManager.getAnnouncement(idEncoder.decodeId(announcementId));
			CredentialData basicCredentialData = credManager.getBasicCredentialData(idEncoder.decodeId(credentialId), loggedUser.getUserId());
			credentialTitle = basicCredentialData.getTitle();
			credentialMandatoryFlow = basicCredentialData.isMandatoryFlow();
			credentialDurationString = basicCredentialData.getDurationString();
		} catch (Exception e) {
			logger.error("Could not mark announcement as read", e);
		}
	}
	
	/** This method is called when rendering announcement info balloon on credentials page
	 *  In order to create links etc, some fields are initialized
	 * @param credentialId
	 * @return
	 */
	public boolean userDidNotReadLastAnnouncement(String credentialId) {
		try {
			Long lastAnnouncementSeenId = announcementManager
					.getLastAnnouncementIdIfNotSeen(idEncoder.decodeId(credentialId), loggedUser.getUserId());
			//we could not find SeenAnnouncement for last announcement for this credential - user did not see it, or there are no announcements
			if(lastAnnouncementSeenId == null) {
				announcementData = announcementManager.getLastAnnouncementForCredential(idEncoder.decodeId(credentialId));
				if(announcementData != null) {
					//initialize data so we can use it for components
					this.credentialId = credentialId;
					this.announcementId = announcementData.getEncodedId();
					return true;
				}
				else {
					//there are no announcements for this credential
					return false;
				}

			}
		} catch (Exception e) {
			logger.error("Error while fetching las announcement data",e);
		}
		return false;
	}
	
	/**
	 * Created so we can handle 'x' button closing (button with a span does not get rendered well, so we use JS)
	 */
	public void markAnnouncementReadFromAjax() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String id = params.get("announcementId");
		try {
			announcementManager.readAnnouncement(idEncoder.decodeId(id), loggedUser.getUserId());
			announcementData = announcementManager.getAnnouncement(idEncoder.decodeId(id));
		} catch (Exception e) {
			logger.error("Could not mark announcement as read", e);
		}
	}
	

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}

	public String getAnnouncementId() {
		return announcementId;
	}

	public void setAnnouncementId(String announcementId) {
		this.announcementId = announcementId;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public String getCredentialDurationString() {
		return credentialDurationString;
	}

	public void setCredentialDurationString(String credentialDurationString) {
		this.credentialDurationString = credentialDurationString;
	}

	public boolean isCredentialMandatoryFlow() {
		return credentialMandatoryFlow;
	}

	public void setCredentialMandatoryFlow(boolean credentialMandatoryFlow) {
		this.credentialMandatoryFlow = credentialMandatoryFlow;
	}


	public AnnouncementManager getAnnouncementManager() {
		return announcementManager;
	}


	public void setAnnouncementManager(AnnouncementManager announcementManager) {
		this.announcementManager = announcementManager;
	}


	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}


	public void setIdEncoder(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}


	public LoggedUserBean getLoggedUser() {
		return loggedUser;
	}


	public void setLoggedUser(LoggedUserBean loggedUser) {
		this.loggedUser = loggedUser;
	}


	public CredentialManager getCredManager() {
		return credManager;
	}


	public void setCredManager(CredentialManager credManager) {
		this.credManager = credManager;
	}


	public AnnouncementData getAnnouncementData() {
		return announcementData;
	}


	public void setAnnouncementData(AnnouncementData announcementData) {
		this.announcementData = announcementData;
	}


	public CredentialData getCredentialData() {
		return credentialData;
	}


	public void setCredentialData(CredentialData credentialData) {
		this.credentialData = credentialData;
	}

}
