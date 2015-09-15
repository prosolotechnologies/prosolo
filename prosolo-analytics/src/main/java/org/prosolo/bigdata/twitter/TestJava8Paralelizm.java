package org.prosolo.bigdata.twitter;

import java.util.List;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Tag;
import twitter4j.Status;

/**
 * @author Zoran Jeremic, Sep 14, 2015
 *
 */
public class TestJava8Paralelizm {
	public static class TestJava8ParalelizmHolder {
		public static final TestJava8Paralelizm INSTANCE = new TestJava8Paralelizm();
	}
	public static TestJava8Paralelizm getInstance() {
		return TestJava8ParalelizmHolder.INSTANCE;
	}
	
	public void runInParalel(List<Status> statuses){
		for(Status status:statuses){
			new Thread(new Runnable() {
			 
				@Override
				public void run() {
					System.out.println("PROCESSING STATUS FROM THREAD:"+status.getText());
					Session session = HibernateUtil.getSessionFactory().openSession();
						     Boolean isActive= session.getTransaction().isActive();
						                    if (!isActive) {
						                  session.beginTransaction();
						       }
						    try{
						               Tag newTag = new Tag();
						                newTag.setTitle("title" + status.getText());
						                System.out.println("Saving tag in java:"+newTag);
						                session.save(newTag);
						                System.out.println("Saved tag in java:"+newTag) ;  
						                session.flush();
						                session.getTransaction().commit();
						                session.close();
						    }catch(Exception ex){
						     // case ex:Exception =>{
						        if (session.getTransaction() != null) {
						                    session.getTransaction().rollback();
						                    ex.printStackTrace();
						                  }
						      }
						    }
					
				//}
				}).start();
		}
	}
	public void runInParalelForUsers(List<Long> statuses){
		for(Long status:statuses){
			new Thread(new Runnable() {
			 
				@Override
				public void run() {
					System.out.println("PROCESSING STATUS FROM THREAD for users:"+status);
					Session session = HibernateUtil.getSessionFactory().openSession();
						     Boolean isActive= session.getTransaction().isActive();
						                    if (!isActive) {
						                  session.beginTransaction();
						       }
						    try{
						               Tag newTag = new Tag();
						                newTag.setTitle("title" + status);
						                System.out.println("Saving tag for user in java:"+newTag);
						                session.save(newTag);
						                System.out.println("Saved tag for user in java:"+newTag) ;  
						                session.flush();
						                session.getTransaction().commit();
						                session.close();
						    }catch(Exception ex){
						     // case ex:Exception =>{
						        if (session.getTransaction() != null) {
						                    session.getTransaction().rollback();
						                    ex.printStackTrace();
						                  }
						      }
						    }
					
				//}
				}).start();
		}
	}
}
