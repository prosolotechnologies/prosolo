package org.prosolo.services.user;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.user.data.profile.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
public interface StudentProfileManager extends AbstractManager {

    /**
     * Returns {@link StudentProfileData} object for the given user id
     *
     * @param customProfileUrl custom profile URL of the student
     * @return
     * @throws DbConnectionException
     */
    Optional<StudentProfileData> getStudentProfileData(String customProfileUrl);

    /**
     * Returns student profile learning data
     *
     * @param userId
     * @return
     * @throws DbConnectionException
     */
    ProfileLearningData getProfileLearningData(long userId);

    /**
     * Adds credentials from the passed list to the profile of a user with given id
     *
     * @param userId
     * @param idsOfTargetCredentialsToAdd - ids of target credentials to add
     * @throws DbConnectionException
     */
    void addCredentialsToProfile(long userId, List<Long> idsOfTargetCredentialsToAdd);

    /**
     * Removes credential from the student profile including all configs for credential
     * with assessment and competence configs.
     *
     * @param userId
     * @param targetCredentialId
     * @throws DbConnectionException
     */
    void removeCredentialFromProfile(long userId, long targetCredentialId);

    /**
     * Returns credential profile options data which contains available credential
     * resources for displaying on profile with the info on currently selected options
     * for displaying
     *
     * @param targetCredentialId
     * @return
     */
    CredentialProfileOptionsData getCredentialProfileOptions(long targetCredentialId);

    /**
     * Updates credential profile options based on {@code profileOptionsData}
     *
     * @param profileOptionsData
     *
     * @throws DbConnectionException
     * @throws StaleDataException when data changed in the meantime (from another browser tab for example) and update is not performed
     */
    void updateCredentialProfileOptions(CredentialProfileOptionsBasicData profileOptionsData);

    /**
     * Returns profile data for all competences in credential
     * @param credProfileConfigId
     * @return
     * @throws DbConnectionException
     */
    List<CompetenceProfileData> getCredentialCompetencesProfileData(long credProfileConfigId);

    /**
     * Returns evidence profile data list for a given competence
     *
     * @param competenceProfileConfigId
     * @return
     * @throws DbConnectionException
     */
    List<CompetenceEvidenceProfileData> getCompetenceEvidenceProfileData(long competenceProfileConfigId);

    /**
     * Returns credential assessments profile data
     *
     * @param credentialProfileConfigId
     * @return
     * @throws DbConnectionException
     */
    List<AssessmentByTypeProfileData> getCredentialAssessmentsProfileData(long credentialProfileConfigId);

    /**
     * Returns competence assessments profile data
     *
     * @param competenceProfileConfigId
     * @return
     * @throws DbConnectionException
     */
    List<AssessmentByTypeProfileData> getCompetenceAssessmentsProfileData(long competenceProfileConfigId);

    /**
     * Updates profile settings.
     *
     * @param profileSettings profile settings to be updated
     */
    void updateProfileSettings(ProfileSettingsData profileSettings) throws ConstraintViolationException;

    /**
     * Retirieves profile settings for the given student.
     *
     * @param customProfileUrl custom profile URL of the student
     * @return
     */
    Optional<ProfileSettingsData> getProfileSettingsData(String customProfileUrl);

    /**
     * Retirieves profile settings for the given student.
     *
     * @param userId id of the student
     * @return
     */
    Optional<ProfileSettingsData> getProfileSettingsData(long userId);

    /**
     * Generates new profile settings for the given user.
     *
     * @param userId id of the profile owner
     * @param summarySidebarEnabled whether summary should be displayed in the Profile page
     * @param session Hibernate session to be used to perform queries
     * @return
     */
    ProfileSettingsData generateProfileSettings(long userId, boolean summarySidebarEnabled, Session session);
}
