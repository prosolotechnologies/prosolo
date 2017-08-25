package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-24
 * @since 1.0.0
 */

@Service("org.prosolo.services.nodes.RubricManager")
public class RubricManagerImpl extends AbstractManagerImpl implements RubricManager {

    private static Logger logger = Logger.getLogger(RubricManagerImpl.class);

    @Inject
    private EventFactory eventFactory;
    @Inject
    private RubricManager self;

    @Override
    public Rubric createNewRubric(String name, long creatorId, long organizationId, UserContextData context) throws DbConnectionException,
            EventException, ConstraintViolationException, DataIntegrityViolationException {

        Result<Rubric> res = self.createNewRubricAndGetEvents(name, creatorId, organizationId, context);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
        return res.getResult();
    }

    @Override
    public Result<Rubric> createNewRubricAndGetEvents(String name, long creatorId, long organizationId, UserContextData context) throws
            DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        try {
            Rubric rubric = new Rubric();
            User user = new User();
            Organization organization = new Organization();
            organization.setId(organizationId);
            user.setId(creatorId);
            rubric.setTitle(name);
            rubric.setCreator(user);
            rubric.setOrganization(organization);

            saveEntity(rubric);

            Result<Rubric> res = new Result<>();

            res.addEvent(eventFactory.generateEventData(
                    EventType.Create, context.getActorId(), context.getOrganizationId(),
                    context.getSessionId(), rubric, null, context.getContext(), null));

            res.setResult(rubric);
            return res;
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

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<RubricData> getRubrics(int page, int limit, long organizationId) throws DbConnectionException {
        try {
            PaginatedResult<RubricData> response = new PaginatedResult<>();

            String query =
                    "SELECT  rubric " +
                            "FROM Rubric rubric " +
                            "WHERE rubric.organization =:organizationId " +
                            "AND rubric.deleted is FALSE";

            Query q = persistence.currentManager().createQuery(query).setLong("organizationId",organizationId);
            if (page >= 0 && limit > 0) {
                q.setFirstResult(page * limit);
                q.setMaxResults(limit);
            }

            List<Rubric> rubrics = q.list();

            if (rubrics != null) {
                for (Rubric r : rubrics) {
                    RubricData rd = new RubricData(r);
                    response.addFoundNode(rd);
                }
            }
            response.setHitsNumber(getOrganizationRubricsCount(organizationId));
            return response;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving rubric data");
        }
    }

    @Override
    public void deleteRubric(long rubricId) throws DbConnectionException {
        Rubric rubric = null;
        try{
            rubric = loadResource(Rubric.class, rubricId);
            rubric.setDeleted(true);
            saveEntity(rubric);
        } catch (ResourceCouldNotBeLoadedException e) {
            throw new DbConnectionException("Error while deleting rubric");
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
