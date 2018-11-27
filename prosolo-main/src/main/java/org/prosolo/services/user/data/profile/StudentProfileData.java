package org.prosolo.services.user.data.profile;

import org.prosolo.services.user.data.UserData;
import org.prosolo.web.profile.data.UserSocialNetworksData;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
public class StudentProfileData implements Serializable {

    private static final long serialVersionUID = -6509477877072995757L;

    private UserData studentData;
    private UserSocialNetworksData socialNetworks;
    private ProfileSummaryData profileSummaryData;
    private List<CategorizedCredentialsProfileData> credentialProfileData;

    public StudentProfileData(UserData studentData, UserSocialNetworksData socialNetworks, ProfileSummaryData profileSummaryData, List<CategorizedCredentialsProfileData> credentialProfileData) {
        this.studentData = studentData;
        this.socialNetworks = socialNetworks;
        this.profileSummaryData = profileSummaryData;
        this.credentialProfileData = credentialProfileData;
    }

    public UserData getStudentData() {
        return studentData;
    }

    public void setStudentData(UserData studentData) {
        this.studentData = studentData;
    }

    public UserSocialNetworksData getSocialNetworks() {
        return socialNetworks;
    }

    public void setSocialNetworks(UserSocialNetworksData socialNetworks) {
        this.socialNetworks = socialNetworks;
    }

    public ProfileSummaryData getProfileSummaryData() {
        return profileSummaryData;
    }

    public void setProfileSummaryData(ProfileSummaryData profileSummaryData) {
        this.profileSummaryData = profileSummaryData;
    }

    public List<CategorizedCredentialsProfileData> getCredentialProfileData() {
        return credentialProfileData;
    }

    public void setCredentialProfileData(List<CategorizedCredentialsProfileData> credentialProfileData) {
        this.credentialProfileData = credentialProfileData;
    }
}
