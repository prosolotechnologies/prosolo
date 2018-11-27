package org.prosolo.services.user;

import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.user.data.profile.CategorizedCredentialsProfileData;
import org.prosolo.services.user.data.profile.CredentialProfileOptionsData;
import org.prosolo.services.user.data.profile.StudentProfileData;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;

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
     * @param userId
     * @return
     * @throws DbConnectionException
     */
    Optional<StudentProfileData> getStudentProfileData(long userId);

    /**
     * Returns credential profile data grouped by credential category for the given user
     *
     * @param userId
     * @return
     */
    List<CategorizedCredentialsProfileData> getCredentialProfileData(long userId);

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
}
