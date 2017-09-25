package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.RubricData;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-24
 * @since 1.0.0
 */
public interface RubricManager extends AbstractManager {

    Rubric createNewRubric(String name, UserContextData context)
            throws DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException;

    Result<Rubric> createNewRubricAndGetEvents(String name, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    PaginatedResult<RubricData> getRubrics(int page, int limit,long organizationId)
            throws DbConnectionException;

    List<Rubric> getAllRubrics (Session session) throws DbConnectionException;

    void deleteRubric(long rubricId,UserContextData context) throws DbConnectionException, EventException;

    Result<Void> deleteRubricAndGetEvents(long rubricId, UserContextData context)
            throws DbConnectionException;

    String getRubricName(long id);

    RubricData getOrganizationRubric(long rubricId);

    RubricData getRubricData(long rubricId, boolean loadCreator, boolean loadItems, boolean trackChanges)
            throws DbConnectionException;
}
