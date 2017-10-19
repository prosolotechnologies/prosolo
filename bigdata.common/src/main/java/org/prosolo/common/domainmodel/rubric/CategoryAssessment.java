package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.*;

/**
 * @author stefanvuckovic
 * @date 2017-10-11
 * @since 1.0.0
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"assessment", "category"})})
public class CategoryAssessment extends BaseEntity {

    private ActivityAssessment assessment;
    private Category category;
    private Level level;
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    public ActivityAssessment getAssessment() {
        return assessment;
    }

    public void setAssessment(ActivityAssessment assessment) {
        this.assessment = assessment;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
