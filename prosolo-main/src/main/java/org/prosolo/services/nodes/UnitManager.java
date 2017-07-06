package org.prosolo.services.nodes;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;

/**
 * @author Bojan
 * @date 2017-07-04
 * @since 0.7
 */
public interface UnitManager extends AbstractManager{

    Unit createNewUnit(String title,long organizationId,long creatorId, LearningContextData contextData)
            throws DbConnectionException, EventException, IllegalDataStateException;

    Result<Unit> createNewUnitAndGetEvents(String title,long organizationId,long creatorId,
                                           LearningContextData contextData) throws DbConnectionException,IllegalDataStateException;

}
