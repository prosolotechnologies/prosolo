package org.prosolo.common.domainmodel.rubric;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.*;
import java.util.List;

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
    private List<Category> categories;
    private List<Level> levels;

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
    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    @OneToMany(mappedBy = "rubric", cascade = CascadeType.REMOVE)
    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

}
