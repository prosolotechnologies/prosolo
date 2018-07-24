package org.prosolo.common.domainmodel.rubric.visitor;

import org.prosolo.common.domainmodel.rubric.Criterion;
import org.prosolo.common.domainmodel.rubric.PointCriterion;

/**
 * @author stefanvuckovic
 * @date 2018-01-10
 * @since 1.2.0
 */
public interface CriterionVisitor<T> {

    T visit(Criterion criterion);
    T visit(PointCriterion criterion);
}
