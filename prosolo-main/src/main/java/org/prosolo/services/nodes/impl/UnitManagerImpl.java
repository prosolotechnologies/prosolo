package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.event.context.data.UserContextData;
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

    public UnitData createNewUnit(String title, long organizationId,long parentUnitId, UserContextData context)
            throws DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException {

        Result<Unit> res = self.createNewUnitAndGetEvents(title, organizationId, parentUnitId, context);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
        return new UnitData(res.getResult(),parentUnitId);
    }

    @Override
    @Transactional
    public Result<Unit> createNewUnitAndGetEvents(String title, long organizationId, long parentUnitId, UserContextData context)
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

            res.addEvent(eventFactory.generateEventData(EventType.Create, context.getActorId(), context.getOrganizationId(), context.getSessionId(),
                    unit, null, context.getContext(), null));
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

    private List<UnitData> getOrganizationUnits(long organizationId) {

        String query =
                "SELECT unit, COUNT(memb) " +
                "FROM Unit unit " +
                "LEFT JOIN unit.unitRoleMemberships memb " +
                "WHERE unit.deleted IS FALSE " +
                "AND unit.organization.id = :organizationId " +
                "GROUP BY unit " +
                "ORDER BY unit.title ASC ";

        List<Object[]> result =  persistence.currentManager()
                .createQuery(query)
                .setParameter("organizationId", organizationId)
                .list();

        UnitData unitData;
        List<UnitData> resultList = new ArrayList<>();

        for (Object[] res : result) {
            Unit unit = (Unit) res[0];
            long count = (long) res[1];

            if(unit.getParentUnit() != null) {
                unitData = new UnitData(unit, unit.getParentUnit().getId());
            }else {
                unitData = new UnitData(unit);
            }
            unitData.setHasUsers(count > 0);
            resultList.add(unitData);
        }

        if (result != null) {
            return resultList;
        }

        return new ArrayList<>();
    }

    @Override
    public List<UnitData> getUnitsWithSubUnits(long organizationId) {
        List<UnitData> units = getOrganizationUnits(organizationId);
        return getRootUnitsWithSubunits(units);
    }

    /**
     * Returns list of root units with child units (and their child units) mapped.
     *
     * @param units
     * @return
     */
    private List<UnitData> getRootUnitsWithSubunits(List<UnitData> units) {
        Map<Long, List<UnitData>> childUnits = new HashMap<>();
        Map<Long, UnitData> parentUnits = new HashMap<>();
        List<UnitData> unitsToReturn = new ArrayList<>();

        for (UnitData u : units) {
            parentUnits.put(u.getId(), u);
            if (u.getParentUnitId() == 0) {
                unitsToReturn.add(u);
            } else {
                List<UnitData> children = childUnits.get(u.getParentUnitId());
                if (children != null) {
                    children.add(u);
                } else {
                    List<UnitData> children1 = new ArrayList<>();
                    children1.add(u);
                    childUnits.put(u.getParentUnitId(), children1);
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
    public void addUserToUnitWithRole(long userId, long unitId, long roleId, UserContextData context)
        throws DbConnectionException, EventException {
        Result<Void> res = self.addUserToUnitWithRoleAndGetEvents(userId, unitId, roleId, context);

        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
    }

    @Override
    @Transactional
    public Result<Void> addUserToUnitWithRoleAndGetEvents(long userId, long unitId, long roleId, UserContextData context) throws DbConnectionException {
        Result<Void> result = new Result<>();
        try {
            UnitRoleMembership urm = new UnitRoleMembership();
            urm.setUser((User) persistence.currentManager().load(User.class, userId));
            urm.setUnit((Unit) persistence.currentManager().load(Unit.class, unitId));
            urm.setRole((Role) persistence.currentManager().load(Role.class, roleId));

            saveEntity(urm);
            persistence.currentManager().flush();

            User user = new User(userId);
            Unit unit = new Unit();
            unit.setId(unitId);
            Map<String, String> params = new HashMap<>();
            params.put("roleId", roleId + "");
            result.addEvent(eventFactory.generateEventData(
                    EventType.ADD_USER_TO_UNIT, context.getActorId(), context.getOrganizationId(), context.getSessionId(), user, unit,
                    context.getContext(), params));

            return result;
        } catch (ConstraintViolationException|DataIntegrityViolationException e) {
            logger.info("User (" + userId + ") not added to unit (" + unitId + ") with role (" + roleId + ") because he is already added");
            return result;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while adding user to unit");
        }
    }


    @Override
    //nt
    public void removeUserFromUnitWithRole(long userId, long unitId, long roleId, UserContextData context)
            throws DbConnectionException, EventException {
        Result<Void> res = self.removeUserFromUnitWithRoleAndGetEvents(userId, unitId, roleId, context);

        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
    }

    @Override
    @Transactional
    public Result<Void> removeUserFromUnitWithRoleAndGetEvents(long userId, long unitId, long roleId, UserContextData context)
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
                    EventType.REMOVE_USER_FROM_UNIT, context.getActorId(), context.getOrganizationId(), context.getSessionId(), user, unit,
                    context.getContext(), params));

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
    public Unit updateUnit(long unitId, String title, UserContextData context)
            throws DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException {

        Result<Unit> res = self.updateUnitAndGetEvents(unitId, title, context);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
        return res.getResult();
    }

    @Override
    public Result<Unit> updateUnitAndGetEvents(long unitId, String title, UserContextData context)
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

            res.addEvent(eventFactory.generateEventData(EventType.Edit, context.getActorId(), context.getOrganizationId(), context.getSessionId(),
                    unit, null, context.getContext(), null));
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

    public void deleteUnit(long unitId) throws DbConnectionException {
        try {
            String query =
                    "SELECT COUNT(unit) " +
                    "FROM Unit unit " +
                    "WHERE unit.parentUnit.id = :unitId";

            Long numberOfSubunits = (Long) persistence.currentManager()
                    .createQuery(query)
                    .setLong("unitId", unitId)
                    .uniqueResult();

            if (numberOfSubunits != 0 ) {
                throw new IllegalStateException("Unit can not be deleted since it has subunits");
            }

            String query1 =
                    "SELECT COUNT(unit_role_membership) " +
                    "FROM UnitRoleMembership unit_role_membership " +
                    "WHERE unit_role_membership.unit.id = :unitId ";

            Long numberOfUsers = (Long) persistence.currentManager()
                    .createQuery(query1)
                    .setLong("unitId", unitId)
                    .uniqueResult();

            if(numberOfUsers != 0){
                throw new IllegalStateException("Unit can not be deleted as there are users associated with it");
            }

            String deleteQuery =
                    "DELETE FROM Unit unit " +
                    "WHERE unit.id = :unitId ";

            int affected = persistence.currentManager()
                    .createQuery(deleteQuery)
                    .setLong("unitId", unitId)
                    .executeUpdate();

            logger.info("Deleted unit : " + affected);

        }catch (IllegalStateException ise){
            throw ise;
        }catch(Exception e) {
            e.printStackTrace();
            logger.error(e);
            throw new DbConnectionException("Error while trying to retrieve units");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserAddedToUnitWithRole(long unitId, long userId, long roleId) throws DbConnectionException {
        try {
            String query =
                    "SELECT urm.id FROM UnitRoleMembership urm " +
                    "WHERE urm.unit.id = :unitId " +
                    "AND urm.user.id = :userId " +
                    "AND urm.role.id = :roleId";

            return persistence.currentManager()
                    .createQuery(query)
                    .setLong("unitId", unitId)
                    .setLong("userId", userId)
                    .setLong("roleId", roleId)
                    .uniqueResult() != null;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving user data");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitData> getUnitsWithCredentialSelectionInfo(long organizationId, long credId)
            throws DbConnectionException {
        try {
            String query =
                    "SELECT u, CASE WHEN c IS NULL THEN false ELSE true END FROM Unit u " +
                    "LEFT JOIN u.credentialUnits c " +
                            "WITH c.credential.id = :credId " +
                    "WHERE u.organization.id = :orgId";

            List<Object[]> res = persistence.currentManager()
                    .createQuery(query)
                    .setLong("orgId", organizationId)
                    .setLong("credId", credId)
                    .list();

            List<UnitData> units = new ArrayList<>();

            for (Object[] row : res) {
                Unit u = (Unit) row[0];
                boolean selected = (boolean) row[1];
                units.add(new UnitData(u, selected));
            }

            return getRootUnitsWithSubunits(units);
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving unit data");
        }
    }

    @Override
    //nt
    public void addCredentialToUnit(long credId, long unitId, UserContextData context)
            throws DbConnectionException, EventException {
        Result<Void> res = self.addCredentialToUnitAndGetEvents(credId, unitId, context);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
    }

    @Override
    @Transactional
    public Result<Void> addCredentialToUnitAndGetEvents(long credId, long unitId, UserContextData context)
            throws DbConnectionException {
        Result<Void> res = new Result<>();
        try {
            CredentialUnit cu = new CredentialUnit();
            cu.setCredential((Credential1) persistence.currentManager().load(Credential1.class, credId));
            cu.setUnit((Unit) persistence.currentManager().load(Unit.class, unitId));
            saveEntity(cu);

            Credential1 cr = new Credential1();
            cr.setId(credId);
            Unit un = new Unit();
            un.setId(unitId);
            res.addEvent(eventFactory.generateEventData(
                    EventType.ADD_CREDENTIAL_TO_UNIT, context.getActorId(),
                    context.getOrganizationId(), context.getSessionId(), cr, un,
                    context.getContext(), null));
        } catch (ConstraintViolationException|DataIntegrityViolationException e) {
            logger.info("Credential (" + credId + ") already added to the unit (" + unitId + ") so it can't be added again");
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while adding credential to unit");
        }

        return res;
    }

    @Override
    //nt
    public void removeCredentialFromUnit(long credId, long unitId, UserContextData context)
            throws DbConnectionException, EventException {
        Result<Void> res = self.removeCredentialFromUnitAndGetEvents(credId, unitId, context);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
    }

    @Override
    @Transactional
    public Result<Void> removeCredentialFromUnitAndGetEvents(long credId, long unitId, UserContextData context)
            throws DbConnectionException {
        Result<Void> res = new Result<>();
        try {
            String query = "DELETE FROM CredentialUnit cu " +
                    "WHERE cu.unit.id = :unitId " +
                    "AND cu.credential.id = :credId";

            int affected = persistence.currentManager()
                    .createQuery(query)
                    .setLong("unitId", unitId)
                    .setLong("credId", credId)
                    .executeUpdate();

            logger.info("Number of removed credentials in a unit: " + affected);

            if (affected > 0) {
                Credential1 cr = new Credential1();
                cr.setId(credId);
                Unit un = new Unit();
                un.setId(unitId);
                res.addEvent(eventFactory.generateEventData(
                        EventType.REMOVE_CREDENTIAL_FROM_UNIT, context.getActorId(),
                        context.getOrganizationId(), context.getSessionId(), cr, un,
                        context.getContext(), null));
            }
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while removing credential from unit");
        }

        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getAllUnitIdsCredentialIsConnectedTo(long credId)
            throws DbConnectionException {
        return getAllUnitIdsCredentialIsConnectedTo(credId, persistence.currentManager());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getAllUnitIdsCredentialIsConnectedTo(long credId, Session session)
            throws DbConnectionException {
        try {
            String query =
                    "SELECT cu.unit.id FROM CredentialUnit cu " +
                    "WHERE cu.credential.id = :credId";

            @SuppressWarnings("unchecked")
            List<Long> res = session
                    .createQuery(query)
                    .setLong("credId", credId)
                    .list();

            return res;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving units");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitData> getUnitsWithCompetenceSelectionInfo(long organizationId, long compId)
            throws DbConnectionException {
        try {
            String query =
                    "SELECT u, CASE WHEN c IS NULL THEN false ELSE true END FROM Unit u " +
                            "LEFT JOIN u.competenceUnits c " +
                            "WITH c.competence.id = :compId " +
                            "WHERE u.organization.id = :orgId";

            List<Object[]> res = persistence.currentManager()
                    .createQuery(query)
                    .setLong("orgId", organizationId)
                    .setLong("compId", compId)
                    .list();

            List<UnitData> units = new ArrayList<>();

            for (Object[] row : res) {
                Unit u = (Unit) row[0];
                boolean selected = (boolean) row[1];
                units.add(new UnitData(u, selected));
            }

            return getRootUnitsWithSubunits(units);
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving unit data");
        }
    }

    @Override
    //nt
    public void addCompetenceToUnit(long compId, long unitId, UserContextData context)
            throws DbConnectionException, EventException {
        Result<Void> res = self.addCompetenceToUnitAndGetEvents(compId, unitId, context);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
    }

    @Override
    @Transactional
    public Result<Void> addCompetenceToUnitAndGetEvents(long compId, long unitId, UserContextData context)
            throws DbConnectionException {
        Result<Void> res = new Result<>();
        try {
            CompetenceUnit cu = new CompetenceUnit();
            cu.setCompetence((Competence1) persistence.currentManager().load(Competence1.class, compId));
            cu.setUnit((Unit) persistence.currentManager().load(Unit.class, unitId));
            saveEntity(cu);

            Competence1 comp = new Competence1();
            comp.setId(compId);
            Unit un = new Unit();
            un.setId(unitId);
            res.addEvent(eventFactory.generateEventData(
                    EventType.ADD_COMPETENCE_TO_UNIT, context.getActorId(),
                    context.getOrganizationId(), context.getSessionId(), comp, un,
                    context.getContext(), null));
        } catch (ConstraintViolationException|DataIntegrityViolationException e) {
            logger.info("Competency (" + compId + ") already added to the unit (" + unitId + ") so it can't be added again");
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while adding competency to unit");
        }

        return res;
    }

    @Override
    //nt
    public void removeCompetenceFromUnit(long compId, long unitId, UserContextData context)
            throws DbConnectionException, EventException {
        Result<Void> res = self.removeCompetenceFromUnitAndGetEvents(compId, unitId, context);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
    }

    @Override
    @Transactional
    public Result<Void> removeCompetenceFromUnitAndGetEvents(long compId, long unitId, UserContextData context)
            throws DbConnectionException {
        Result<Void> res = new Result<>();
        try {
            String query = "DELETE FROM CompetenceUnit cu " +
                    "WHERE cu.unit.id = :unitId " +
                    "AND cu.competence.id = :compId";

            int affected = persistence.currentManager()
                    .createQuery(query)
                    .setLong("unitId", unitId)
                    .setLong("compId", compId)
                    .executeUpdate();

            logger.info("Number of removed competencies from unit: " + affected);

            if (affected > 0) {
                Competence1 comp = new Competence1();
                comp.setId(compId);
                Unit un = new Unit();
                un.setId(unitId);
                res.addEvent(eventFactory.generateEventData(
                        EventType.REMOVE_COMPETENCE_FROM_UNIT, context.getActorId(),
                        context.getOrganizationId(), context.getSessionId(), comp, un,
                        context.getContext(), null));
            }
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while removing competency from unit");
        }

        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getAllUnitIdsCompetenceIsConnectedTo(long compId, Session session)
            throws DbConnectionException {
        try {
            String query =
                    "SELECT cu.unit.id FROM CompetenceUnit cu " +
                    "WHERE cu.competence.id = :compId";

            @SuppressWarnings("unchecked")
            List<Long> res = session
                    .createQuery(query)
                    .setLong("compId", compId)
                    .list();

            return res;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving units");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getAllUnitIdsCompetenceIsConnectedTo(long compId)
            throws DbConnectionException {
        return getAllUnitIdsCompetenceIsConnectedTo(compId, persistence.currentManager());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkIfUserHasRoleInUnitsConnectedToCredential(long userId, long credId, long roleId)
            throws DbConnectionException {
        List<Long> unitIds = getAllUnitIdsCredentialIsConnectedTo(credId);

        return checkIfUserHasRoleInAtLeastOneOfTheUnits(userId, roleId, unitIds);

    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkIfUserHasRoleInUnitsConnectedToCompetence(long userId, long compId, long roleId)
            throws DbConnectionException {
        List<Long> unitIds = getAllUnitIdsCompetenceIsConnectedTo(compId);

        return checkIfUserHasRoleInAtLeastOneOfTheUnits(userId, roleId, unitIds);
    }

    private boolean checkIfUserHasRoleInAtLeastOneOfTheUnits(long userId, long roleId, List<Long> unitIds) {
        if (unitIds == null || unitIds.isEmpty()) {
            return false;
        }

        String query =
                "SELECT COUNT(urm) " +
                "FROM UnitRoleMembership urm " +
                "WHERE urm.role.id = :roleId " +
                "AND urm.unit.id IN (:unitIds) " +
                "AND urm.user.id = :userId";

        return (long) persistence.currentManager()
                .createQuery(query)
                .setParameterList("unitIds", unitIds)
                .setLong("roleId", roleId)
                .setLong("userId", userId)
                .uniqueResult() > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserUnitIdsInRole(long userId, long roleId) throws DbConnectionException {
        try {
            String query =
                    "SELECT urm.unit.id " +
                            "FROM UnitRoleMembership urm " +
                            "WHERE urm.role.id = :roleId " +
                            "AND urm.user.id = :userId";

            @SuppressWarnings("unchecked")
            List<Long> result = persistence.currentManager()
                    .createQuery(query)
                    .setLong("userId", userId)
                    .setLong("roleId", roleId)
                    .list();

            return result;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving user units");
        }
    }

}
