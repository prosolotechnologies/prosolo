package org.prosolo.services.admin;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.indexing.*;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.OrganizationData;
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

    @Override
    public void deleteAndInitElasticSearchIndexes() throws IndexingServiceNotAvailable {
        esAdministration.deleteIndexes();
        esAdministration.createIndexes();
    }

    @Override
    public void deleteAndReindexUsers() throws IndexingServiceNotAvailable {
        //delete all user indexes
        esAdministration.deleteIndexByName(ESIndexNames.INDEX_USERS + "*");

        //create system user index
        esAdministration.createIndex(ESIndexNames.INDEX_USERS);

        List<OrganizationData> organizations = orgManager.getAllOrganizations(-1, 0, false)
                .getFoundNodes();
        for (OrganizationData o : organizations) {
            //create user index for each organization
            esAdministration.createIndex(ESIndexNames.INDEX_USERS
                    + ElasticsearchUtil.getOrganizationIndexSuffix(o.getId()));
        }
        indexUsers();
    }

    private void indexUsers() {
        Session session = (Session) defaultManager.getPersistence().openSession();
        Collection<User> users = userManager.getAllUsers();

        try {
            for (User user : users) {
                //if (!user.isSystem()) {
                user = (User) session.merge(user);
                logger.debug("indexing user:" + user.getId() + ". " + user.getName() + " " + user.getLastname());
                userEntityESService.saveUserNode(user, session);
                //}
            }
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
            Collection<User> users = userManager.getAllUsers();
            for (User user : users) {
                //if (!user.isSystem()) {
                user = (User) session.merge(user);
                logger.debug("indexing user:" + user.getId() + ". " + user.getName() + " " + user.getLastname());
                userEntityESService.saveUserNode(user, session);
                //}
            }

            //index credentials
            List<Credential1> credentials = credManager.getAllCredentials(session);
            for (Credential1 cred : credentials) {
                credESService.saveCredentialNode(cred, session);
            }
            //index competences
            List<Competence1> comps = compManager.getAllCompetences(session);
            for (Competence1 comp : comps) {
                compESService.saveCompetenceNode(comp, session);
            }

            //index user groups
            List<UserGroup> groups = userGroupManager.getAllGroups(false, session);
            for (UserGroup group : groups) {
                userGroupESService.saveUserGroup(group.getUnit().getOrganization().getId(), group);
            }

            //index rubrics
            List<Rubric> rubrics = rubricManager.getAllRubrics(session);
            for (Rubric r : rubrics) {
                rubricsESService.saveRubric(r.getOrganization().getId(),r.getId());
            }
        } catch (Exception e) {
            logger.error("Exception in handling message", e);
        } finally {
            HibernateUtil.close(session);
        }
    }
}
