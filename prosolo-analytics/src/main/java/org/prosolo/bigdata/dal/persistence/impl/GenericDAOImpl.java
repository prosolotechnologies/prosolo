package org.prosolo.bigdata.dal.persistence.impl;

import java.util.List;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.bigdata.dal.persistence.GenericDAO;
import org.prosolo.bigdata.dal.persistence.HibernateWork;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
 


public abstract class GenericDAOImpl implements GenericDAO {
	protected Session session; 
	
	
	
	@Override
	public void setSession(Session s) {  
        this.session = s;  
    } 
	@Override
	public Session getSession(){
		return this.session;
	}

	@Override
	public Object save(Object entity) {
		System.out.println("SAVING OR UPDATING2: class:"+entity.getClass().getName());
		Transaction tx=null;
		try{
			tx=session.beginTransaction();
			session.save(entity);
			tx.commit();
		}catch(Exception ex){
			if(tx!=null) tx.rollback();
			ex.printStackTrace();
		}
		
		System.out.println("SAVED:"+entity.getClass().getName());
		
		return entity;
	}
	@Override
	public  <T extends BaseEntity> void saveInBatch(List<T> entities) {
		System.out.println("SAVING in batch: entities:"+entities.size());
		try{
			 boolean isActive = session.getTransaction().isActive();  
	            if ( !isActive) {  
	                session.beginTransaction();  
	            }  
			for(Object entity:entities){
				System.out.println("SAVING OR UPDATING in batch:"+((BaseEntity) entity).getId()+" class:"+entity.getClass().getName());
				session.save(entity);
				System.out.println("SAVED:"+((BaseEntity) entity).getId());
			}
			
			session.getTransaction().commit();
		}catch(Exception ex){
			if(session.getTransaction()!=null){
				session.getTransaction().rollback();
				ex.printStackTrace();
			}
			
		}
		 
	}
	@Override public void execute(HibernateWork hibernateWork){
		session.beginTransaction();
		hibernateWork.doInTransaction(session);
		session.getTransaction().commit();
	}

	@Override
	public Object merge(Object entity){
		Object merged= session.merge(entity);
		session.flush();
		return merged;
	}
	@Override
	public <T extends BaseEntity> T load(Class<T> clazz, long id)  throws ResourceCouldNotBeLoadedException{
		try {
			T resource = (T) session.load(clazz, id);
			return  resource;
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
			throw new ResourceCouldNotBeLoadedException(id, clazz);
		}
	}
}
