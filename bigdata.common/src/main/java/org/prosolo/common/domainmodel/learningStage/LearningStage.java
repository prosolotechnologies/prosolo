package org.prosolo.common.domainmodel.learningStage;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Organization;

import javax.persistence.*;

/**
 * @author stefanvuckovic
 * @date 2017-11-14
 * @since 1.2.0
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"organization", "title"})})
public class LearningStage extends BaseEntity {

    private int order;
    private Organization organization;

    @Column(name = "learning_stage_order")
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
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
