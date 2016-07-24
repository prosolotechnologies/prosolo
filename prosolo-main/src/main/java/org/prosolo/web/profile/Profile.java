package org.prosolo.web.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.achievements.data.CompetenceAchievementsData;
import org.prosolo.web.achievements.data.CredentialAchievementsData;
import org.prosolo.web.datatopagemappers.CompetenceAchievementsDataToPageMapper;
import org.prosolo.web.datatopagemappers.CredentialAchievementsDataToPageMapper;
import org.prosolo.web.datatopagemappers.SocialNetworksDataToPageMapper;
import org.prosolo.web.portfolio.data.SocialNetworksData;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "profile")
@Component("profile")
@Scope("view")
public class Profile {
	
	private static Logger logger = Logger.getLogger(Profile.class);
	
	@Autowired
	private SocialNetworksManager socialNetworksManager;
	/* used only to get id if the studentId parameter is not present */
	@Autowired
	private LoggedUserBean loggedUserBean;
	@Autowired
	private CredentialManager credentialManager;
	@Autowired
	private UrlIdEncoder idEncoder;
	@Autowired
	private UserManager userManager;
	@Autowired
	private Competence1Manager competenceManager;
	@Autowired 
	private MessagingManager messagingManager;
	@Autowired 
	private EventFactory eventFactory;
	@Autowired 
	@Qualifier("taskExecutor") 
	private ThreadPoolTaskExecutor taskExecutor;
	
	private SocialNetworksData socialNetworksData;
	private CredentialAchievementsData credentialAchievementsData;
	private CompetenceAchievementsData competenceAchievementsData;
//	private CompetenceAchievementsData unfinishedCompetenceAchievementsData;
	private CredentialAchievementsData inProgressCredentialAchievementsData;
	private Map<String, String> nameMap = new HashMap<>();
	/* parameter that can be provided in the via UI*/
	private String studentId;
	private String message;
	
	private String avatarUrl;
	private String smallAvatarUrl;
	private String studentInitials;
	private String studentFullName;
	private String studentAffiliation;
	private String studentLocation;
	private boolean personalProfile;


	public void init() {
		//TODO we can lazily initialize these lists using tab event listeners
		try {
			User student = getUser();
			initializeStudentData(student);
			initializeSocialNetworkData(student);
			initializeTargetCredentialData(student);
			initializeTargetCompetenceData(student);
			//initializeExternalTargetCompetenceData(student);
//			initializeUnfinishedCompetences(student);
			initializeInProgressCredentials(student);
			initializeSocialNetworkNameMap();
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Cannot find user", e);
			try {
				PageUtil.sendToAccessDeniedPage();
			} catch (IOException ex) {
				logger.error("Error redirecting user to acces denied page", ex);
			}
		}

	}
	
	public void sendMessage() {
		if (StringUtils.isNotBlank(studentId)) {
			if (Long.valueOf(studentId) != loggedUserBean.getUserId()) {
				try {
					long decodedRecieverId = Long.valueOf(studentId);
					Message message = messagingManager.sendMessage(loggedUserBean.getUserId(), decodedRecieverId, this.message);
					logger.debug("User "+loggedUserBean.getUserId()+" sent a message to "+decodedRecieverId+" with content: '"+message+"'");
					
					List<UserData> participants = new ArrayList<UserData>();
					
					participants.add(new UserData(loggedUserBean.getUserId(), loggedUserBean.getFullName(), loggedUserBean.getAvatar()));
					
					final Message message1 = message;
					
					taskExecutor.execute(new Runnable() {
			            @Override
			            public void run() {
			            	try {
			            		Map<String, String> parameters = new HashMap<String, String>();
			            		parameters.put("context", createContext());
			            		parameters.put("user", String.valueOf(decodedRecieverId));
			            		parameters.put("message", String.valueOf(message1.getId()));
			            		eventFactory.generateEvent(EventType.SEND_MESSAGE, loggedUserBean.getUserId(), message1, null, parameters);
			            	} catch (EventException e) {
			            		logger.error(e);
			            	}
			            }
					});
					this.message = "";
					
					PageUtil.fireSuccessfulInfoMessage("profileGrowl", "Message sent");
				} catch (Exception e) {
					logger.error(e);
				}
			}
			else {
				PageUtil.fireErrorMessage("Canno't send message to self!");
				logger.error("Error while sending message from profile page, studentId was the same as logged student id : "+loggedUserBean.getUserId());
			}
		}
		else {
			PageUtil.fireErrorMessage("Canno't send message, student unknown!");
			logger.error("Error while sending message from profile page, studentId was blank");
		}
	}


