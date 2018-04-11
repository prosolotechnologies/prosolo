package org.prosolo.common.domainmodel.credential;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Organization;

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
public class CredentialCategory extends BaseEntity {

    private static final long serialVersionUID = -5410256371279670751L;

    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
