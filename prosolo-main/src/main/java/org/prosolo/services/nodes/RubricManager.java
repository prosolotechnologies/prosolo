package org.prosolo.services.nodes;

import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-24
 * @since 1.0.0
 */
public interface RubricManager extends AbstractManager {

    Rubric createNewRubric(String name, long creatorId, long organizationId, UserContextData context)
            throws DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException;

    Result<Rubric> createNewRubricAndGetEvents(String name, long creatorId, long organizationId, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;
}
