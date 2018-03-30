package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.TitleData;
import org.prosolo.services.nodes.data.UnitData;
import org.prosolo.services.nodes.data.UserData;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

/**
 * @author Bojan
 * @date 2017-07-04
 * @since 1.0.0
 */
public interface UnitManager extends AbstractManager{

    UnitData createNewUnit(String title,long organizationId,long parentUnitId, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    Result<Unit> createNewUnitAndGetEvents(String title,long organizationId,long parentUnitId, UserContextData context)
            throws DbConnectionException,ConstraintViolationException, DataIntegrityViolationException;

    List<UnitData> getUnitsWithSubUnits(long organizationId);

    Result<Void> addUserToUnitWithRoleAndGetEvents(long userId, long unitId, long roleId,
                                                                 UserContextData context) throws DbConnectionException;

    void addUserToUnitWithRole(long userId, long unitId, long roleId, UserContextData context) throws DbConnectionException;

    Result<Void> addUserToUnitAndGroupWithRoleAndGetEvents(long userId, long unitId, long roleId, long groupId, UserContextData context);

    void addUserToUnitAndGroupWithRole(long userId, long unitId, long roleId, long groupId, UserContextData context);

    void removeUserFromUnitWithRole(long userId, long unitId, long roleId, UserContextData context) throws DbConnectionException;

    Result<Void> removeUserFromUnitWithRoleAndGetEvents(long userId, long unitId, long roleId, UserContextData context)
            throws DbConnectionException;

    void removeUserFromAllUnitsWithRole(long userId, long roleId, UserContextData context) throws DbConnectionException;

    Result<Void> removeUserFromAllUnitsWithRoleAndGetEvents(long userId, long roleId, UserContextData context)
            throws DbConnectionException;

    List<Unit> getAllUnitsWithUserInARole(long userId, long roleId, Session session) throws DbConnectionException;

    UnitData getUnitData(long unitId) throws DbConnectionException;

    Unit updateUnit(long unitId,String title, UserContextData context) throws
            DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    Result<Unit> updateUnitAndGetEvents(long unitId,String title, UserContextData context) throws
            DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    String getUnitTitle(long organizationId, long unitId) throws DbConnectionException;

    TitleData getOrganizationAndUnitTitle(long organizationId, long unitId) throws DbConnectionException;

    PaginatedResult<UserData> getPaginatedUnitUsersInRole(long unitId, long roleId,
                                                          int offset, int limit)
            throws DbConnectionException;

    PaginatedResult<UserData> getPaginatedUnitUsersInRoleNotAddedToGroup(long unitId, long roleId,
                                                                         long groupId, int offset, int limit)
            throws DbConnectionException;

    void deleteUnit(long unitId) throws DbConnectionException;

    boolean isUserAddedToUnitWithRole(long unitId, long userId, long roleId) throws DbConnectionException;

    /**
     * Returns all organization units (root units with subunits and subunits with their subunits)
     * and information if credential is added to unit.
     *
     * @param organizationId
     * @param credId
     * @return
     * @throws DbConnectionException
     */
    List<UnitData> getUnitsWithCredentialSelectionInfo(long organizationId, long credId) throws DbConnectionException;

    Result<Void> addCredentialToUnitAndGetEvents(long credId, long unitId, UserContextData context)
            throws DbConnectionException;

    void addCredentialToUnit(long credId, long unitId, UserContextData context)
            throws DbConnectionException;

    Result<Void> removeCredentialFromUnitAndGetEvents(long credId, long unitId, UserContextData context)
            throws DbConnectionException;

    void removeCredentialFromUnit(long credId, long unitId, UserContextData context)
            throws DbConnectionException;

    List<Long> getAllUnitIdsCredentialIsConnectedTo(long credId, Session session)
            throws DbConnectionException;

    List<UnitData> getUnitsWithCompetenceSelectionInfo(long organizationId, long compId)
            throws DbConnectionException;

    void addCompetenceToUnit(long compId, long unitId, UserContextData context)
            throws DbConnectionException;

    Result<Void> addCompetenceToUnitAndGetEvents(long compId, long unitId, UserContextData context)
            throws DbConnectionException;

    void removeCompetenceFromUnit(long compId, long unitId, UserContextData context)
            throws DbConnectionException;

    Result<Void> removeCompetenceFromUnitAndGetEvents(long compId, long unitId, UserContextData context)
            throws DbConnectionException;

    List<Long> getAllUnitIdsCompetenceIsConnectedTo(long compId, Session session) throws DbConnectionException;

    List<Long> getAllUnitIdsCredentialIsConnectedTo(long credId) throws DbConnectionException;

    List<Long> getAllUnitIdsCompetenceIsConnectedTo(long compId) throws DbConnectionException;

    boolean checkIfUserHasRoleInUnitsConnectedToCredential(long userId, long credId, long roleId)
            throws DbConnectionException;

    boolean checkIfUserHasRoleInUnitsConnectedToCompetence(long userId, long compId, long roleId)
            throws DbConnectionException;

    boolean checkIfUserHasRoleInUnitsConnectedToCompetence(long userId, long compId, String roleName)
            throws DbConnectionException;

    List<Long> getUserUnitIdsInRole(long userId, long roleId) throws DbConnectionException;

    List<Long> getUserUnitIdsInRole(long userId, String role) throws DbConnectionException;

    List<Long> getUserUnitIdsWithUserCapability(long userId, String capability) throws DbConnectionException;

    List<UnitData> getUnitsWithRubricSelectionInfo(long organizationId, long rubricId)
            throws DbConnectionException;

    void addRubricToUnit(long rubricId, long unitId, UserContextData context)
            throws DbConnectionException;

    Result<Void> addRubricToUnitAndGetEvents(long rubricId, long unitId, UserContextData context)
            throws DbConnectionException;

    void removeRubricFromUnit(long rubricId, long unitId, UserContextData context)
            throws DbConnectionException;

    Result<Void> removeRubricFromUnitAndGetEvents(long rubricId, long unitId, UserContextData context)
            throws DbConnectionException;

    boolean isCredentialConnectedToUnit(long credId, long unitId, CredentialType type) throws DbConnectionException;

    boolean isCompetenceConnectedToUnit(long compId, long unitId) throws DbConnectionException;

    boolean isCredentialConnectedToUnit(long credId, long unitId) throws DbConnectionException;

}