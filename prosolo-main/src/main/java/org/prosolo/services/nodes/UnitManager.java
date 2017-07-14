package org.prosolo.services.nodes;

import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.UnitData;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

/**
 * @author Bojan
 * @date 2017-07-04
 * @since 0.7
 */
public interface UnitManager extends AbstractManager{

    UnitData createNewUnit(String title,long organizationId,long parentUnitId, long creatorId, LearningContextData contextData)
            throws DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException;

    Result<Unit> createNewUnitAndGetEvents(String title,long organizationId,long parentUnitId, long creatorId,
                                           LearningContextData contextData) throws DbConnectionException,ConstraintViolationException, DataIntegrityViolationException;

    List<UnitData> getUnitsWithSubUnits(long organizationId);

    UnitData getUnitData(long unitId) throws DbConnectionException;

    Unit updateUnit(long unitId,String title, long creatorId,LearningContextData contextData) throws
            DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException;

    Result<Unit> updateUnitAndGetEvents(long unitId,String title, long creatorId,LearningContextData contextData) throws
            DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException;

}
