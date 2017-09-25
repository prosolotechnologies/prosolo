package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.*;
import java.util.Set;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-22
 * @since 1.0.0
 */

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"title","rubric"})})
public class Level extends BaseEntity{

    private double points;
    private Rubric rubric;
    private int order;
    private Set<CategoryLevel> categories;

    @Column(name = "points", nullable = false)
    public double getPoints() {
        return points;
    }

    public void setPoints(double points){
        this.points = points;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Rubric getRubric() {
        return rubric;
    }

    public void setRubric(Rubric rubric) {
        this.rubric = rubric;
    }

    @Column(name = "level_order", nullable = false)
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @OneToMany(mappedBy = "level", cascade = CascadeType.REMOVE, orphanRemoval = true)
    public Set<CategoryLevel> getCategories() {
        return categories;
    }

    public void setCategories(Set<CategoryLevel> categories) {
        this.categories = categories;
    }
}
