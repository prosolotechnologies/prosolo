package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.*;

/**
 * @author stefanvuckovic
 * @date 2017-09-20
 * @since 1.1.0
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"criterion", "level"})})
/**
 * Represents a level of a criterion that can store the description explaining when the level of this criterion is achieved.
 */
public class CriterionLevel extends BaseEntity {

    private static final long serialVersionUID = -1389497705841901046L;

    private Criterion criterion;
    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public Criterion getCriterion() {
        return criterion;
    }

    public void setCriterion(Criterion criterion) {
        this.criterion = criterion;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
