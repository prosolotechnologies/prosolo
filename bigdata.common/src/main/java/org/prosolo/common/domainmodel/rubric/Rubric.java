package org.prosolo.common.domainmodel.rubric;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
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

    private User creator;
    private Organization organization;
    private Set<Category> categories;
    private Set<Level> levels;
    private boolean readyToUse;

    public Rubric() {
        categories = new HashSet<>();
        levels = new HashSet<>();
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
    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    @Type(type = "true_false")
    @Column(columnDefinition = "char(1) DEFAULT 'F'", nullable = false)
    public boolean isReadyToUse() {
        return readyToUse;
    }

    public void setReadyToUse(boolean readyToUse) {
        this.readyToUse = readyToUse;
    }
}
