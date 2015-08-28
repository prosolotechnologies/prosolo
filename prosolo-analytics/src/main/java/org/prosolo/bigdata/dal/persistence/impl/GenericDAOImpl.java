package org.prosolo.bigdata.dal.persistence.impl;

import org.hibernate.Session;
import org.prosolo.bigdata.dal.persistence.GenericDAO;


public abstract class GenericDAOImpl implements GenericDAO {
	protected Session session; 
	
	@Override
	public void setSession(Session s) {  
        this.session = s;  
    } 
	//@Override
//	public Session openSession(){
//		return HibernateUtil.getSessionFactory().openSession();
//	}
	@Override
	public Object save(Object entity) {
		session.saveOrUpdate(entity);
		return entity;
		//session.flush();
		//session.close();
	}
//	@Override
//	public void save(Object o,Session session) {
//		session.save(o);
//		session.flush();
//		//session.close();
//	}
	@Override
	public Object merge(Object entity){
		//Session session=HibernateUtil.getSessionFactory().openSession();
		//@SuppressWarnings("unchecked")
		Object merged= session.merge(entity);
		session.flush();
		return merged;
	}
}
