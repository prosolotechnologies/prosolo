package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.RubricData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-24
 * @since 1.0.0
 */

@Service("org.prosolo.services.nodes.RubricManager")
public class RubricManagerImpl extends AbstractManagerImpl implements RubricManager {

    private static Logger logger = Logger.getLogger(RubricManagerImpl.class);

    @Autowired
    private EventFactory eventFactory;
    @Inject
    private RubricManager self;

    @Override
    public Rubric createNewRubric(String name, UserContextData context) throws DbConnectionException,
            EventException, ConstraintViolationException, DataIntegrityViolationException {

        Result<Rubric> res = self.createNewRubricAndGetEvents(name, context);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
        return res.getResult();
    }

    @Override
    @Transactional
    public Result<Rubric> createNewRubricAndGetEvents(String name, UserContextData context) throws DbConnectionException,
            ConstraintViolationException, DataIntegrityViolationException {
        try {
            Rubric rubric = new Rubric();
            User user = (User) persistence.currentManager().load(User.class,
                    context.getActorId());
            Organization organization = (Organization) persistence.currentManager().load(Organization.class,
                    context.getOrganizationId());

            rubric.setTitle(name);
            rubric.setCreator(user);
            rubric.setOrganization(organization);

            saveEntity(rubric);

            Result<Rubric> res = new Result<>();

            res.addEvent(eventFactory.generateEventData(
                    EventType.Create, context, rubric, null, null, null));

            res.setResult(rubric);
            return res;
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error(e);
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while saving rubric data");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<RubricData> getRubrics(int page, int limit, long organizationId) throws DbConnectionException {
        try {
            PaginatedResult<RubricData> response = new PaginatedResult<>();

            String query =
                    "SELECT  rubric " +
                    "FROM Rubric rubric " +
                    "LEFT JOIN FETCH rubric.creator " +
                    "WHERE rubric.organization =:organizationId " +
                    "AND rubric.deleted is FALSE " +
                    "ORDER BY rubric.title ASC";

            long rubricNumber = getOrganizationRubricsCount(organizationId);

            if (rubricNumber > 0) {
                Query q = persistence.currentManager().createQuery(query).setLong("organizationId", organizationId);
                if (page >= 0 && limit > 0) {
                    q.setFirstResult(page * limit);
                    q.setMaxResults(limit);
                }
                List<Rubric> rubrics = q.list();
                for (Rubric r : rubrics) {
                    RubricData rd = new RubricData(r, r.getCreator());
                    response.addFoundNode(rd);
                    response.setHitsNumber(rubricNumber);
                }
                return response;
            }

            return response;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving rubric data");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rubric> getAllRubrics(long orgId, Session session) throws DbConnectionException {
        try {
            String query =
                    "SELECT rubric " +
                    "FROM Rubric rubric " +
                    "WHERE rubric.deleted = :deleted ";

            if (orgId > 0) {
                query += "AND rubric.organization.id = :orgId";
            }

            Query q = session.createQuery(query).setBoolean("deleted", false);

            if (orgId > 0) {
                q.setLong("orgId", orgId);
            }

            @SuppressWarnings("unchecked")
            List<Rubric> result = q.list();

            if (result == null) {
                return new ArrayList<>();
            }
            return result;
        } catch (DbConnectionException e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while retrieving rubrics");
        }
    }

    @Override
    public void deleteRubric(long rubricId, UserContextData context) throws DbConnectionException {
        Result<Void> result = self.deleteRubricAndGetEvents(rubricId, context);
        for (EventData ev : result.getEvents()) {
            try {
                eventFactory.generateEvent(ev);
            } catch (EventException e) {
                logger.error(e);
            }
        }
    }

    @Override
    @Transactional
    public Result<Void> deleteRubricAndGetEvents(long rubricId, UserContextData context) throws DbConnectionException {
        Rubric rubric;
        try {
            rubric = loadResource(Rubric.class, rubricId);
            rubric.setDeleted(true);
            saveEntity(rubric);

            Result<Void> result = new Result<>();

            result.addEvent(eventFactory.generateEventData(EventType.Delete, context, rubric, null, null, null));

            return result;

        } catch (ResourceCouldNotBeLoadedException e) {
            e.printStackTrace();
            throw new DbConnectionException("Error while deleting rubric");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RubricData getOrganizationRubric(long rubricId) {
        String query =
                "SELECT rubric " +
                "FROM Rubric rubric " +
                "INNER JOIN FETCH rubric.creator " +
                "WHERE rubric.id = :rubricId";

        Rubric rubric = (Rubric) persistence.currentManager()
                .createQuery(query)
                .setLong("rubricId", rubricId)
                .uniqueResult();

        RubricData rubricData = new RubricData(rubric, rubric.getCreator());

        return rubricData;
    }

    @Override
    @Transactional(readOnly = true)
    public RubricData getRubricData(long rubricId) throws DbConnectionException {
        try {
            Rubric rubric = loadResource(Rubric.class, rubricId);

            return new RubricData(rubric);
        } catch (ResourceCouldNotBeLoadedException e) {
            throw new DbConnectionException("Error while loading rubric");
        }
    }

    @Override
    public void updateRubric(long rubricId, String name, UserContextData context) throws
            DbConnectionException, EventException, ConstraintViolationException, DataIntegrityViolationException {
        Result<Void> result = self.updateRubricAndGetEvents(rubricId, name, context);
        for(EventData eventData : result.getEvents()){
            eventFactory.generateEvent(eventData);
        }
    }

    @Override
    @Transactional
    public Result<Void> updateRubricAndGetEvents(long rubricId, String name, UserContextData context) throws
            DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        try {
            Result<Void> result = new Result<>();

            String query =
                    "UPDATE Rubric rubric " +
                    "SET rubric.title = :name " +
                    "WHERE rubric.id = :rubricId ";

            persistence.currentManager()
                    .createQuery(query)
                    .setString("name", name)
                    .setLong("rubricId", rubricId)
                    .executeUpdate();

            Rubric rubric = new Rubric();
            rubric.setId(rubricId);
            result.addEvent(eventFactory.generateEventData(EventType.Edit, context, rubric, null, null, null));

            return result;
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error(e);
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while saving rubric");
        }
    }

    private Long getOrganizationRubricsCount(long organizationId) {
        String countQuery =
                "SELECT COUNT (rubric) " +
                        "FROM Rubric rubric " +
                        "WHERE rubric.organization =:organizationId " +
                        "AND rubric.deleted is FALSE";

        Query result = persistence.currentManager()
                .createQuery(countQuery)
                .setLong("organizationId", organizationId);

        return (Long) result.uniqueResult();
    }

}
