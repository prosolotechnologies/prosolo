package org.prosolo.services.user.data.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-12-04
 * @since 1.2.0
 */
@AllArgsConstructor
@Getter
@Setter
public class ProfileLearningData implements Serializable {

    private static final long serialVersionUID = 7314629856490421943L;

    private ProfileSummaryData profileSummaryData;
    private List<CategorizedCredentialsProfileData> credentialProfileData;

}
