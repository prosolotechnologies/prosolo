package org.prosolo.web.administration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.util.net.HTTPSConnectionValidator;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.indexing.ESAdministration;
import org.prosolo.services.indexing.FileESIndexer;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.indexing.UserGroupESService;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
	@Autowired private PostManager postManager;
	@Autowired private ActivityManager activityManager;
	@Autowired private FileESIndexer fileESIndexer;
	@Inject private CredentialManager credManager;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialESService credESService;
	@Inject private CompetenceESService compESService;
	@Inject private UserGroupManager userGroupManager;
	@Inject private UserGroupESService userGroupESService;

	private static Logger logger = Logger.getLogger(BulkDataAdministration.class.getName());

	public void deleteAndReindexElasticSearch() {
//
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
				try {
					logger.info("Delete and reindex elasticsearch started");
					deleteAndInitElasticSearchIndexes();
					indexDBData();
					logger.info("Delete and reindex elasticsearch finished");
				} catch (IndexingServiceNotAvailable e) {
					logger.error(e);
				}
				PageUtil.fireSuccessfulInfoMessage("ElasticSearch complete reindexing completed");
//			}
//		}).start();
	}

	public void deleteAndReindexDocuments(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Delete and reindex documents started");
					deleteAndInitIndex(ESIndexNames.INDEX_DOCUMENTS);
					indexDocuments();
					logger.info("Delete and reindex documents finished");
				} catch (IndexingServiceNotAvailable e) {
					logger.error(e);
				}
			}
		}).start();
	}

	public void deleteAndReindexUsers() {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
				try {
					logger.info("Delete and reindex users started");
					deleteAndInitIndex(ESIndexNames.INDEX_USERS);
					indexUsers();
					logger.info("Delete and reindex users finished");
				} catch (IndexingServiceNotAvailable e) {
					logger.error(e);
				}
				PageUtil.fireSuccessfulInfoMessage("ElasticSearch user reindexing completed");
//			}
//		}).start();
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
	
	private void indexDocuments(){
		List<RichContent> richContents = activityManager.getAllResources(RichContent.class);
		for (RichContent richContent : richContents) {
			System.out.println("class:" + richContent.getClass().getName() + " id:" + richContent.getId() + " type:" + richContent.getContentType()
					+ " link:" + richContent.getLink());
			if (richContent.getContentType().equals(ContentType.UPLOAD)) {
				URL url;
				try {
					//	url = new URL(URLEncoder.encode(richContent.getLink(),"UTF-8"));
					String sUrl = richContent.getLink().replaceAll(" ", "%20");
					url = new URL(sUrl);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					HttpURLConnection.setFollowRedirects(true);
					connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:25.0) Gecko/20100101 Firefox/25.0");
					connection.setConnectTimeout(5000);
					connection.setReadTimeout(10000);
					HTTPSConnectionValidator.checkIfHttpsConnection((HttpURLConnection) connection);
					// String contentEncoding= connection.getContentEncoding();
					connection.connect();
					InputStream inputStream = null;
					inputStream = connection.getInputStream();
					fileESIndexer.indexFileForRichContent(inputStream, richContent, 0);
				} catch (MalformedURLException e) {
					logger.error(e);
				} catch (IOException e) {
					logger.error(e);
				}
				 
			} else if (richContent.getContentType().equals(ContentType.LINK)) {
			 
				URL url;
				try {
					url = new URL(richContent.getLink());
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					HttpURLConnection.setFollowRedirects(true);
					connection.setRequestProperty("User-Agent",
							"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:25.0) Gecko/20100101 Firefox/25.0");
					connection.setConnectTimeout(5000);
					connection.setReadTimeout(10000);
					HTTPSConnectionValidator.checkIfHttpsConnection((HttpURLConnection) connection);
					connection.connect();
					InputStream inputStream = null;
					inputStream = connection.getInputStream();
					fileESIndexer.indexHTMLPage(inputStream, richContent, 0);
				} catch (MalformedURLException e) {
					logger.error(e);
				} catch (IOException e) {
					logger.error(e);
				}
			 }
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
			
			//index credentials
			List<Credential1> credentials = credManager.getAllCredentials(session);
			for(Credential1 cred : credentials) {
				credESService.saveCredentialNode(cred, session);
			}
			//index competences
			List<Competence1> comps = compManager.getAllCompetences(session);
			for(Competence1 comp : comps) {
				compESService.saveCompetenceNode(comp, session);
			}
			
			//index user groups
			List<UserGroup> groups = userGroupManager.getAllGroups();
			for(UserGroup group : groups) {
				if(!group.isDefaultGroup()) {
					userGroupESService.saveUserGroup(group);
				}
			}
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
	}

//	private void indexNodes(List<Node> nodes) {
//		for (Node node : nodes) {
//			logger.debug("indexing node:" + node.getClass() + "///" + node.getId() + "." + node.getTitle() + " ");
//			nodeEntityESService.saveNodeToES(node);
//		}
//	}

	public void deleteOldTwitterPosts() {
		deleteOldPostsBeforeDays(0);
	}

	private void deleteOldPostsBeforeDays(int numbOfDays) {
		Session session = (Session) postManager.getPersistence().openSession();
		
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, numbOfDays);
			final Date date = calendar.getTime();
			logger.debug("deleting before date:" + date.toString());
			int deletedResources = postManager.bulkDeleteTwitterPostSocialActivitiesCreatedBefore(session, date);
			PageUtil.fireSuccessfulInfoMessage("Deleted total "
					+ deletedResources
					+ " records related to Twitter posts before "
					+ DateUtil.getPrettyDate(date));
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
	}
	
}
