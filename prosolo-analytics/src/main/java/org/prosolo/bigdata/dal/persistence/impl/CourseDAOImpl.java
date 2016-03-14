package org.prosolo.bigdata.dal.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.CourseDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;

 


public class CourseDAOImpl extends GenericDAOImpl implements CourseDAO {
	
	
	
	private static Logger logger = Logger
			.getLogger(CourseDAOImpl.class);
	
	public CourseDAOImpl(){
		setSession(HibernateUtil.getSessionFactory().openSession());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public  List<Long> getAllCourseIds() {
		String query = 
			"SELECT course.id " +
			"FROM Course course " +
			"WHERE course.deleted = :deleted " +
			"AND course.published = :published";
		List<Long> result =null;
		try {
			 result = session.createQuery(query)
					 .setParameter("deleted", false)
					 .setParameter("published", true)
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
	public String getCourseTitle(long courseId) {
		String title = null;
		String query = 
			"SELECT course.title " +
			"FROM Course course " +
			"WHERE course.id = :courseId";
		try {
			 title = (String) session.createQuery(query)
					 .setParameter("courseId", courseId)
					 .uniqueResult();
		} catch(Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
		}
		return title;
	}
}
