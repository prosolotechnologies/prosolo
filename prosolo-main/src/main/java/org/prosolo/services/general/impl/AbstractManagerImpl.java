package org.prosolo.services.general.impl;


import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.core.persistance.PersistenceManager;
import org.prosolo.core.spring.deadlock.DeadlockRetry;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractManagerImpl implements AbstractManager {
	
	private static final long serialVersionUID = 8783451939057745352L;

	protected static Logger logger = Logger.getLogger(AbstractManagerImpl.class.getName());
	
	@Inject
	protected PersistenceManager<Session> persistence;
	
	public PersistenceManager<Session> getPersistence() {
		return persistence;
	}

	@Override
	@Transactional
	@DeadlockRetry
	public <T> T saveEntity(T entity) {
 		persistence.save(entity);
		return entity;
	}
	
	@Override
	@Transactional
	@DeadlockRetry
	public <T> T saveEntity(T entity,boolean inNewSession) {
 		persistence.save(entity,inNewSession);
		return entity;
	}
	
	@Override
	@Transactional
	@DeadlockRetry
	public <T> T saveEntity(T entity, Session persistence) {
		persistence.save(entity);
		return entity;
	}
	
	@Override
	public void flush() {
		persistence.flush();
	}
	
	@Override
	@Transactional
	public void clear() {
		persistence.clear();
	}
 
	@Override
	@Transactional
	public void fullCacheClear() {
		persistence.fullCacheClear();
	}
	
	@Override
	@Transactional
	@DeadlockRetry
	public <T extends BaseEntity> T merge(T entity) {
		T object = (T) persistence.merge(entity);
		return object;
	}
	
	@Override
	@Transactional
	@DeadlockRetry
	public <T extends BaseEntity> T merge(T entity, Session session) {
		@SuppressWarnings("unchecked")
		T object = (T) session.merge(entity);
		return object;
	}
	
	@Override
	public <T extends BaseEntity> T refresh(T entity) {
		return (T) persistence.refresh(entity);
	}
	
	@Override
	@Transactional
	public <T extends BaseEntity> void delete(T entity) {
		persistence.delete(entity);
	}
	
	@Override
	@Transactional
	public <T extends BaseEntity> boolean deleteById(Class<T> clazz,  long id, Session session) throws ResourceCouldNotBeLoadedException{
		try {
		Object persistentInstance = session.load(clazz, id);
	    if (persistentInstance != null) {
	        session.delete(persistentInstance);
	        return true;
	    }
	    	return false;
		} catch (ObjectNotFoundException e) {
			throw new ResourceCouldNotBeLoadedException(id, clazz);			 
		}
	}
	
	@Override
	@Transactional
	public <T extends BaseEntity> T markAsDeleted(T entity) {
		entity.setDeleted(true);
		return saveEntity(entity);
	}
	
	@Override
	@Transactional
	public <T extends BaseEntity> T markAsDeleted(T entity, Session session) {
		entity.setDeleted(true);
		return saveEntity(entity, session);
	}

 
	@Override
	@Transactional(readOnly = true)
	public <T extends BaseEntity> T loadResource(Class<T> clazz, long id) throws ResourceCouldNotBeLoadedException {
		return loadResource(clazz, id, persistence.currentManager());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public <T extends BaseEntity> T loadResource(Class<T> clazz, long id,
			Session persistence) throws ResourceCouldNotBeLoadedException {
		try {
			T object = (T) persistence.load(clazz, id);
			return object;
		} catch (ObjectNotFoundException e) {
			throw new ResourceCouldNotBeLoadedException(id, clazz);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public <T extends BaseEntity> T get(Class<T> clazz, long id) throws ResourceCouldNotBeLoadedException {
		try {
			T object = (T) persistence.currentManager().get(clazz, id);
			return object;
		} catch (ObjectNotFoundException e) {
			throw new ResourceCouldNotBeLoadedException(id, clazz);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public <T extends BaseEntity> T loadResource(Class<T> clazz, long id, boolean unproxy)
			throws ResourceCouldNotBeLoadedException {
		
		return loadResource(clazz, id, unproxy, persistence.currentManager());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public <T extends BaseEntity> T loadResource(Class<T> clazz, long id, boolean unproxy,
			Session persistence)
			throws ResourceCouldNotBeLoadedException {
		
		try {
			T resource = (T) persistence.load(clazz, id);
			
			return unproxy ? HibernateUtil.initializeAndUnproxy(resource) : resource;
		} catch (ObjectNotFoundException e) {
			throw new ResourceCouldNotBeLoadedException(id, clazz);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public <T extends BaseEntity> List<T> getAllResources(Class<T> clazz) {
		return persistence.get(clazz);
	}
	
}
