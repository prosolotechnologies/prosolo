package org.prosolo.common.domainmodel.organization.settings;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.organization.Organization;

import javax.persistence.*;

/**
 * An abstract class that describes an organization plugin.
 *
 * @author Nikola Milikic
 * @date 2019-05-30
 * @since 1.3.2
 */

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
public abstract class OrganizationPlugin {

    private long id;
    protected boolean enabled;
    public OrganizationPluginType type;
    private Organization organization;

    @Id
    @Column(name = "id", unique = true, nullable = false, insertable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Type(type = "long")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Type(type="true_false")
    @Column(columnDefinition = "char(1) DEFAULT 'T'")
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public OrganizationPluginType getType() {
        return type;
    }

    public void setType(OrganizationPluginType type) {
        this.type = type;
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
