package org.prosolo.common.domainmodel.credential;

import lombok.Getter;
import lombok.Setter;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.settings.CredentialCategoriesPlugin;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author stefanvuckovic
 * @date 2018-04-11
 * @since 1.2.0
 */
@Entity
//unique constraint added from the script
public class CredentialCategory extends BaseEntity {

    private static final long serialVersionUID = -5410256371279670751L;

    private CredentialCategoriesPlugin credentialCategoriesPlugin;
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public CredentialCategoriesPlugin getCredentialCategoriesPlugin() {
        return credentialCategoriesPlugin;
    }

    public void setCredentialCategoriesPlugin(CredentialCategoriesPlugin credentialCategoriesPlugin) {
        this.credentialCategoriesPlugin = credentialCategoriesPlugin;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
