package org.prosolo.web.profile;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.studentprofile.CompetenceProfileConfig;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.common.data.SelectableData;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.credential.CredentialIdData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.StudentProfileManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.user.data.profile.*;
import org.prosolo.services.user.data.profile.factory.CredentialProfileOptionsFullToBasicFunction;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.profile.data.UserSocialNetworksData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ManagedBean(name = "profileBean")
@Component("profileBean")
@Scope("view")
public class ProfileBean {
	
	private static Logger logger = Logger.getLogger(ProfileBean.class);

	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private CredentialManager credentialManager;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private StudentProfileManager studentProfileManager;
	@Inject 
	private MessagingManager messagingManager;
	@Inject private CredentialProfileOptionsFullToBasicFunction credentialProfileOptionsFullToBasicFunction;

	private StudentProfileData studentProfileData;
	private List<SelectableData<CredentialIdData>> credentialsToAdd = new ArrayList<>();

	/* parameter that can be provided in the via UI*/
	private String studentId;
	private long decodedStudentId;
	private String message;

	private long ownerOfAProfileUserId;

	private CredentialProfileData credentialToRemove;
	private CredentialProfileOptionsData credentialForEdit;

	private CredentialProfileData selectedCredentialProfileData;
	private CompetenceProfileData selectedCompetenceProfileData;
	private LearningResourceType selectedResourceType;

	public void init() {
		decodedStudentId = idEncoder.decodeId(studentId);
		ownerOfAProfileUserId = StringUtils.isNotBlank(studentId) ? decodedStudentId : loggedUserBean.getUserId();
		if (ownerOfAProfileUserId > 0) {
		    try {
                Optional<StudentProfileData> studentProfileDataOpt = studentProfileManager.getStudentProfileData(ownerOfAProfileUserId);
                if (studentProfileDataOpt.isPresent()) {
                    studentProfileData = studentProfileDataOpt.get();
                } else {
                    PageUtil.notFound();
                }
            } catch (Exception e) {
		        PageUtil.fireErrorMessage("Error loading the page");
            }
		} else {
			PageUtil.notFound();
		}
	}

	public void initCompetencesIfNotInitialized(CredentialProfileData credentialProfileData) {
		if (!credentialProfileData.getCompetences().isInitialized()) {
			try {
				credentialProfileData.getCompetences().init(
						studentProfileManager.getCredentialCompetencesProfileData(
								credentialProfileData.getCredentialProfileConfigId()));
			} catch (DbConnectionException e) {
				logger.error("error", e);
				PageUtil.fireErrorMessage("Error loading the data");
			}
		}
	}

	public void initCompetenceEvidenceIfNotInitialized(CompetenceProfileData competenceProfileData) {
		this.selectedResourceType = LearningResourceType.COMPETENCE;
		this.selectedCompetenceProfileData = competenceProfileData;
		if (!competenceProfileData.getEvidence().isInitialized()) {
			try {
				competenceProfileData.getEvidence().init(
						studentProfileManager.getCompetenceEvidenceProfileData(competenceProfileData.getId()));
			} catch (DbConnectionException e) {
				logger.error("error", e);
				PageUtil.fireErrorMessage("Error loading the data");
			}
		}
	}

	public void initCredentialAssessmentsIfNotInitialized(CredentialProfileData credentialProfileData) {
		this.selectedResourceType = LearningResourceType.CREDENTIAL;
		this.selectedCredentialProfileData = credentialProfileData;
		if (!credentialProfileData.getAssessments().isInitialized()) {
			try {
				credentialProfileData.getAssessments().init(
						studentProfileManager.getCredentialAssessmentsProfileData(credentialProfileData.getCredentialProfileConfigId()));
			} catch (DbConnectionException e) {
				logger.error("error", e);
				PageUtil.fireErrorMessage("Error loading the data");
			}
		}
	}

    public void initCompetenceAssessmentsIfNotInitialized(CompetenceProfileData compProfileData) {
        this.selectedResourceType = LearningResourceType.COMPETENCE;
        this.selectedCompetenceProfileData = compProfileData;
        if (!compProfileData.getAssessments().isInitialized()) {
            try {
                compProfileData.getAssessments().init(
                        studentProfileManager.getCompetenceAssessmentsProfileData(compProfileData.getId()));
            } catch (DbConnectionException e) {
                logger.error("error", e);
                PageUtil.fireErrorMessage("Error loading the data");
            }
        }
    }

	public void prepareAddingCredentials() {
		try {
			credentialsToAdd.clear();
			List<CredentialIdData> completedCredentialsBasicData = credentialManager.getCompletedCredentialsBasicDataForCredentialsNotAddedToProfile(ownerOfAProfileUserId);
			completedCredentialsBasicData.forEach(cred -> credentialsToAdd.add(new SelectableData<>(cred)));
		} catch (DbConnectionException e) {
			logger.error("error", e);
			PageUtil.fireErrorMessage("Error loading " + ResourceBundleUtil.getLabel("credential.plural").toLowerCase());
		}
	}

	public void addCredentials() {
		try {
            List<Long> idsOfTargetCredentialsToAdd = credentialsToAdd
                    .stream()
                    .filter(cred -> cred.isSelected())
                    .map(cred -> cred.getData().getId())
                    .collect(Collectors.toList());
            int numberOfCredentialsToAdd = idsOfTargetCredentialsToAdd.size();
		    if (numberOfCredentialsToAdd > 0) {
                studentProfileManager.addCredentialsToProfile(ownerOfAProfileUserId, idsOfTargetCredentialsToAdd);
                boolean success = refreshProfile();
                if (success) {
					PageUtil.fireSuccessfulInfoMessage((numberOfCredentialsToAdd == 1 ? ResourceBundleUtil.getLabel("credential") : ResourceBundleUtil.getLabel("credential.plural")) + " added");
				}
            } else {
		        PageUtil.fireWarnMessage("Error","No " + ResourceBundleUtil.getLabel("credential.plural").toLowerCase() + " selected");
            }
		} catch (DbConnectionException e) {
			logger.error("error", e);
			PageUtil.fireErrorMessage("Error adding " + ResourceBundleUtil.getLabel("credential.plural").toLowerCase() + " to the profile");
		}
	}

