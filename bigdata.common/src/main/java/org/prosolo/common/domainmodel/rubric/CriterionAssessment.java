package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * @author stefanvuckovic
 * @date 2018-01-30
 * @since 1.2.0
 */
@MappedSuperclass
public class CriterionAssessment extends BaseEntity {

    private static final long serialVersionUID = 5736873719391939079L;

    private Criterion criterion;
    private Level level;
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    public Criterion getCriterion() {
        return criterion;
    }

    public void setCriterion(Criterion criterion) {
        this.criterion = criterion;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Column(length = 1000)
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
