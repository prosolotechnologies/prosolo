package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.organization.UnitRoleMembership;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.UnitData;
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

    @Transactional
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
                    .setString("title",title)
                    .setParameter("unitId",unitId)
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

}
