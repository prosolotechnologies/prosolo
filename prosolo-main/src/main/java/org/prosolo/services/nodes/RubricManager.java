package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.OperationForbiddenException;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.assessments.grading.RubricCriteriaGradeData;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.prosolo.services.nodes.data.assessments.grading.RubricGradeData;
import org.prosolo.services.nodes.impl.util.EditMode;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-24
 * @since 1.0.0
 */
public interface RubricManager extends AbstractManager {

    Rubric createNewRubric(String name, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    Result<Rubric> createNewRubricAndGetEvents(String name, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    PaginatedResult<RubricData> getRubrics(int page, int limit,long organizationId)
            throws DbConnectionException;

    List<Rubric> getAllRubrics (long orgId, Session session) throws DbConnectionException;

    void deleteRubric(long rubricId,UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    Result<Void> deleteRubricAndGetEvents(long rubricId, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    RubricData getOrganizationRubric(long rubricId);

    /**
     * Returns rubric data.
     *
     *
     * @param rubricId
     * @param loadCreator
     * @param loadItems
     * @param userId if greater than 0, rubric data is returned only if id of a rubric creator equals this parameter value
     * @param trackChanges
     * @return
     * @throws DbConnectionException
     */
    RubricData getRubricData(long rubricId, boolean loadCreator, boolean loadItems, long userId, boolean trackChanges, boolean loadRubricUsed)
            throws DbConnectionException;

    void saveRubricCriteriaAndLevels(RubricData rubric, EditMode editMode)
            throws DbConnectionException, OperationForbiddenException;

    List<RubricData> getPreparedRubricsFromUnits(List<Long> unitIds) throws DbConnectionException;

    boolean isRubricUsed(long rubricId) throws DbConnectionException;

    boolean isRubricReadyToUse(long rubricId) throws DbConnectionException;

    void updateRubricName(long rubricId, String name, UserContextData context) throws
            DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    Result<Void> updateRubricNameAndGetEvents(long rubricId, String name, UserContextData context) throws
            DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    String getRubricName(long id) throws DbConnectionException;

    RubricCriteriaGradeData getRubricDataForActivity(long actId, long activityAssessmentId, boolean loadGrades)
            throws DbConnectionException;

}
