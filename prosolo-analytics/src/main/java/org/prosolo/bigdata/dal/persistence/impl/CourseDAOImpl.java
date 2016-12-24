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
	public void changeVisibilityForCredential(long credentialId) throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) session.load(Credential1.class, credentialId);
			if(cred.isPublished()) {
				cred.setPublished(false);
			} else {
				cred.setPublished(true);
			}
			cred.setScheduledPublishDate(null);
			
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
			"SELECT cred.scheduledPublicDate " +
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
}
