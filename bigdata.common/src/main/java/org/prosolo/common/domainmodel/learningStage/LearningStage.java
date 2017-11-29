package org.prosolo.common.domainmodel.learningStage;

import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Organization;

import javax.persistence.*;
import java.util.Set;

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

    private Set<Credential1> credentials;
    private Set<Competence1> competences;

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

    @OneToMany(mappedBy = "learningStage")
    public Set<Credential1> getCredentials() {
        return credentials;
    }

    public void setCredentials(Set<Credential1> credentials) {
        this.credentials = credentials;
    }

    @OneToMany(mappedBy = "learningStage")
    public Set<Competence1> getCompetences() {
        return competences;
    }

    public void setCompetences(Set<Competence1> competences) {
        this.competences = competences;
    }
}
