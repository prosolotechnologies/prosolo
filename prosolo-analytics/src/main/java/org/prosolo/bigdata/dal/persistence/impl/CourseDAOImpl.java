package org.prosolo.bigdata.dal.persistence.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.dal.persistence.CourseDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.bigdata.es.impl.CredentialIndexerImpl;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;

public class CourseDAOImpl extends GenericDAOImpl implements CourseDAO {

	private static Logger logger = Logger
			.getLogger(CourseDAOImpl.class);
	
	public CourseDAOImpl(){
		setSession(HibernateUtil.getSessionFactory().openSession());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public  List<Long> getAllCredentialIds() {
		String query = 
			"SELECT cred.id " +
			"FROM Credential1 cred " +
			"WHERE cred.deleted = :deleted " +
			"AND (cred.published = :published " +
			"OR cred.hasDraft = :hasDraft) " +
			"AND cred.type = :type";
		List<Long> result =null;
		try {
			 result = session.createQuery(query)
					 .setBoolean("deleted", false)
					 .setBoolean("published", true)
					 .setBoolean("hasDraft", true)
					 .setParameter("type", LearningResourceType.UNIVERSITY_CREATED)
					 .list();
		} catch(Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
		}
		if (result != null) {
			return result;
		}
		return new ArrayList<Long>();
	}
	
	@Override
	public String getCredentialTitle(long credId) {
		String title = null;
		String query = 
			"SELECT cred.title " +
			"FROM Credential1 cred " +
			"WHERE cred.id = :credId";
		try {
			 title = (String) session.createQuery(query)
					 .setParameter("credId", credId)
					 .uniqueResult();
		} catch(Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
		}
		return title;
	}

	@Override
	public void publishCredential(long credentialId) {
		String query = 
				"UPDATE Credential1 " +
				"SET published = 1, " +
				"scheduledPublishDate = null"
				+ " WHERE id = :credId";
		session.createQuery(query)
		 	.setParameter("credId", credentialId)
		 .executeUpdate();
	}
	
	@Override
	public void changeVisibilityForCredential(long credentialId, long userId) throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) session.load(Credential1.class, credentialId);
			if(cred.isPublished()) {
				cred.setPublished(false);
			} else {
				cred.setPublished(true);
				new CompetenceDAOImpl().publishCompetences(credentialId, userId);
				session.flush();
				cred.setDuration(getRecalculatedDuration(credentialId));
			}
			cred.setScheduledPublishDate(null);
			session.flush();
			CredentialIndexerImpl.getInstance().updateVisibility(credentialId, cred.isPublished());
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential visibility");
		}
	}
	
	@Override
	public Date getScheduledVisibilityUpdateDate(long credId) {
		String query = 
			"SELECT cred.scheduledPublishDate " +
			"FROM Credential1 cred " +
			"WHERE cred.id = :credId";
		try {
			 return (Date) session.createQuery(query)
					 .setParameter("credId", credId)
					 .uniqueResult();
		} catch(Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
		}
		return null;
	}
	
	@Override
	public UserGroupPrivilege getUserPrivilegeForCredential(long credId, long userId) 
			throws DbConnectionException {
		try {
			String query = "SELECT credUserGroup.privilege, cred.createdBy.id, cred.visibleToAll " +
					"FROM CredentialUserGroup credUserGroup " +
					"INNER JOIN credUserGroup.userGroup userGroup " +
					"RIGHT JOIN credUserGroup.credential cred " +
					"INNER JOIN userGroup.users user " +
						"WITH user.user.id = :userId " +
					"WHERE cred.id = :credId " +
					"ORDER BY CASE WHEN credUserGroup.privilege = :editPriv THEN 1 WHEN credUserGroup.privilege = :viewPriv THEN 2 ELSE 3 END";
			
			Object[] res = (Object[]) session
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("credId", credId)
					.setParameter("editPriv", UserGroupPrivilege.Edit)
					.setParameter("viewPriv", UserGroupPrivilege.View)
					.setMaxResults(1)
					.uniqueResult();
			
			if(res == null) {
				return UserGroupPrivilege.None;
			}
			UserGroupPrivilege priv = (UserGroupPrivilege) res[0];
			if(priv == null) {
				priv = UserGroupPrivilege.None;
			}
			long owner = (long) res[1];
			boolean visibleToAll = (boolean) res[2];
			return owner == userId 
				? UserGroupPrivilege.Edit
				: priv == UserGroupPrivilege.None && visibleToAll ? UserGroupPrivilege.View : priv;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while trying to retrieve user privilege for credential");
		}
	}
	
	private long getRecalculatedDuration(long credId) {
		String query = "SELECT sum(c.duration) FROM CredentialCompetence1 cc " +
					   "INNER JOIN cc.competence c " +
					   "WHERE cc.credential.id = :credId " +
					   "AND c.published = :published";
		Long res = (Long) session
				.createQuery(query)
				.setLong("credId", credId)
				.setBoolean("published", true)
				.uniqueResult();
		
		return res != null ? res : 0;
	}
}
