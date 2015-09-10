package org.prosolo.bigdata.dal.impl;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.prosolo.bigdata.dal.persistence.DiggestGeneratorDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.bigdata.dal.persistence.impl.DiggestGeneratorDAOImpl;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;

public class HibernateUtilTest {
	@Test
	public void testHibernateUtil(){
		useDAO();
	}
	private void useSession(){
		SessionFactory sf=HibernateUtil.getSessionFactory();
		Session session=sf.openSession();
		session.beginTransaction();
		User user=(User) session.load(User.class, (long) 22);
		System.out.println("USER :"+user.getLastname());
		user.setLastname("TESTING");
		session.save(user);
		session.getTransaction().commit();
		session.close();
	}
	private void useDAO(){
		DiggestGeneratorDAO dao=new DiggestGeneratorDAOImpl();
		try {
			User user=dao.load(User.class, (long) 22);
			System.out.println("USER :"+user.getLastname());
			user.setLastname("TESTING3");
			dao.save(user);
		} catch (ResourceCouldNotBeLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
