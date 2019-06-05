package org.prosolo.web.profile;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.services.common.data.SelectableData;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.credential.CredentialIdData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.StudentProfileManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.user.data.profile.*;
import org.prosolo.services.user.data.profile.factory.CredentialProfileOptionsFullToBasicFunction;
import org.prosolo.web.LoggedUserBean;
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

/**
 * This class was introduced to serve the custom profile URL scheme. It serves the page profile-new.xhtml.
 */
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
	private CredentialProfileOptionsFullToBasicFunction credentialProfileOptionsFullToBasicFunction;

	@Getter @Setter
	private String customProfileId;
	@Getter @Setter
	private String message;
	@Getter
	private long studentId;

	@Getter
	private StudentProfileData studentProfileData;
	@Getter
	private List<SelectableData<CredentialIdData>> credentialsToAdd = new ArrayList<>();

	private CredentialProfileData credentialToRemove;
	@Getter
	private CredentialProfileOptionsData credentialForEdit;

	private CredentialProfileData selectedCredentialProfileData;
	private CompetenceProfileData selectedCompetenceProfileData;
	@Getter
	private LearningResourceType selectedResourceType;

	public void init() {
		if (!StringUtils.isBlank(customProfileId)) {
		    try {
                Optional<StudentProfileData> studentProfileDataOpt = studentProfileManager.getStudentProfileData(customProfileId);
                if (studentProfileDataOpt.isPresent()) {
                    studentProfileData = studentProfileDataOpt.get();
					studentId = studentProfileData.getStudentData().getId();
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
			List<CredentialIdData> completedCredentialsBasicData = credentialManager.getCompletedCredentialsBasicDataForCredentialsNotAddedToProfile(studentId);
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
                studentProfileManager.addCredentialsToProfile(studentId, idsOfTargetCredentialsToAdd);
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
			studentProfileData.setProfileLearningData(studentProfileManager.getProfileLearningData(studentId));
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
			studentProfileManager.removeCredentialFromProfile(studentId, credentialToRemove.getTargetCredentialId());
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

	public int getTotalNumberOfCredentialAssessmentsAvailableToAdd() {
		return getTotalNumberOfAssessmentsAvailableToAdd(
				credentialForEdit != null ? credentialForEdit.getAssessments() : Collections.emptyList());
	}

	public int getTotalNumberOfCompetenceAssessmentsAvailableToAdd(CompetenceProfileOptionsData competenceProfileOptionsData) {
		return getTotalNumberOfAssessmentsAvailableToAdd(competenceProfileOptionsData.getAssessments());
	}

	private int getTotalNumberOfAssessmentsAvailableToAdd(List<AssessmentByTypeProfileOptionsData> assessments) {
		return assessments
				.stream()
				.mapToInt(assessmentByType -> assessmentByType.getAssessments().size())
				.sum();
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

	public void updateProfileSettings() {
		try {
			if (studentProfileData.getProfileSettings().hasObjectChanged()) {
				studentProfileManager.updateProfileSettings(studentProfileData.getProfileSettings());
			}
			PageUtil.fireSuccessfulInfoMessage("Profile settings are updated");
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error updating profile settings");
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public boolean isPersonalProfile() {
		return studentId == loggedUserBean.getUserId();
	}

	public UserData getUserData() {
		return studentProfileData != null ? studentProfileData.getStudentData() : null;
	}

	public UserSocialNetworksData getUserSocialNetworksData() {
		return studentProfileData != null ? studentProfileData.getSocialNetworks() : null;
	}

	public List<CategorizedCredentialsProfileData> getCredentialProfileData() {
	    return studentProfileData != null ? studentProfileData.getProfileLearningData().getCredentialProfileData() : null;
    }

	public ProfileSummaryData getProfileSummaryData() {
		return studentProfileData != null ? studentProfileData.getProfileLearningData().getProfileSummaryData() : null;
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

	public long getOwnerOfAProfileUserId() {
		return studentId;
	}
}
