package org.prosolo.services.admin;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.indexing.*;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.user.UserManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * @author nikolamilikic
 * @date 2017-08-18
 * @since 1.0.0
 */
@Service("org.prosolo.services.admin.BulkDataAdministrationService")
public class BulkDataAdministrationServiceImpl implements BulkDataAdministrationService {

    protected static Logger logger = Logger.getLogger(BulkDataAdministrationServiceImpl.class);

    @Inject
    private UserManager userManager;
    @Inject
    private DefaultManager defaultManager;
    @Inject
    private UserEntityESService userEntityESService;
    @Inject
    private CredentialManager credManager;
    @Inject
    private Competence1Manager compManager;
    @Inject
    private CredentialESService credESService;
    @Inject
    private CompetenceESService compESService;
    @Inject
    private UserGroupManager userGroupManager;
    @Inject
    private UserGroupESService userGroupESService;
    @Inject
    private ESAdministration esAdministration;
    @Inject
    private OrganizationManager orgManager;
    @Inject
    private RubricManager rubricManager;
    @Inject
    private RubricsESService rubricsESService;
    @Inject
    private LearningEvidenceManager learningEvidenceManager;
    @Inject
    private LearningEvidenceESService learningEvidenceESService;

    @Override
    public void deleteAndInitElasticSearchDBIndexes() throws IndexingServiceNotAvailable {
        esAdministration.deleteDBIndexes();
        esAdministration.createDBIndexes();
    }

    @Override
    public void deleteAndReindexDBESIndexes() throws IndexingServiceNotAvailable {
        deleteAndInitElasticSearchDBIndexes();
        indexDBData();
    }

    @Override
    public void deleteAndReindexLearningContent(long orgId) throws IndexingServiceNotAvailable {
        //reindex nodes and learning evidences
        deleteAndReindexNodes(orgId);
        deleteAndReindexEvidences(orgId);
    }



    private void deleteAndReindexNodes(long orgId) throws IndexingServiceNotAvailable {
        //delete nodes indexes
        esAdministration.deleteIndexByName(orgId > 0
                ? ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, orgId)
                : ESIndexNames.INDEX_NODES + "*");

        createOrganizationsIndexes(orgId, new String[] {ESIndexNames.INDEX_NODES});

        indexNodes(orgId);
    }

    private void deleteAndReindexEvidences(long orgId) throws IndexingServiceNotAvailable {
        //delete nodes indexes
        esAdministration.deleteIndexByName(orgId > 0
                ? ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_EVIDENCE, orgId)
                : ESIndexNames.INDEX_EVIDENCE + "*");

        createOrganizationsIndexes(orgId, new String[] {ESIndexNames.INDEX_EVIDENCE});

        indexEvidences(orgId);
    }

    @Override
    public void deleteAndReindexRubrics(long orgId) throws IndexingServiceNotAvailable {
        //delete rubrics indexes
        esAdministration.deleteIndexByName(orgId > 0
                ? ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_RUBRIC_NAME, orgId)
                : ESIndexNames.INDEX_RUBRIC_NAME + "*");

        createOrganizationsIndexes(orgId, new String[] {ESIndexNames.INDEX_RUBRIC_NAME});

        indexRubrics(orgId);
    }

    @Override
    public void deleteAndReindexUsersAndGroups(long orgId) throws IndexingServiceNotAvailable {
        //delete all user and user group indexes
        esAdministration.deleteIndexesByName(
                new String[] {
                        orgId > 0 ? ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId) : ESIndexNames.INDEX_USERS + "*",
                        orgId > 0 ? ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USER_GROUP, orgId) : ESIndexNames.INDEX_USER_GROUP + "*"
                });

        if (orgId <= 0) {
            /*
                create system user index but only if general reindex of all users is done. If reindex is done
                for specific organization system level user index should not be created.
             */
            esAdministration.createIndex(ESIndexNames.INDEX_USERS);
        }

        createOrganizationsIndexes(orgId, new String[] {ESIndexNames.INDEX_USERS, ESIndexNames.INDEX_USER_GROUP});

        indexUsersAndUserGroups(orgId);
    }

    private void createOrganizationsIndexes(long orgId, String[] indexes) throws IndexingServiceNotAvailable {
        if (orgId > 0) {
            createOrgIndexes(orgId, indexes);
        } else {
            List<OrganizationData> organizations = orgManager.getAllOrganizations(-1, 0, false)
                    .getFoundNodes();
            for (OrganizationData o : organizations) {
                //create indexes for each organization
                createOrgIndexes(o.getId(), indexes);
            }
        }
    }

    private void createOrgIndexes(long orgId, String[] indexes) throws IndexingServiceNotAvailable {
        for (String index : indexes) {
            esAdministration.createIndex(ElasticsearchUtil.getOrganizationIndexName(index, orgId));
        }
    }

    private void indexUsersAndUserGroups(long orgId) {
        Session session = (Session) defaultManager.getPersistence().openSession();
        try {
            indexUsers(orgId, session);
            indexUserGroups(orgId, session);
        } catch (Exception e) {
            logger.error("Exception in handling message", e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    private void indexNodes(long orgId) {
        Session session = (Session) defaultManager.getPersistence().openSession();
        try {
            indexNodes(orgId, session);
        } catch (Exception e) {
            logger.error("Exception in handling message", e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    private void indexEvidences(long orgId) {
        Session session = (Session) defaultManager.getPersistence().openSession();
        try {
            indexEvidences(orgId, session);
        } catch (Exception e) {
            logger.error("Exception in handling message", e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    private void indexRubrics(long orgId) {
        Session session = (Session) defaultManager.getPersistence().openSession();
        try {
            indexRubrics(orgId, session);
        } catch (Exception e) {
            logger.error("Exception in handling message", e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    @Override
    public void indexDBData() {
        Session session = (Session) defaultManager.getPersistence().openSession();
        try {
            indexUsers(0, session);
            indexNodes(0, session);
            indexUserGroups(0, session);
            indexRubrics(0, session);
            indexEvidences(0, session);
        } catch (Exception e) {
            logger.error("Exception in handling message", e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    private void indexUsers(long orgId, Session session) {
        Collection<User> users = userManager.getAllUsers(orgId, session);
        for (User user : users) {
            logger.debug("indexing user:" + user.getId() + ". " + user.getName() + " " + user.getLastname());
            userEntityESService.saveUserNode(user, session);
        }
    }

    private void indexUserGroups(long orgId, Session session) {
        List<UserGroup> groups = userGroupManager.getAllGroups(orgId,false, session);
        for (UserGroup group : groups) {
            userGroupESService.saveUserGroup(group.getUnit().getOrganization().getId(), group);
        }
    }

    private void indexNodes(long orgId, Session session) {
        //index credentials
        List<Credential1> credentials = credManager.getAllCredentials(orgId, session);
        for (Credential1 cred : credentials) {
            credESService.saveCredentialNode(cred, session);
        }
        //index competences
        List<Competence1> comps = compManager.getAllCompetences(orgId, session);
        for (Competence1 comp : comps) {
            compESService.saveCompetenceNode(comp, session);
        }
    }

    private void indexRubrics(long orgId, Session session) {
        List<Rubric> rubrics = rubricManager.getAllRubrics(orgId, session);
        for (Rubric r : rubrics) {
            rubricsESService.saveRubric(r.getOrganization().getId(), r);
        }
    }

    private void indexEvidences(long orgId, Session session) {
        List<LearningEvidence> evidences = learningEvidenceManager.getAllEvidences(orgId, session);
        for (LearningEvidence le : evidences) {
            learningEvidenceESService.saveEvidence(le);
        }
    }
}
