package org.prosolo.common.domainmodel.rubric;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-22
 * @since 1.0.0
 */

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"title","organization"})})
public class Rubric extends BaseEntity {

    private RubricType rubricType = RubricType.DESCRIPTIVE;
    private User creator;
    private Organization organization;
    private Set<Criterion> criteria;
    private Set<Level> levels;
    private boolean readyToUse;
    private List<RubricUnit> rubricUnits;

    public Rubric() {
        criteria = new HashSet<>();
        levels = new HashSet<>();
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public RubricType getRubricType() {
        return rubricType;
    }

    public void setRubricType(RubricType rubricType) {
        this.rubricType = rubricType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public Organization getOrganization(){
        return organization;
    }

    public void setOrganization(Organization organization){
        this.organization = organization;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @OneToMany(mappedBy = "rubric", cascade = CascadeType.REMOVE)
    public Set<Level> getLevels() {
        return levels;
    }

    public void setLevels(Set<Level> levels) {
        this.levels = levels;
    }

    @OneToMany(mappedBy = "rubric", cascade = CascadeType.REMOVE)
    public Set<Criterion> getCriteria() {
        return criteria;
    }

    public void setCriteria(Set<Criterion> criteria) {
        this.criteria = criteria;
    }

    @Type(type = "true_false")
    @Column(columnDefinition = "char(1) DEFAULT 'F'", nullable = false)
    public boolean isReadyToUse() {
        return readyToUse;
    }

    public void setReadyToUse(boolean readyToUse) {
        this.readyToUse = readyToUse;
    }

    @OneToMany(mappedBy = "rubric", cascade = CascadeType.REMOVE)
    public List<RubricUnit> getRubricUnits() {
        return rubricUnits;
    }

    public void setRubricUnits(List<RubricUnit> rubricUnits) {
        this.rubricUnits = rubricUnits;
    }
}
