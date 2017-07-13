package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.organization.UnitRoleMembership;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UnitData;
import org.prosolo.services.nodes.data.UnitRoleMembershipData;
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

    @Override
    public UnitData createNewUnit(String title, long organizationId, long creatorId, LearningContextData contextData)
            throws DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException {

        Result<Unit> res = self.createNewUnitAndGetEvents(title,organizationId,creatorId,contextData);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
        return new UnitData(res.getResult());
    }

    @Override
    @Transactional
    public Result<Unit> createNewUnitAndGetEvents(String title, long organizationId, long creatorId,
                                                  LearningContextData contextData)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException{
        try{
            Result<Unit> res = new Result<>();
            Organization organization = new Organization();
            organization.setId(organizationId);
            Unit unit = new Unit();
            unit.setTitle(title);
            unit.setOrganization(organization);
            unit.setParentUnit(null);
            saveEntity(unit);

            res.addEvent(eventFactory.generateEventData(EventType.Create,creatorId,unit,null,contextData,null));
            res.setResult(unit);

            return res;
        }catch (ConstraintViolationException | DataIntegrityViolationException e){
            logger.error(e);
            e.printStackTrace();
            throw e;
        }catch (Exception e){
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
                .setParameter("organizationId",organizationId)
                .list();

        if(result != null){
            return result;
        }

        return new ArrayList<>();
    }

    @Override
    public List<UnitData>  getUnitsWithSubUnits(long organizationId) {

        List<Unit> units = getOrganizationUnits(organizationId);
        Map<Long,List<UnitData>> childUnits = new HashMap<>();
        Map<Long,UnitData> parentUnits = new HashMap<>();
        List<UnitData> unitsToReturn = new ArrayList<>();

        for(Unit u : units){
            UnitData ud = new UnitData(u);
            parentUnits.put(u.getId(),ud);
            if(u.getParentUnit() == null){
                unitsToReturn.add(ud);
            }else{
                List<UnitData> children = childUnits.get(u.getParentUnit().getId());
                if(children != null) {
                    children.add(ud);
                }else {
                    List<UnitData> children1 = new ArrayList<>();
                    children1.add(ud);
                    childUnits.put(u.getParentUnit().getId(), children1);
                }
            }
        }
        for (Map.Entry<Long,List<UnitData>> entry : childUnits.entrySet()){
            parentUnits.get(entry.getKey()).addChildren(entry.getValue());
        }

        return unitsToReturn;
    }

    private List<UnitRoleMembershipData> getUnitUsersInRole(long organizationId, long unitId, long roleId,
                                                           int offset, int limit) {

        String query =
                "SELECT urm " +
                "FROM UnitRoleMembership urm " +
                "INNER JOIN urm.unit unit " +
                        "WITH unit.id = :unitId AND unit.organization.id = :orgId AND unit.deleted IS FALSE " +
                "INNER JOIN fetch urm.user user " +
                        "WITH user.deleted IS FALSE " +
                "WHERE urm.role.id = :roleId " +
                "ORDER BY user.name ASC, user.lastname ASC";

        List<UnitRoleMembership> result = persistence.currentManager()
                .createQuery(query)
                .setLong("orgId", organizationId)
                .setLong("unitId", unitId)
                .setLong("roleId", roleId)
                .setMaxResults(limit)
                .setFirstResult(offset)
                .list();

        List<UnitRoleMembershipData> unitRoleMemberships = new ArrayList<>();
        for (UnitRoleMembership urm : result) {
            unitRoleMemberships.add(new UnitRoleMembershipData(
                    urm.getId(), urm.getUnit().getId(), urm.getRole().getId(), urm.getUser()));
        }

        return unitRoleMemberships;
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
    public void removeUserFromUnitWithRole(UnitRoleMembershipData unitRoleMembership, long actorId,
                                           LearningContextData context) throws DbConnectionException, EventException {
        Result<Void> res = self.removeUserFromUnitWithRoleAndGetEvents(unitRoleMembership, actorId, context);

        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
    }

    @Override
    @Transactional
    public Result<Void> removeUserFromUnitWithRoleAndGetEvents(UnitRoleMembershipData unitRoleMembership,
                                                               long actorId, LearningContextData context)
            throws DbConnectionException {
        try {
            UnitRoleMembership urmToDelete = (UnitRoleMembership) persistence.currentManager()
                    .load(UnitRoleMembership.class, unitRoleMembership.getId());
            delete(urmToDelete);

            User user = new User(unitRoleMembership.getUser().getId());
            Unit unit = new Unit();
            unit.setId(unitRoleMembership.getUnitId());
            Result<Void> result = new Result<>();
            Map<String, String> params = new HashMap<>();
            params.put("roleId", unitRoleMembership.getRoleId() + "");
            result.addEvent(eventFactory.generateEventData(
                    EventType.REMOVE_USER_FROM_UNIT, actorId, user, unit, context, params));

            return result;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while removing user from unit");
        }
    }
}
