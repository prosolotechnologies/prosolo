package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.organization.UnitRoleMembership;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.UnitData;
import org.prosolo.services.nodes.data.UnitRoleMembershipData;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

/**
 * @author Bojan
 * @date 2017-07-04
 * @since 0.7
 */
public interface UnitManager extends AbstractManager{

    UnitData createNewUnit(String title,long organizationId,long creatorId, LearningContextData contextData)
            throws DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException;

    Result<Unit> createNewUnitAndGetEvents(String title,long organizationId,long creatorId,
                                           LearningContextData contextData) throws DbConnectionException,ConstraintViolationException, DataIntegrityViolationException;

    List<UnitData> getUnitsWithSubUnits(long organizationId);

    UnitRoleMembership addUserToUnitWithRole(long userId, long unitId, long roleId, long actorId,
                               LearningContextData context) throws DbConnectionException, EventException;

    Result<UnitRoleMembership> addUserToUnitWithRoleAndGetEvents(long userId, long unitId, long roleId, long actorId,
                                                                 LearningContextData context) throws DbConnectionException;

    void removeUserFromUnitWithRole(UnitRoleMembershipData unitRoleMembership, long actorId,
                                    LearningContextData context) throws DbConnectionException, EventException;

    Result<Void> removeUserFromUnitWithRoleAndGetEvents(UnitRoleMembershipData unitRoleMembership,
                                                        long actorId, LearningContextData context)
            throws DbConnectionException;

    List<Unit> getAllUnitsWithUserInARole(long userId, long roleId, Session session) throws DbConnectionException;

}
