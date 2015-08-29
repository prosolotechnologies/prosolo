package org.prosolo.bigdata.dal.persistence.impl;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.prosolo.bigdata.dal.persistence.GenericDAO;
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
		session.saveOrUpdate(entity);
		return entity;
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
