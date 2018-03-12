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
	//@Override
	/*public Session getSession(){
		return this.session;
	}*/
	@Override
	public Object save(Object entity, Session session) {
		try{
			boolean isActive = session.getTransaction().isActive();  
            if ( !isActive) {  
                session.beginTransaction();  
            }  
			session.save(entity);
			 session.getTransaction().commit();
		}catch(Exception ex){
			 		 
			if(session.getTransaction()!=null){
				session.getTransaction().rollback();
			
			}
			ex.printStackTrace();
		}
		return entity;
	}
	@Override
	public Object save(Object entity) {
		try{
 
			boolean isActive = session.getTransaction().isActive();  
            if ( !isActive) {  
                session.beginTransaction();  
            }  
			session.save(entity);
			 session.getTransaction().commit();
		}catch(Exception ex){
			 		 
			if(session.getTransaction()!=null){
				session.getTransaction().rollback();
			
			}
			ex.printStackTrace();
		}
		
		return entity;
	}
	@Override
	public  <T extends BaseEntity> void saveInBatch(List<T> entities) {
		try{
			 boolean isActive = session.getTransaction().isActive();  
	            if ( !isActive) {  
	                session.beginTransaction();  
	            }  
			for(Object entity:entities){
				session.save(entity);
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
			@SuppressWarnings("unchecked")
			T resource = (T) session.load(clazz, id);
			return  resource;
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
			throw new ResourceCouldNotBeLoadedException(id, clazz);
		}
	}
}
