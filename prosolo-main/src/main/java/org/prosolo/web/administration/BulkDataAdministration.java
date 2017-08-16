package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.hibernate.Session;
import org.prosolo.app.AfterContextLoader;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.indexing.*;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.OrganizationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author Zoran Jeremic Feb 12, 2014
 */
@ManagedBean(name = "bulkDataAdministration")
@Component("bulkDataAdministration")
@Scope("view")
public class BulkDataAdministration implements Serializable {

	private static final long serialVersionUID = -5786790275116348611L;

	@Autowired private ESAdministration esAdministration;
	@Autowired private UserManager userManager;
	@Autowired private DefaultManager defaultManager;
	@Autowired private UserEntityESService userEntityESService;
	@Inject private CredentialManager credManager;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialESService credESService;
	@Inject private CompetenceESService compESService;
	@Inject private UserGroupManager userGroupManager;
	@Inject private UserGroupESService userGroupESService;
	@Inject private OrganizationManager orgManager;

	private static Logger logger = Logger.getLogger(AfterContextLoader.class.getName());

	public void deleteAndReindexElasticSearch() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Delete and reindex elasticsearch started");
					deleteAndInitElasticSearchIndexes();
					indexDBData();
					logger.info("Delete and reindex elasticsearch finished");
				} catch (IndexingServiceNotAvailable e) {
					logger.error(e);
				}
			}
		}).start();
	}

	public void deleteAndReindexUsers() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Delete and reindex users started");
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
					logger.info("Delete and reindex users finished");
				} catch (IndexingServiceNotAvailable e) {
					logger.error(e);
				}
			}
		}).start();
	}

	private void deleteAndInitElasticSearchIndexes() throws IndexingServiceNotAvailable {
		// ESAdministration esAdmin=new ESAdministrationImpl();
		esAdministration.deleteIndexes();
		esAdministration.createIndexes();
	}

	private void deleteAndInitIndex(String indexName) throws IndexingServiceNotAvailable {
		esAdministration.deleteIndex(indexName);
		esAdministration.createIndex(indexName);
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
	
	private void indexDBData() {
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

			//TODO reimplement when credentials and competences are connected to units
//			//index credentials
//			List<Credential1> credentials = credManager.getAllCredentials(session);
//			for(Credential1 cred : credentials) {
//				credESService.saveCredentialNode(cred, session);
//			}
//			//index competences
//			List<Competence1> comps = compManager.getAllCompetences(session);
//			for(Competence1 comp : comps) {
//				compESService.saveCompetenceNode(comp, session);
//			}
			
			//index user groups
			List<UserGroup> groups = userGroupManager.getAllGroups(false, session);
			for(UserGroup group : groups) {
				userGroupESService.saveUserGroup(group.getUnit().getOrganization().getId(), group);
			}
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
	}

}
