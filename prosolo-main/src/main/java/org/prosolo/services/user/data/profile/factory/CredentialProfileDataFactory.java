package org.prosolo.services.user.data.profile.factory;

import org.prosolo.common.domainmodel.credential.CredentialCategory;
import org.prosolo.common.domainmodel.studentprofile.CredentialProfileConfig;
import org.prosolo.services.common.data.LazyInitData;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.nodes.util.TimeUtil;
import org.prosolo.services.user.data.profile.CategorizedCredentialsProfileData;
import org.prosolo.services.user.data.profile.CredentialProfileData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author stefanvuckovic
 * @date 2018-11-19
 * @since 1.2.0
 */
@Component
public class CredentialProfileDataFactory {

    public CredentialProfileData getCredentialProfileData(CredentialProfileConfig credentialProfileConfig) {
        CredentialCategory category = credentialProfileConfig.getTargetCredential().getCredential().getCategory();
        CredentialCategoryData categoryData = category != null ? new CredentialCategoryData(category.getId(), category.getTitle(), false) : null;
        return new CredentialProfileData(
                credentialProfileConfig.getId(),
                credentialProfileConfig.getTargetCredential().getId(),
                credentialProfileConfig.getTargetCredential().getCredential().getId(),
                credentialProfileConfig.getTargetCredential().getCredential().getTitle(),
                credentialProfileConfig.getTargetCredential().getCredential().getDescription(),
                TimeUtil.getHoursAndMinutesInString(credentialProfileConfig.getTargetCredential().getCredential().getDuration()),
                credentialProfileConfig.getTargetCredential().getCredential().getTags().stream().map(tag -> tag.getTitle()).collect(Collectors.toList()),
                credentialProfileConfig.getTargetCredential().getDateFinished().getTime(),
                new LazyInitData<>(),
                new LazyInitData<>(),
                categoryData);
    }

    /**
     * This method assumes that credentials are already sorted by category
     *
     * @param credentialsSortedByCategory
     * @return
     */
    public List<CategorizedCredentialsProfileData> groupCredentialsByCategory(List<CredentialProfileData> credentialsSortedByCategory) {
        if (credentialsSortedByCategory == null) {
            return null;
        }
        if (credentialsSortedByCategory.isEmpty()) {
            return new ArrayList<>();
        }
        List<CategorizedCredentialsProfileData> categorizedCredentials = new ArrayList<>();
        CredentialCategoryData currentCategory = null;
        List<CredentialProfileData> credentialsInCurrentCategory = null;
        boolean first = true;
        for (CredentialProfileData cd : credentialsSortedByCategory) {
            if (!(cd.getCategory() == currentCategory || (cd.getCategory() != null && currentCategory != null && cd.getCategory().getId() == currentCategory.getId())) || first) {
                //if category is different than current one, we should add current data to the list because data for current category is collected
                if (!first) {
                    categorizedCredentials.add(new CategorizedCredentialsProfileData(currentCategory, credentialsInCurrentCategory));
                } else {
                    first = false;
                }
                currentCategory = cd.getCategory();
                credentialsInCurrentCategory = new ArrayList<>();
            }
            credentialsInCurrentCategory.add(cd);
        }
        //add last category with credentials
        categorizedCredentials.add(new CategorizedCredentialsProfileData(currentCategory, credentialsInCurrentCategory));
        return categorizedCredentials;
    }

}