	private boolean refreshProfile() {
		//reload credentials
		try {
			studentProfileData.setCredentialProfileData(studentProfileManager.getCredentialProfileData(ownerOfAProfileUserId));
			return true;
		} catch (Exception e) {
			logger.error("error", e);
			PageUtil.fireErrorMessage("Error refreshing the data");
			return false;
		}
	}

	public void prepareRemovingCredentialFromProfile(CredentialProfileData credentialProfileData) {
		this.credentialToRemove = credentialProfileData;
	}

	public void removeCredentialFromProfile() {
		try {
			studentProfileManager.removeCredentialFromProfile(ownerOfAProfileUserId, credentialToRemove.getTargetCredentialId());
			boolean success = refreshProfile();
			if (success) {
				PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getLabel("credential") + " successfully removed from profile");
			}
		} catch (DbConnectionException e) {
			logger.error("error", e);
			PageUtil.fireErrorMessage("Error removing the " + ResourceBundleUtil.getLabel("credential").toLowerCase() + " from the profile");
		}
	}

	public void prepareEditCredential(CredentialProfileData credentialProfileData) {
		try {
			credentialForEdit = studentProfileManager.getCredentialProfileOptions(credentialProfileData.getTargetCredentialId());
		} catch (Exception e) {
			credentialForEdit = null;
			logger.error("error", e);
			PageUtil.fireErrorMessage("Error loading the data");
		}
	}

	public void updateCredentialProfileOptions() {
		if (credentialForEdit != null) {
			try {
				studentProfileManager.updateCredentialProfileOptions(
						credentialProfileOptionsFullToBasicFunction.apply(credentialForEdit));
				boolean success = refreshProfile();
				if (success) {
					PageUtil.fireSuccessfulInfoMessage("Profile options successfully saved");
				}
			} catch (StaleDataException e) {
				logger.error("error", e);
				PageUtil.fireErrorMessage("Error: " + ResourceBundleUtil.getLabel("credential").toLowerCase() + " profile options have bean changed in the meantime. Please try again.");
			} catch (DbConnectionException e) {
				logger.error("error", e);
				PageUtil.fireErrorMessage("Error updating profile options");
			}
		}
	}

	public void sendMessage() {
		if (!isPersonalProfile()) {
			try {
				MessageData messageData = messagingManager.sendMessage(0, loggedUserBean.getUserId(), decodedStudentId, this.message, loggedUserBean.getUserContext());
				logger.debug("User "+loggedUserBean.getUserId()+" sent a message to "+decodedStudentId+" with content: '"+messageData+"'");

				List<UserData> participants = new ArrayList<UserData>();

				participants.add(new UserData(loggedUserBean.getUserId(), loggedUserBean.getFullName()));

				final Message message1 = new Message();
				message1.setId(messageData.getId());

				UserContextData userContext = loggedUserBean.getUserContext();

				this.message = "";

				PageUtil.fireSuccessfulInfoMessage("Your message is sent");
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public String getStudentId() {
		return studentId;
	}

	public boolean isPersonalProfile() {
		return ownerOfAProfileUserId == loggedUserBean.getUserId();
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

	public StudentProfileData getStudentProfileData() {
		return studentProfileData;
	}

	public UserData getUserData() {
		return studentProfileData != null ? studentProfileData.getStudentData() : null;
	}

	public UserSocialNetworksData getUserSocialNetworksData() {
		return studentProfileData != null ? studentProfileData.getSocialNetworks() : null;
	}

	public List<SelectableData<CredentialIdData>> getCredentialsToAdd() {
		return credentialsToAdd;
	}

	public List<CategorizedCredentialsProfileData> getCredentialProfileData() {
	    return studentProfileData != null ? studentProfileData.getCredentialProfileData() : null;
    }

	public CredentialProfileOptionsData getCredentialForEdit() {
		return credentialForEdit;
	}

	public String getSelectedResourceTitle() {
		return selectedResourceType != null
				? selectedResourceType == LearningResourceType.CREDENTIAL
					? selectedCredentialProfileData.getTitle()
					: selectedCompetenceProfileData.getTitle()
				: "";
	}

	public List<CompetenceEvidenceProfileData> getSelectedCompetenceEvidences() {
		return selectedCompetenceProfileData != null
				? selectedCompetenceProfileData.getEvidence().getData()
				: Collections.emptyList();
	}

	public LearningResourceType getSelectedResourceType() {
		return selectedResourceType;
	}

	public List<AssessmentByTypeProfileData> getSelectedResourceAssessmentsByType() {
		return selectedResourceType != null
				? selectedResourceType == LearningResourceType.CREDENTIAL
						? selectedCredentialProfileData.getAssessments().getData()
						: selectedCompetenceProfileData.getAssessments().getData()
				: null;
	}

	public boolean isAssessmentTabInitiallyActive(AssessmentType type) {
	    List<AssessmentByTypeProfileData> assessments = getSelectedResourceAssessmentsByType();
	    if (assessments != null && !assessments.isEmpty()) {
	        return assessments.get(0).getAssessmentType() == type;
        }
	    return false;
    }

}
