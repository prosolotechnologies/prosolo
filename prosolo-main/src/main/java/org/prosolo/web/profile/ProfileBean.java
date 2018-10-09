package org.prosolo.web.profile;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.credential.CategorizedCredentialsData;
import org.prosolo.services.nodes.data.credential.TargetCredentialData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.services.nodes.data.competence.TargetCompetenceData;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.profile.data.UserSocialNetworksData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ManagedBean(name = "profileBean")
@Component("profileBean")
@Scope("view")
public class ProfileBean {
	
	private static Logger logger = Logger.getLogger(ProfileBean.class);
	
	@Inject
	private SocialNetworksManager socialNetworksManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private CredentialManager credentialManager;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private UserManager userManager;
	@Inject
	private Competence1Manager competenceManager;
	@Inject 
	private MessagingManager messagingManager;
	@Inject 
	private EventFactory eventFactory;
	@Inject
	private ThreadPoolTaskExecutor taskExecutor;
	
	private UserSocialNetworksData userSocialNetworksData;
	private List<CategorizedCredentialsData> targetCredential1List;
	private List<CategorizedCredentialsData> targetCredential1ListInProgress;
	private Map<String, String> nameMap = new HashMap<>();
	
	/* parameter that can be provided in the via UI*/
	private String studentId;
	private String message;
	private String tab;
	private long decodedStudentId;

	private boolean personalProfile;
	private UserData userData;

	private List<TargetCompetenceData> targetCompetence1List;

	private static final String COMPETENCES_TAB = "competences";
	private static final String CREDENTIALS_IN_PROGRESS_TAB = "inprogress";

	public void init() {
		decodedStudentId = idEncoder.decodeId(studentId);
		if (StringUtils.isNotBlank(studentId) &&
                loggedUserBean.isInitialized() &&
                decodedStudentId != loggedUserBean.getUserId()) {
			personalProfile = false;
			userData = userManager.getUserData(decodedStudentId);
        } else {
			personalProfile = true;
			userData = userManager.getUserData(loggedUserBean.getUserId());
		}
		initializeSocialNetworkData(userData.getId());
		if (COMPETENCES_TAB.equals(tab)) {
			initializeTargetCompetenceData(userData);
		} else if (CREDENTIALS_IN_PROGRESS_TAB.equals(tab)) {
			initializeInProgressCredentials(userData);
		} else {
			initializeTargetCredentialData(userData);
		}
		initializeSocialNetworkNameMap();

	}
	
	public void sendMessage() {
		if (StringUtils.isNotBlank(studentId)) {
			if ( decodedStudentId != loggedUserBean.getUserId()) {
				try {
					MessageData messageData = messagingManager.sendMessage(0, loggedUserBean.getUserId(), decodedStudentId, this.message, loggedUserBean.getUserContext());
					logger.debug("User "+loggedUserBean.getUserId()+" sent a message to "+decodedStudentId+" with content: '"+messageData+"'");
					
					List<UserData> participants = new ArrayList<UserData>();
					
					participants.add(new UserData(loggedUserBean.getUserId(), loggedUserBean.getFullName()));
					
					final Message message1 = new Message();
					message1.setId(messageData.getId());

					UserContextData userContext = loggedUserBean.getUserContext();

					this.message = "";
					
					PageUtil.fireSuccessfulInfoMessage("profileGrowl", "Your message is sent");
				} catch (Exception e) {
					logger.error(e);
				}
			}
			else {
				PageUtil.fireErrorMessage("Can not send a message to yourself");
				logger.error("Error while sending message from profile page, studentId was the same as logged student id : "+loggedUserBean.getUserId());
			}
		}
		else {
			PageUtil.fireErrorMessage("Canno't send message, student unknown!");
			logger.error("Error while sending message from profile page, studentId was blank");
		}
	}
	
	public void changeTab(String tab) {
		try {
			initializeData(tab);
		} catch (ResourceCouldNotBeLoadedException e) {
			PageUtil.fireErrorMessage(String.format("Cannot initialize data for profile tab : %s"),tab);
			logger.error("Error initializing data",e);
		}
	}


