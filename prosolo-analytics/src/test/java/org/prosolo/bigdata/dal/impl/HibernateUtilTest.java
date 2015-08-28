package org.prosolo.bigdata.dal.impl;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;

public class HibernateUtilTest {
	@Test
	public void testHibernateUtil(){
		SessionFactory sf=HibernateUtil.getSessionFactory();
		Session session=sf.openSession();
		User user=(User) session.load(User.class, (long) 22);
		Set<TargetLearningGoal> goals=user.getLearningGoals();
		for(TargetLearningGoal tlf:goals){
			System.out.println("FOUND GOAL:"+tlf.getId());
		}
		session.close();
	}
}
