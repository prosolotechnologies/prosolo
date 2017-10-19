package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.OperationForbiddenException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.rubric.*;
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
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.factory.RubricDataFactory;
import org.prosolo.services.nodes.impl.util.EditMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;
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
    @Inject private RubricDataFactory rubricDataFactory;

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
        } catch (Exception e) {
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

    @Override
    @Transactional(readOnly = true)
    public RubricData getRubricData(long rubricId, boolean loadCreator, boolean loadItems, long userId, boolean trackChanges)
            throws DbConnectionException {
        try {
            Rubric rubric = getRubric(rubricId, loadCreator, loadItems, userId);

            if (rubric != null) {
                User creator = loadCreator ? rubric.getCreator() : null;
                Set<Category> categories = loadItems ? rubric.getCategories() : null;
                Set<Level> levels = loadItems ? rubric.getLevels() : null;
                return rubricDataFactory.getRubricData(rubric, creator, categories, levels, trackChanges);
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
            query.append("LEFT JOIN fetch r.categories cat " +
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
    public void saveRubricCategoriesAndLevels(RubricData rubric, EditMode editMode)
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

            Category cat;
            for (RubricCategoryData category : rubric.getCategories()) {
                switch (category.getStatus()) {
                    case CREATED:
                        cat = new Category();
                        cat.setTitle(category.getName());
                        cat.setPoints(category.getPoints());
                        cat.setOrder(category.getOrder());
                        cat.setRubric(rub);
                        saveEntity(cat);
                        category.setId(cat.getId());
                        break;
                    case CHANGED:
                        cat = (Category) persistence.currentManager().load(Category.class, category.getId());
                        cat.setTitle(category.getName());
                        cat.setPoints(category.getPoints());
                        cat.setOrder(category.getOrder());
                        break;
                    case REMOVED:
                        deleteById(Category.class, category.getId(), persistence.currentManager());
                        break;
                    default:
                        break;
                }

                if (category.getStatus() != ObjectStatus.REMOVED) {
                    rubric.getLevels().stream()
                            .filter(level -> level.getStatus() != ObjectStatus.REMOVED)
                            .forEach(level -> {
                                /*
                                if either category or level is just created, the description should be created,
                                otherwise it exists and it should be updated if it is changed except when category
                                or level is removed in which case description should not be touched.
                                 */
                                //if level or category is new, create description
                                if (category.getStatus() == ObjectStatus.CREATED || level.getStatus() == ObjectStatus.CREATED) {
                                    CategoryLevel cl = new CategoryLevel();
                                    cl.setCategory((Category) persistence.currentManager().load(Category.class, category.getId()));
                                    cl.setLevel((Level) persistence.currentManager().load(Level.class, level.getId()));
                                    cl.setDescription(category.getLevels().get(level).getDescription());
                                    saveEntity(cl);
                                } else {
                                    //if category and level are not new nor deleted update description if it has changed
                                    RubricItemDescriptionData desc = category.getLevels().get(level);
                                    if (desc.hasObjectChanged()) {
                                        String query = "UPDATE CategoryLevel cl SET cl.description = :description " +
                                                "WHERE cl.category.id = :categoryId AND cl.level.id = :levelId";
                                        persistence.currentManager()
                                                .createQuery(query)
                                                .setLong("categoryId", category.getId())
                                                .setLong("levelId", level.getId())
                                                .setString("description", category.getLevels().get(level).getDescription())
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
                - creating new categories and levels
                - removing existing categories and levels
                - changing category and level weights/points
                 */

            boolean notAllowedChangesMade = rubric.isReadyToUseChanged();

            if (!notAllowedChangesMade) {
                notAllowedChangesMade = rubric.getCategories()
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
                    .map(r -> rubricDataFactory.getRubricData(r, null, null, null, false))
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
    public List<ActivityRubricCategoryData> getRubricDataForActivity(long actId, long activityAssessmentId, boolean loadGrades)
            throws DbConnectionException {
        try {
            String query =
                    "SELECT cat, catLvl, act.maxPoints ";
            if (loadGrades && activityAssessmentId > 0) {
                query += ", ass ";
            }
            query += "FROM Activity1 act " +
                     "INNER JOIN act.rubric rubric " +
                     "INNER JOIN rubric.categories cat " +
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

            List<ActivityRubricCategoryData> categories = new ArrayList<>();
            Category category = null;
            CategoryAssessment assessment = null;
            List<CategoryLevel> levels = new ArrayList<>();
            for (Object[] row : res) {
                Category cat = (Category) row[0];
                if (category == null || category.getId() != cat.getId()) {
                    if (category != null) {
                        categories.add(rubricDataFactory.getActivityRubricData(category, assessment, levels));
                    }
                    category = cat;
                    if (loadGrades && activityAssessmentId > 0) {
                        assessment = (CategoryAssessment) row[3];
                    }
                    levels.clear();
                }
                levels.add((CategoryLevel) row[1]);
            }
            //add the last category
            if (category != null) {
                categories.add(rubricDataFactory.getActivityRubricData(category, assessment, levels));
            }

            //calculate absolute points based on activity maximum points set
            rubricDataFactory.calculatePointsForCategoriesAndLevels(categories, maxPoints);

            return categories;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the rubric data");
        }
    }

}
