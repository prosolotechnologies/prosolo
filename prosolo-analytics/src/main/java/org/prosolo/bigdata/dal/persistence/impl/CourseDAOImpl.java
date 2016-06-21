package org.prosolo.bigdata.dal.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.CourseDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
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
}
