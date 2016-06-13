package org.prosolo.web.manage.students;

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
import org.prosolo.web.activitywall.data.UserDataFactory;
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

@ManagedBean(name = "profile1")
@Component
@Scope("view")
public class Profile1 {
	
	private static Logger logger = Logger.getLogger(Profile1.class);
	
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
	private CompetenceAchievementsData unfinishedCompetenceAchievementsData;
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
			initializeUnfinishedCompetences(student);
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
		if(StringUtils.isNotBlank(studentId)) {
			if(Long.valueOf(studentId) != loggedUserBean.getUser().getId()) {
				try {
					long decodedRecieverId = Long.valueOf(studentId);
					Message message = messagingManager.sendMessage(loggedUserBean.getUser().getId(), decodedRecieverId, this.message);
					logger.debug("User "+loggedUserBean.getUser()+" sent a message to "+decodedRecieverId+" with content: '"+message+"'");
					
					List<UserData> participants = new ArrayList<UserData>();
					participants.add(UserDataFactory.createUserData(loggedUserBean.getUser()));
					
					final Message message1 = message;
					final User user = loggedUserBean.getUser();
					
					taskExecutor.execute(new Runnable() {
			            @Override
			            public void run() {
			            	try {
			            		Map<String, String> parameters = new HashMap<String, String>();
			            		parameters.put("context", createContext());
			            		parameters.put("user", String.valueOf(decodedRecieverId));
			            		parameters.put("message", String.valueOf(message1.getId()));
			            		eventFactory.generateEvent(EventType.SEND_MESSAGE, user, message1, parameters);
			            	} catch (EventException e) {
			            		logger.error(e);
			            	}
			            }
					});
					this.message = "";
					PageUtil.fireSuccessfulInfoMessage("profileGrowl", 
							"You have sent a message to " + studentFullName);
				} catch (Exception e) {
					logger.error(e);
				}
			}
			else {
				PageUtil.fireErrorMessage("Canno't send message to self!");
				logger.error("Error while sending message from profile page, studentId was the same as logged student id : "+loggedUserBean.getUser().getId());
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
		//studentAffiliation = student.get
		studentLocation = student.getLocationName();
		personalProfile = student.getId() == loggedUserBean.getUser().getId();
		
	}

	private void initializeTargetCredentialData(User student) {
		List<TargetCredential1> targetCredential1List = credentialManager
				.getAllCompletedCredentials(student.getId(), false);
		credentialAchievementsData = new CredentialAchievementsDataToPageMapper(idEncoder)
				.mapDataToPageObject(targetCredential1List);
	}
	
	private void initializeTargetCompetenceData(User student) {
		try {
			List<TargetCompetence1> targetCompetence1List = competenceManager
					.getAllCompletedCompetences(student.getId(), false);
			competenceAchievementsData = new CompetenceAchievementsDataToPageMapper(idEncoder)
					.mapDataToPageObject(targetCompetence1List);
		} catch (Exception e) {
			PageUtil.fireErrorMessage("Credential data could not be loaded!");
			logger.error("Error while loading target credentials with progress == 100 Error:\n" + e);
		}
	}
	

	private void initializeUnfinishedCompetences(User student) {
		try {
			List<TargetCompetence1> targetCompetence1List = competenceManager
					.getAllUnfinishedCompetences(student.getId(), false);
			unfinishedCompetenceAchievementsData = new CompetenceAchievementsDataToPageMapper(idEncoder)
					.mapDataToPageObject(targetCompetence1List);
		} catch (Exception e) {
			PageUtil.fireErrorMessage("Credential data could not be loaded!");
			logger.error("Error while loading target credentials with progress == 100 Error:\n" + e);
		}
		
	}
	


	private void initializeSocialNetworkData(User student) {
		UserSocialNetworks userSocialNetworks = socialNetworksManager.getSocialNetworks(student);
		socialNetworksData = new SocialNetworksDataToPageMapper()
				.mapDataToPageObject(userSocialNetworks);
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
				: loggedUserBean.getUser();
	}
	
	private String getInitials(User student) {
		if(StringUtils.isNotBlank(student.getName())) {
			if(StringUtils.isNotBlank(student.getLastname())) {
				return (student.getName().charAt(0) + "" + student.getLastname().charAt(0)).toUpperCase();
			}
			else return student.getName().toUpperCase().charAt(0)+"";
		}
		else {
			return "N/A";
		}
	}
	
	private String createContext() {
		return null;
	}
	
	
	public String getAlternativeName(SocialNetworkName name) {
		return nameMap.get(name.toString());
	}

	public SocialNetworksManager getSocialNetworksManager() {
		return socialNetworksManager;
	}

	public void setSocialNetworksManager(SocialNetworksManager socialNetworksManager) {
		this.socialNetworksManager = socialNetworksManager;
	}

	public void setLoggedUserBean(LoggedUserBean loggedUserBean) {
		this.loggedUserBean = loggedUserBean;
	}

	public SocialNetworksData getSocialNetworksData() {
		return socialNetworksData;
	}

	public void setSocialNetworksData(SocialNetworksData socialNetworksData) {
		this.socialNetworksData = socialNetworksData;
	}


	public CredentialManager getCredentialManager() {
		return credentialManager;
	}


	public void setCredentialManager(CredentialManager credentialManager) {
		this.credentialManager = credentialManager;
	}
	
	public CredentialAchievementsData getCredentialAchievementsData() {
		return credentialAchievementsData;
	}


	public void setCredentialAchievementsData(CredentialAchievementsData credentialAchievementsData) {
		this.credentialAchievementsData = credentialAchievementsData;
	}


	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}


	public void setIdEncoder(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}


	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getStudentInitials() {
		return studentInitials;
	}

	public void setStudentInitials(String studentInitials) {
		this.studentInitials = studentInitials;
	}

	public String getStudentFullName() {
		return studentFullName;
	}

	public void setStudentFullName(String studentFullName) {
		this.studentFullName = studentFullName;
	}

	public String getStudentAffiliation() {
		return studentAffiliation;
	}

	public void setStudentAffiliation(String studentAffiliation) {
		this.studentAffiliation = studentAffiliation;
	}

	public String getStudentLocation() {
		return studentLocation;
	}

	public void setStudentLocation(String studentLocation) {
		this.studentLocation = studentLocation;
	}

	public boolean isPersonalProfile() {
		return personalProfile;
	}

	public void setPersonalProfile(boolean personalProfile) {
		this.personalProfile = personalProfile;
	}



	public Competence1Manager getCompetenceManager() {
		return competenceManager;
	}

	public void setCompetenceManager(Competence1Manager competenceManager) {
		this.competenceManager = competenceManager;
	}

	public CompetenceAchievementsData getCompetenceAchievementsData() {
		return competenceAchievementsData;
	}


	public CompetenceAchievementsData getUnfinishedCompetenceAchievementsData() {
		return unfinishedCompetenceAchievementsData;
	}


	public void setUnfinishedCompetenceAchievementsData(CompetenceAchievementsData unfinishedCompetenceAchievementsData) {
		this.unfinishedCompetenceAchievementsData = unfinishedCompetenceAchievementsData;
	}


	public String getSmallAvatarUrl() {
		return smallAvatarUrl;
	}

	public MessagingManager getMessagingManager() {
		return messagingManager;
	}

	public void setMessagingManager(MessagingManager messagingManager) {
		this.messagingManager = messagingManager;
	}

	public void setEventFactory(EventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
}