	private void initializeData(String activeTab) throws ResourceCouldNotBeLoadedException {
		//student is already initialized in init() method
		if(activeTab.contains("credentials")) {
			initializeTargetCredentialData(userData);
		}
		else if(activeTab.contains("competences")) {
			initializeTargetCompetenceData(userData);
		}
		else if(activeTab.contains("inprogress")) {
			initializeInProgressCredentials(userData);
		}
		
	}

	private void initializeTargetCredentialData(UserData student) {
		//if student is viewing his personal profile he should see all his credentials including those that should not be publicly displayed
		targetCredential1List = credentialManager.getAllCompletedCredentials(
				student.getId(), 
				!isPersonalProfile());
	}
	
	private void initializeTargetCompetenceData(UserData student) {
		try {
			//if student is viewing his personal profile he should see all his credentials including those that should not be publicly displayed
			targetCompetence1List= competenceManager.getAllCompletedCompetences(
					student.getId(),
					!isPersonalProfile());
		} catch (Exception e) {
			PageUtil.fireErrorMessage("Competence data could not be loaded!");
			logger.error("Error while loading target credentials with progress == 100 Error:\n" + e, e);
		}
	}
	
	private void initializeInProgressCredentials(UserData student) {
		try {
			//if student is viewing his personal profile he should see all his credentials including those that should not be publicly displayed
			targetCredential1ListInProgress = credentialManager.getAllInProgressCredentials(
					student.getId(),
					!isPersonalProfile());
		} catch (Exception e) {
			PageUtil.fireErrorMessage("Credential data could not be loaded!");
			logger.error("Error while loading target credentials with progress == 100 Error:\n" + e);
		}
	}

	private void initializeSocialNetworkData(long id) {
		try {
			userSocialNetworksData = socialNetworksManager.getUserSocialNetworkData(id);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}


	private void initializeSocialNetworkNameMap() {
		nameMap.put(SocialNetworkName.BLOG.toString(), "website");
		nameMap.put(SocialNetworkName.FACEBOOK.toString(), "facebook");
		nameMap.put(SocialNetworkName.GPLUS.toString(), "gplus");
		nameMap.put(SocialNetworkName.LINKEDIN.toString(), "linkedIn");
		nameMap.put(SocialNetworkName.TWITTER.toString(), "twitter");
	}

	public void updateTargetCredentialProfileVisibility(TargetCredentialData cred) {
		String hiddenOrShown = cred.isHiddenFromProfile() ? "hidden from" : "displayed in";

		try {
			credentialManager.updateHiddenTargetCredentialFromProfile(cred.getId(), cred.isHiddenFromProfile());
			PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " will be " + hiddenOrShown + " your profile");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error updating " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " visibility");
			logger.error("Error while updating credential visibility in a profile!\n" + e);
		}
	}

	public void updateTargetCompetenceProfileVisibility(TargetCompetenceData comp) {
		String hiddenOrShown = comp.isHiddenFromProfile() ? "hidden from" : "displayed in";

		try {
			competenceManager.updateHiddenTargetCompetenceFromProfile(comp.getId(), comp.isHiddenFromProfile());
			PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.competence").toLowerCase() + " will be " + hiddenOrShown + " your profile");
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error updating " + ResourceBundleUtil.getMessage("label.competence").toLowerCase() + " visibility");
			logger.error("Error while updating competency visibility in a profile!\n" + e);
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public String getAlternativeName(SocialNetworkName name) {
		return nameMap.get(name.toString());
	}

	public UserSocialNetworksData getUserSocialNetworksData() {
		return userSocialNetworksData;
	}

	public String getStudentId() {
		return studentId;
	}

	public UserData getUserData() {
		return userData;
	}

	public boolean isPersonalProfile() {
		return personalProfile;
	}

	public String getMessage() {
		return message;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getDecodedStudentId() {
		return decodedStudentId;
	}

	public void setDecodedStudentId(long decodedStudentId) {
		this.decodedStudentId = decodedStudentId;
	}

	public List<TargetCompetenceData> getTargetCompetence1List() {
		return targetCompetence1List;
	}

	public void setTargetCompetence1List(List<TargetCompetenceData> targetCompetence1List) {
		this.targetCompetence1List = targetCompetence1List;
	}

	public List<CategorizedCredentialsData> getTargetCredential1List() {
		return targetCredential1List;
	}

	public List<CategorizedCredentialsData> getTargetCredential1ListInProgress() {
		return targetCredential1ListInProgress;
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}
}
