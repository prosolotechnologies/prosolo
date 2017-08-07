package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.organization.UnitRoleMembership;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.TitleData;
import org.prosolo.services.nodes.data.UnitData;
import org.prosolo.services.nodes.data.UnitRoleMembershipData;
import org.prosolo.services.nodes.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bojan
 * @date 2017-07-04
 * @since 0.7
 */

@Service("org.prosolo.services.nodes.UnitManager")
public class UnitManagerImpl extends AbstractManagerImpl implements UnitManager {

    private static Logger logger = Logger.getLogger(UnitManagerImpl.class);

    @Autowired
    private EventFactory eventFactory;
    @Inject
    private OrganizationManager organizationManager;
    @Inject
    private UnitManager self;
    @Inject
    private RoleManager roleManager;

    public UnitData createNewUnit(String title, long organizationId,long parentUnitId, long creatorId, LearningContextData contextData)
            throws DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException {

        Result<Unit> res = self.createNewUnitAndGetEvents(title, organizationId, parentUnitId, creatorId, contextData);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
        return new UnitData(res.getResult(),parentUnitId);
    }

    @Override
    @Transactional
    public Result<Unit> createNewUnitAndGetEvents(String title, long organizationId, long parentUnitId, long creatorId,
                                                  LearningContextData contextData)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        try {
            Result<Unit> res = new Result<>();
            Organization organization = new Organization();
            organization.setId(organizationId);
            Unit unit = new Unit();
            unit.setTitle(title);
            unit.setOrganization(organization);

            if(parentUnitId == 0) {
                unit.setParentUnit(null);
            }else{
                unit.setParentUnit(loadResource(Unit.class,parentUnitId));
            }
            saveEntity(unit);

            res.addEvent(eventFactory.generateEventData(EventType.Create, creatorId, unit, null, contextData, null));
            res.setResult(unit);

            return res;
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error(e);
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while saving organization unit");
        }
    }

    private List<Unit> getOrganizationUnits(long organizationId) {

        String query =
                "SELECT unit " +
                "FROM Unit unit " +
                "WHERE unit.deleted IS FALSE " +
                "AND unit.organization.id = :organizationId " +
                "ORDER BY unit.title ASC ";

        List<Unit> result = persistence.currentManager()
                .createQuery(query)
                .setParameter("organizationId", organizationId)
                .list();

        if (result != null) {
            return result;
        }

        return new ArrayList<>();
    }

    @Override
    public List<UnitData> getUnitsWithSubUnits(long organizationId) {

        List<Unit> units = getOrganizationUnits(organizationId);
        Map<Long, List<UnitData>> childUnits = new HashMap<>();
        Map<Long, UnitData> parentUnits = new HashMap<>();
        List<UnitData> unitsToReturn = new ArrayList<>();

        for (Unit u : units) {
            UnitData ud = new UnitData(u);
            parentUnits.put(u.getId(), ud);
            if (u.getParentUnit() == null) {
                unitsToReturn.add(ud);
            } else {
                List<UnitData> children = childUnits.get(u.getParentUnit().getId());
                if (children != null) {
                    children.add(ud);
                } else {
                    List<UnitData> children1 = new ArrayList<>();
                    children1.add(ud);
                    childUnits.put(u.getParentUnit().getId(), children1);
                }
            }
        }
        for (Map.Entry<Long, List<UnitData>> entry : childUnits.entrySet()) {
            parentUnits.get(entry.getKey()).addChildren(entry.getValue());
        }

        return unitsToReturn;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<UserData> getPaginatedUnitUsersInRole(long unitId, long roleId,
                                                                 int offset, int limit)
            throws DbConnectionException {
        try {
            PaginatedResult<UserData> res = new PaginatedResult<>();
            res.setHitsNumber(countUnitUsersInRole(unitId, roleId));
            if (res.getHitsNumber() > 0) {
                res.setFoundNodes(getUnitUsersInRole(unitId, roleId, offset, limit));
            }

            return res;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving unit users");
        }
    }

    private List<UserData> getUnitUsersInRole(long unitId, long roleId, int offset, int limit) {
        String query =
                "SELECT user " +
                "FROM UnitRoleMembership urm " +
                "INNER JOIN urm.user user " +
                    "WITH user.deleted IS FALSE " +
                "WHERE urm.role.id = :roleId " +
                "AND urm.unit.id = :unitId " +
                "ORDER BY user.lastname ASC, user.name ASC";

        List<User> result = persistence.currentManager()
                .createQuery(query)
                .setLong("unitId", unitId)
                .setLong("roleId", roleId)
                .setMaxResults(limit)
                .setFirstResult(offset)
                .list();

        List<UserData> res = new ArrayList<>();
        for (User u : result) {
            res.add(new UserData(u));
        }

        return res;
    }

    private long countUnitUsersInRole(long unitId, long roleId) {
        String query =
                "SELECT COUNT(urm) " +
                "FROM UnitRoleMembership urm " +
                "INNER JOIN urm.user user " +
                "WITH user.deleted IS FALSE " +
                "WHERE urm.role.id = :roleId " +
                "AND urm.unit.id = :unitId";

        return (long) persistence.currentManager()
                .createQuery(query)
                .setLong("unitId", unitId)
                .setLong("roleId", roleId)
                .uniqueResult();
    }

    @Override
    //nt
    public UnitRoleMembership addUserToUnitWithRole(long userId, long unitId, long roleId, long actorId,
                                      LearningContextData context) throws DbConnectionException, EventException {
        Result<UnitRoleMembership> res = self.addUserToUnitWithRoleAndGetEvents(userId, unitId, roleId, actorId, context);

        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }

        return res.getResult();
    }

    @Override
    @Transactional
    public Result<UnitRoleMembership> addUserToUnitWithRoleAndGetEvents(long userId, long unitId, long roleId, long actorId,
                                                                        LearningContextData context) throws DbConnectionException {
        try {
            UnitRoleMembership urm = new UnitRoleMembership();
            urm.setUser((User) persistence.currentManager().load(User.class, userId));
            urm.setUnit((Unit) persistence.currentManager().load(Unit.class, unitId));
            urm.setRole((Role) persistence.currentManager().load(Role.class, roleId));

            saveEntity(urm);

            User user = new User(userId);
            Unit unit = new Unit();
            unit.setId(unitId);
            Result<UnitRoleMembership> result = new Result<>();
            Map<String, String> params = new HashMap<>();
            params.put("roleId", roleId + "");
            result.addEvent(eventFactory.generateEventData(
                    EventType.ADD_USER_TO_UNIT, actorId, user, unit, context, params));

            result.setResult(urm);
            return result;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while adding user to unit");
        }
    }


    @Override
    //nt
    public void removeUserFromUnitWithRole(long userId, long unitId, long roleId, long actorId,
                                           LearningContextData context) throws DbConnectionException, EventException {
        Result<Void> res = self.removeUserFromUnitWithRoleAndGetEvents(userId, unitId, roleId, actorId, context);

        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
    }

    @Override
    @Transactional
    public Result<Void> removeUserFromUnitWithRoleAndGetEvents(long userId, long unitId, long roleId,
                                                               long actorId, LearningContextData context)
            throws DbConnectionException {
        try {
            String query = "DELETE FROM UnitRoleMembership urm " +
                           "WHERE urm.unit.id = :unitId " +
                           "AND urm.user.id = :userId " +
                           "AND urm.role.id = :roleId";

            int affected = persistence.currentManager()
                    .createQuery(query)
                    .setLong("unitId", unitId)
                    .setLong("userId", userId)
                    .setLong("roleId", roleId)
                    .executeUpdate();

            logger.info("Number of deleted users in a unit in a role: " + affected);

            User user = new User(userId);
            Unit unit = new Unit();
            unit.setId(unitId);
            Result<Void> result = new Result<>();
            Map<String, String> params = new HashMap<>();
            params.put("roleId", roleId + "");
            result.addEvent(eventFactory.generateEventData(
                    EventType.REMOVE_USER_FROM_UNIT, actorId, user, unit, context, params));

            return result;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while removing user from unit");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Unit> getAllUnitsWithUserInARole(long userId, long roleId, Session session) throws DbConnectionException {
        try {
            String query = "SELECT unit FROM UnitRoleMembership urm " +
                    "INNER JOIN urm.unit unit " +
                    "WHERE urm.user.id = :userId " +
                    "AND urm.role.id = :roleId";

            @SuppressWarnings("unchecked")
            List<Unit> units = persistence.currentManager()
                    .createQuery(query)
                    .setLong("userId", userId)
                    .setLong("roleId", roleId)
                    .list();

            return units;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving units");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UnitData getUnitData(long unitId) throws DbConnectionException {
        try {
            Unit unit = loadResource(Unit.class, unitId);

            return new UnitData(unit);
        } catch (ResourceCouldNotBeLoadedException e) {
            throw new DbConnectionException("Error while loading unit");
        }
    }

    @Override
    public Unit updateUnit(long unitId, String title, long creatorId, LearningContextData contextData)
            throws DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException {

        Result<Unit> res = self.updateUnitAndGetEvents(unitId, title, creatorId, contextData);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
        return res.getResult();
    }

    @Override
    public Result<Unit> updateUnitAndGetEvents(long unitId, String title, long creatorId, LearningContextData contextData)
            throws DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException {
        try {
            Result<Unit> res = new Result<>();
            Unit unit = new Unit();
            unit.setId(unitId);

            String query =
                    "UPDATE Unit unit " +
                            "SET unit.title = :title " +
                            "WHERE unit.id = :unitId ";

            persistence.currentManager()
                    .createQuery(query)
                    .setString("title", title)
                    .setParameter("unitId", unitId)
                    .executeUpdate();

            res.addEvent(eventFactory.generateEventData(EventType.Edit, creatorId, unit, null, contextData, null));
            res.setResult(unit);

            return res;
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error(e);
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while saving organization unit");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getUnitTitle(long organizationId, long unitId) throws DbConnectionException {
        try {
            String query = "SELECT unit.title FROM Unit unit " +
                    "WHERE unit.id = :unitId " +
                    "AND unit.organization.id = :orgId";

            return (String) persistence.currentManager()
                    .createQuery(query)
                    .setLong("unitId", unitId)
                    .setLong("orgId", organizationId)
                    .uniqueResult();
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving unit title");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TitleData getOrganizationAndUnitTitle(long organizationId, long unitId) throws DbConnectionException {
        try {
            String query = "SELECT org.title, unit.title FROM Unit unit " +
                    "INNER JOIN unit.organization org " +
                        "WITH org.id = :orgId " +
                    "WHERE unit.id = :unitId";

            Object[] res = (Object[]) persistence.currentManager()
                    .createQuery(query)
                    .setLong("unitId", unitId)
                    .setLong("orgId", organizationId)
                    .uniqueResult();

            return res != null
                    ? TitleData.ofOrganizationAndUnitTitle((String) res[0], (String) res[1])
                    : null;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving unit title");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<UserData> getPaginatedUnitUsersInRoleNotAddedToGroup(long unitId, long roleId,
                                                                 long groupId, int offset, int limit)
            throws DbConnectionException {
        try {
            PaginatedResult<UserData> res = new PaginatedResult<>();
            res.setHitsNumber(countUnitUsersInRoleNotAddedToGroup(unitId, roleId, groupId));
            if (res.getHitsNumber() > 0) {
                res.setFoundNodes(getUnitUsersInRoleNotAddedToGroup(unitId, roleId, groupId, offset, limit));
            }

            return res;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving unit users");
        }
    }

    private List<UserData> getUnitUsersInRoleNotAddedToGroup(long unitId, long roleId, long groupId,
                                                             int offset, int limit) {
        String query =
                "SELECT user " +
                "FROM UnitRoleMembership urm " +
                "INNER JOIN urm.user user " +
                "WITH user.deleted IS FALSE " +
                "LEFT JOIN user.groups userGroupUser " +
                    "WITH userGroupUser.group.id = :groupId " +
                "WHERE urm.role.id = :roleId " +
                "AND urm.unit.id = :unitId " +
                "AND userGroupUser IS NULL " +
                "ORDER BY user.lastname ASC, user.name ASC";

        List<User> result = persistence.currentManager()
                .createQuery(query)
                .setLong("unitId", unitId)
                .setLong("roleId", roleId)
                .setLong("groupId", groupId)
                .setMaxResults(limit)
                .setFirstResult(offset)
                .list();

        List<UserData> res = new ArrayList<>();
        for (User u : result) {
            res.add(new UserData(u));
        }

        return res;
    }

    private long countUnitUsersInRoleNotAddedToGroup(long unitId, long roleId, long groupId) {
        String query =
                "SELECT count(urm) " +
                "FROM UnitRoleMembership urm " +
                "INNER JOIN urm.user user " +
                "WITH user.deleted IS FALSE " +
                "LEFT JOIN user.groups userGroupUser " +
                "WITH userGroupUser.group.id = :groupId " +
                "WHERE urm.role.id = :roleId " +
                "AND urm.unit.id = :unitId " +
                "AND userGroupUser IS NULL";

        return (long) persistence.currentManager()
                .createQuery(query)
                .setLong("unitId", unitId)
                .setLong("roleId", roleId)
                .setLong("groupId", groupId)
                .uniqueResult();
    }

}
