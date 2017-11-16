package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.OperationForbiddenException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.rubric.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.rubrics.*;
import org.prosolo.services.nodes.factory.RubricDataFactory;
import org.prosolo.services.nodes.impl.util.EditMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Inject
    private RubricDataFactory rubricDataFactory;
    @Inject
    private Activity1Manager activity1Manager;

    @Override
    public Rubric createNewRubric(String name, UserContextData context) throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {

        Result<Rubric> res = self.createNewRubricAndGetEvents(name, context);
        eventFactory.generateEvents(res.getEventQueue());
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

            res.appendEvent(eventFactory.generateEventData(
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
                    RubricData rd = new RubricData(r, r.getCreator(), isRubricUsed(r.getId()));
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
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while retrieving rubrics");
        }
    }

    @Override
    public void deleteRubric(long rubricId, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        Result<Void> result = self.deleteRubricAndGetEvents(rubricId, context);
        eventFactory.generateEvents(result.getEventQueue());
    }

    @Override
    @Transactional
    public Result<Void> deleteRubricAndGetEvents(long rubricId, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        try {
            Result<Void> result = new Result<>();

            Rubric rubric = new Rubric();
            rubric.setId(rubricId);
            result.appendEvent(eventFactory.generateEventData(EventType.Delete, context, rubric, null, null, null));
            deleteById(Rubric.class, rubricId, persistence.currentManager());

            return result;

        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error("Error: ", e);
            throw e;
        } catch (ResourceCouldNotBeLoadedException e) {
            logger.error("Error: ", e);
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

        RubricData rubricData = new RubricData(rubric, rubric.getCreator(), isRubricUsed(rubricId));

        return rubricData;
    }

    @Override
    public void updateRubricName(long rubricId, String name, UserContextData context) throws
            DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        Result<Void> result = self.updateRubricNameAndGetEvents(rubricId, name, context);
        eventFactory.generateEvents(result.getEventQueue());
    }

    @Override
    @Transactional
    public Result<Void> updateRubricNameAndGetEvents(long rubricId, String name, UserContextData context) throws
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
            result.appendEvent(eventFactory.generateEventData(EventType.Edit, context, rubric, null, null, null));

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

    @Override
    @Transactional(readOnly = true)
    public RubricData getRubricData(long rubricId, boolean loadCreator, boolean loadItems, long userId, boolean trackChanges, boolean loadRubricUsed)
            throws DbConnectionException {
        try {
            Rubric rubric = getRubric(rubricId, loadCreator, loadItems, userId);
            boolean rubricUsed = false;

            if (rubric != null) {
                if (loadRubricUsed) {
                    rubricUsed = isRubricUsed(rubricId);
                }
                User creator = loadCreator ? rubric.getCreator() : null;
                Set<Criterion> criteria = loadItems ? rubric.getCriteria() : null;
                Set<Level> levels = loadItems ? rubric.getLevels() : null;
                return rubricDataFactory.getRubricData(rubric, creator, criteria, levels, trackChanges, rubricUsed);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the rubric data");
        }
    }

    private Rubric getRubric(long rubricId, boolean loadCreator, boolean loadItems, long userId) {
        StringBuilder query = new StringBuilder("SELECT r FROM Rubric r ");
        if (loadCreator) {
            query.append("INNER JOIN fetch r.creator ");
        }
        if (loadItems) {
            query.append("LEFT JOIN fetch r.criteria cat " +
                    "LEFT JOIN fetch cat.levels " +
                    "LEFT JOIN fetch r.levels ");
        }
        query.append("WHERE r.id = :rubricId ");

        if (userId > 0) {
            query.append("AND r.creator.id = :userId");
        }

        Query q = persistence.currentManager()
                .createQuery(query.toString())
                .setLong("rubricId", rubricId);

        if (userId > 0) {
            q.setLong("userId", userId);
        }

        return (Rubric) q.uniqueResult();
    }

    @Override
    @Transactional (rollbackFor = Exception.class)
    public void saveRubricCriteriaAndLevels(RubricData rubric, EditMode editMode)
            throws DbConnectionException, OperationForbiddenException {
        try {
            /*
            set a lock on a rubric so we can be sure that 'isUsed' status does not change between read
			and update
             */
            Rubric rub = (Rubric) persistence.currentManager().load(Rubric.class, rubric.getId(), LockOptions.UPGRADE);

            checkIfRequestIsValid(rubric, editMode);

            if (rubric.isReadyToUseChanged()) {
                rub.setReadyToUse(rubric.isReadyToUse());
            }

            Level lvl;
            for (RubricItemData level : rubric.getLevels()) {
                switch (level.getStatus()) {
                    case CREATED:
                        lvl = new Level();
                        lvl.setTitle(level.getName());
                        lvl.setPoints(level.getPoints());
                        lvl.setOrder(level.getOrder());
                        lvl.setRubric(rub);
                        saveEntity(lvl);
                        level.setId(lvl.getId());
                        break;
                    case CHANGED:
                        lvl = (Level) persistence.currentManager().load(Level.class, level.getId());
                        lvl.setTitle(level.getName());
                        lvl.setPoints(level.getPoints());
                        lvl.setOrder(level.getOrder());
                        break;
                    case REMOVED:
                        deleteById(Level.class, level.getId(), persistence.currentManager());
                        break;
                    default:
                        break;
                }
            }

            Criterion cat;
            for (RubricCriterionData criterion : rubric.getCriteria()) {
                switch (criterion.getStatus()) {
                    case CREATED:
                        cat = new Criterion();
                        cat.setTitle(criterion.getName());
                        cat.setPoints(criterion.getPoints());
                        cat.setOrder(criterion.getOrder());
                        cat.setRubric(rub);
                        saveEntity(cat);
                        criterion.setId(cat.getId());
                        break;
                    case CHANGED:
                        cat = (Criterion) persistence.currentManager().load(Criterion.class, criterion.getId());
                        cat.setTitle(criterion.getName());
                        cat.setPoints(criterion.getPoints());
                        cat.setOrder(criterion.getOrder());
                        break;
                    case REMOVED:
                        deleteById(Criterion.class, criterion.getId(), persistence.currentManager());
                        break;
                    default:
                        break;
                }

                if (criterion.getStatus() != ObjectStatus.REMOVED) {
                    rubric.getLevels().stream()
                            .filter(level -> level.getStatus() != ObjectStatus.REMOVED)
                            .forEach(level -> {
                                /*
                                if either criterion or level is just created, the description should be created,
                                otherwise it exists and it should be updated if it is changed except when criterion
                                or level is removed in which case description should not be touched.
                                 */
                                //if level or criterion is new, create description
                                if (criterion.getStatus() == ObjectStatus.CREATED || level.getStatus() == ObjectStatus.CREATED) {
                                    CriterionLevel cl = new CriterionLevel();
                                    cl.setCriterion((Criterion) persistence.currentManager().load(Criterion.class, criterion.getId()));
                                    cl.setLevel((Level) persistence.currentManager().load(Level.class, level.getId()));
                                    cl.setDescription(criterion.getLevels().get(level).getDescription());
                                    saveEntity(cl);
                                } else {
                                    //if criterion and level are not new nor deleted update description if it has changed
                                    RubricItemDescriptionData desc = criterion.getLevels().get(level);
                                    if (desc.hasObjectChanged()) {
                                        String query = "UPDATE CriterionLevel cl SET cl.description = :description " +
                                                "WHERE cl.criterion.id = :criterionId AND cl.level.id = :levelId";
                                        persistence.currentManager()
                                                .createQuery(query)
                                                .setLong("criterionId", criterion.getId())
                                                .setLong("levelId", level.getId())
                                                .setString("description", criterion.getLevels().get(level).getDescription())
                                                .executeUpdate();
                                    }
                                }
                            });
                }
            }
            /*
            flush and clear are called to first flush all changes and than clear session cache so other queries
            after this one (in the same request) do not return stale data - which in our use case happens
             */
            /*
            TODO see if this is needed after OSIV is no longer used - that will depend on implementation that replaces OSIV
            if session per transaction is used, these two lines can be removed, but if session per client request is used
            these two lines would still be necessary
             */
            persistence.currentManager().flush();
            persistence.currentManager().clear();
        } catch (OperationForbiddenException|DbConnectionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error saving the rubric data");
        }
    }

    private void checkIfRequestIsValid(RubricData rubric, EditMode editMode) throws OperationForbiddenException{
        boolean rubricUsed = isRubricUsed(rubric.getId());
            /*
            if edit mode is full but rubric is used in at least one activity it means we have the stale data
            and rubric is added to at least one activity in the meantime
             */
        if (rubricUsed && editMode == EditMode.FULL) {
            throw new OperationForbiddenException("Rubric can't be saved because it is connected to at least one activity");
        }

        /**
         * if edit mode is limited but changes are made that are not allowed in limited edit mode
         * OperationForbiddenException is thrown
         */
        if (editMode == EditMode.LIMITED) {
                /*
                following changes are allowed only in full edit mode:
                - changing the 'ready' status for the rubric
                - creating new criteria and levels
                - removing existing criteria and levels
                - changing criteria and level weights/points
                 */

            boolean notAllowedChangesMade = rubric.isReadyToUseChanged();

            if (!notAllowedChangesMade) {
                notAllowedChangesMade = rubric.getCriteria()
                        .stream()
                        .anyMatch(c -> c.getStatus() == ObjectStatus.CREATED || c.getStatus() == ObjectStatus.REMOVED || c.arePointsChanged());

                if (!notAllowedChangesMade) {
                    notAllowedChangesMade = rubric.getLevels()
                            .stream()
                            .anyMatch(l -> l.getStatus() == ObjectStatus.CREATED || l.getStatus() == ObjectStatus.REMOVED || l.arePointsChanged());
                }
            }

            if (notAllowedChangesMade) {
                throw new OperationForbiddenException("Limited edit is requested but changes are made that are not allowed in this edit mode");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRubricUsed(long rubricId) throws DbConnectionException {
        try {
            String query = "SELECT a.id FROM Activity1 a " +
                    "WHERE a.deleted IS FALSE " +
                    "AND a.rubric.id = :rubricId";

            return persistence.currentManager().createQuery(query)
                    .setLong("rubricId", rubricId)
                    .setMaxResults(1)
                    .uniqueResult() != null;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error retrieving rubric data");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRubricReadyToUse(long rubricId) throws DbConnectionException {
        try {
            String query =
                    "SELECT r.readyToUse FROM Rubric r " +
                            "WHERE r.id = :id";

            return persistence.currentManager().createQuery(query)
                    .setLong("id", rubricId)
                    .uniqueResult() != null;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error retrieving rubric data");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RubricData> getPreparedRubricsFromUnits(List<Long> unitIds) throws DbConnectionException {
        try {
            if (unitIds.isEmpty()) {
                return new ArrayList<>();
            }

            String query = "SELECT DISTINCT r FROM RubricUnit ru " +
                    "INNER JOIN ru.rubric r " +
                    "WHERE r.readyToUse IS TRUE AND ru.unit.id IN (:unitIds)";

            List<Rubric> rubrics = persistence.currentManager()
                    .createQuery(query)
                    .setParameterList("unitIds", unitIds)
                    .list();

            return rubrics.stream()
                    .map(r -> rubricDataFactory.getRubricData(r, null, null, null, false, false))
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the rubrics data");
        }
    }

    @Override
    @Transactional (readOnly = true)
    public String getRubricName(long id) throws DbConnectionException {
        try {
            String query = "SELECT r.title FROM Rubric r " +
                    "WHERE r.id = :rId";
            return (String) persistence.currentManager()
                    .createQuery(query)
                    .setLong("rId", id)
                    .uniqueResult();
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the rubric name");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityRubricCriterionData> getRubricDataForActivity(long actId, long activityAssessmentId, boolean loadGrades)
            throws DbConnectionException {
        try {
            String query =
                    "SELECT cat, catLvl, act.maxPoints ";
            if (loadGrades && activityAssessmentId > 0) {
                query += ", ass ";
            }
            query += "FROM Activity1 act " +
                    "INNER JOIN act.rubric rubric " +
                    "INNER JOIN rubric.criteria cat " +
                    "INNER JOIN cat.levels catLvl " +
                    "INNER JOIN fetch catLvl.level lvl ";

            if (loadGrades && activityAssessmentId > 0) {
                query += "LEFT JOIN cat.assessments ass " +
                        "WITH ass.assessment.id = :assessmentId ";
            }
            query += "WHERE act.id = :actId " +
                    "ORDER BY cat.order, lvl.order";

            Query q = persistence.currentManager()
                    .createQuery(query)
                    .setLong("actId", actId);

            if (loadGrades && activityAssessmentId > 0) {
                q.setLong("assessmentId", activityAssessmentId);
            }

            List<Object[]> res = q.list();

            if (res.isEmpty()) {
                return null;
            }

            //max points for activity
            int maxPoints = (int) res.get(0)[2];

            List<ActivityRubricCriterionData> criteria = new ArrayList<>();
            Criterion crit = null;
            CriterionAssessment assessment = null;
            List<CriterionLevel> levels = new ArrayList<>();
            for (Object[] row : res) {
                Criterion c = (Criterion) row[0];
                if (crit == null || crit.getId() != c.getId()) {
                    if (crit != null) {
                        criteria.add(rubricDataFactory.getActivityRubricCriterionData(crit, assessment, levels));
                    }
                    crit = c;
                    if (loadGrades && activityAssessmentId > 0) {
                        assessment = (CriterionAssessment) row[3];
                    }
                    levels.clear();
                }
                levels.add((CriterionLevel) row[1]);
            }
            //add the last criterion
            if (crit != null) {
                criteria.add(rubricDataFactory.getActivityRubricCriterionData(crit, assessment, levels));
            }

            //calculate absolute points based on activity maximum points set
            rubricDataFactory.calculatePointsForCriteriaAndLevels(criteria, maxPoints);

            return criteria;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the rubric data");
        }
    }

}
