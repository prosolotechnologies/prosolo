package org.prosolo.common.domainmodel.organization.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.prosolo.common.domainmodel.credential.CredentialCategory;

import javax.persistence.*;
import java.util.Set;

/**
 * @author Nikola Milikic
 * @date 2019-05-30
 * @since 1.3.2
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialCategoriesPlugin extends OrganizationPlugin {

    private Set<CredentialCategory> credentialCategories;

    @OneToMany(mappedBy = "credentialCategoriesPlugin", cascade = CascadeType.REMOVE, orphanRemoval = true)
    public Set<CredentialCategory> getCredentialCategories() {
        return credentialCategories;
    }

    public void setCredentialCategories(Set<CredentialCategory> credentialCategories) {
        this.credentialCategories = credentialCategories;
    }

}
