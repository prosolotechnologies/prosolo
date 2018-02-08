package org.prosolo.common.domainmodel.rubric.visitor;

import org.prosolo.common.domainmodel.rubric.Level;
import org.prosolo.common.domainmodel.rubric.PointLevel;
import org.prosolo.common.domainmodel.rubric.PointRangeLevel;

/**
 * @author stefanvuckovic
 * @date 2018-01-10
 * @since 1.2.0
 */
public interface LevelVisitor<T> {

    T visit(Level level);

    T visit(PointLevel level);

    T visit(PointRangeLevel level);
}
