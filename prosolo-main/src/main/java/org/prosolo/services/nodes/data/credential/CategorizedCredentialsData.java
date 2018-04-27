package org.prosolo.services.nodes.data.credential;

import org.prosolo.services.nodes.data.organization.CredentialCategoryData;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-04-13
 * @since 1.2.0
 */
public class CategorizedCredentialsData implements Serializable {

    private CredentialCategoryData category;
    private List<TargetCredentialData> credentials;

    public CategorizedCredentialsData(CredentialCategoryData category, List<TargetCredentialData> credentials) {
        this.category = category;
        this.credentials = credentials;
    }

    public CredentialCategoryData getCategory() {
        return category;
    }

    public List<TargetCredentialData> getCredentials() {
        return credentials;
    }
}
