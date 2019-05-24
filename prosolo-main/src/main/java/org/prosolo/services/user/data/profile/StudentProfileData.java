package org.prosolo.services.user.data.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.profile.data.UserSocialNetworksData;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
@AllArgsConstructor
@Getter
@Setter
public class StudentProfileData implements Serializable {

    private static final long serialVersionUID = -6509477877072995757L;

    private UserData studentData;
    private UserSocialNetworksData socialNetworks;
    private ProfileLearningData profileLearningData;
    private ProfileSettingsData profileSettings;

}