	private void initializeStudentData(User student) {
		studentId = String.valueOf(student.getId());
		avatarUrl = AvatarUtils.getAvatarUrlInFormat(student, ImageFormat.size120x120);
		smallAvatarUrl = AvatarUtils.getAvatarUrlInFormat(student, ImageFormat.size34x34);
		studentInitials = getInitials(student);
		studentFullName = student.getName()+" "+student.getLastname();
		studentLocation = student.getLocationName();
		personalProfile = student.getId() == loggedUserBean.getUserId();
	}

	private void initializeTargetCredentialData(User student) {
		List<TargetCredential1> targetCredential1List = credentialManager.getAllCompletedCredentials(
				student.getId(), 
				true);
		
		credentialAchievementsData = new CredentialAchievementsDataToPageMapper(idEncoder)
				.mapDataToPageObject(targetCredential1List);
	}
	
	private void initializeTargetCompetenceData(User student) {
		try {
			List<TargetCompetence1> targetCompetence1List = competenceManager.getAllCompletedCompetences(
					student.getId(), 
					false);
			
			competenceAchievementsData = new CompetenceAchievementsDataToPageMapper()
					.mapDataToPageObject(targetCompetence1List);
		} catch (Exception e) {
			PageUtil.fireErrorMessage("Competence data could not be loaded!");
			logger.error("Error while loading target credentials with progress == 100 Error:\n" + e);
		}
	}

//	private void initializeUnfinishedCompetences(User student) {
//		try {
//			List<TargetCompetence1> targetCompetence1List = competenceManager
//					.getAllUnfinishedCompetences(student.getId(), false);
//			unfinishedCompetenceAchievementsData = new CompetenceAchievementsDataToPageMapper(idEncoder)
//					.mapDataToPageObject(targetCompetence1List);
//		} catch (Exception e) {
//			PageUtil.fireErrorMessage("Credential data could not be loaded!");
//			logger.error("Error while loading target credentials with progress == 100 Error:\n" + e);
//		}
//	}
	
	private void initializeInProgressCredentials(User student) {
		try {
			List<TargetCredential1> targetCompetence1List = credentialManager.getAllInProgressCredentials(
					student.getId(), 
					true);
			
			inProgressCredentialAchievementsData = new CredentialAchievementsDataToPageMapper(idEncoder)
					.mapDataToPageObject(targetCompetence1List);
		} catch (Exception e) {
			PageUtil.fireErrorMessage("Credential data could not be loaded!");
			logger.error("Error while loading target credentials with progress == 100 Error:\n" + e);
		}
	}
	
	private void initializeSocialNetworkData(User student) {
		try {
			UserSocialNetworks userSocialNetworks = socialNetworksManager.getSocialNetworks(student.getId());
			socialNetworksData = new SocialNetworksDataToPageMapper()
					.mapDataToPageObject(userSocialNetworks);
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
	

	private User getUser() throws ResourceCouldNotBeLoadedException {
		return StringUtils.isNotBlank(studentId) 
				? userManager.get(User.class, idEncoder.decodeId(studentId)) 
				: userManager.loadResource(User.class, loggedUserBean.getUserId());
	}
	
	private String getInitials(User student) {
		if (StringUtils.isNotBlank(student.getName())) {
			if (StringUtils.isNotBlank(student.getLastname())) {
				return (student.getName().charAt(0) + "" + student.getLastname().charAt(0)).toUpperCase();
			} else
				return student.getName().toUpperCase().charAt(0) + "";
		} else {
			return "N/A";
		}
	}
	
	private String createContext() {
		return null;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public String getAlternativeName(SocialNetworkName name) {
		return nameMap.get(name.toString());
	}

	public SocialNetworksData getSocialNetworksData() {
		return socialNetworksData;
	}

	public CredentialAchievementsData getCredentialAchievementsData() {
		return credentialAchievementsData;
	}

	public String getStudentId() {
		return studentId;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public String getStudentInitials() {
		return studentInitials;
	}

	public String getStudentFullName() {
		return studentFullName;
	}

	public String getStudentAffiliation() {
		return studentAffiliation;
	}

	public String getStudentLocation() {
		return studentLocation;
	}

	public boolean isPersonalProfile() {
		return personalProfile;
	}

	public CompetenceAchievementsData getCompetenceAchievementsData() {
		return competenceAchievementsData;
	}

	public String getSmallAvatarUrl() {
		return smallAvatarUrl;
	}

	public String getMessage() {
		return message;
	}

	public CredentialAchievementsData getInProgressCredentialAchievementsData() {
		return inProgressCredentialAchievementsData;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	

}
