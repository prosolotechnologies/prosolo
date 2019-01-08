package org.prosolo.services.user.data.profile;

import org.prosolo.services.nodes.data.organization.CredentialCategoryData;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-04-13
 * @since 1.2.0
 */
public class CategorizedCredentialsProfileData implements Serializable {

    private final CredentialCategoryData category;
    private final List<CredentialProfileData> credentials;

    public CategorizedCredentialsProfileData(CredentialCategoryData category, List<CredentialProfileData> credentials) {
        this.category = category;
        this.credentials = credentials;
    }

    public CredentialCategoryData getCategory() {
        return category;
    }

    public List<CredentialProfileData> getCredentials() {
        return credentials;
    }
}
