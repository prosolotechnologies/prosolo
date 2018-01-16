package org.prosolo.services.general;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.core.persistance.PersistenceManager;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;

public interface AbstractManager extends Serializable {
	
	@SuppressWarnings("rawtypes")
	PersistenceManager getPersistence();

	<T> T saveEntity(T entity);
	
	<T> T saveEntity(T entity, boolean inNewSession);

	<T> T saveEntity(T entity, Session persistence);
	
	void flush();
	
	void clear();
	
	<T extends BaseEntity> T merge(T entity);

	<T extends BaseEntity> T merge(T entity, Session session);
	
	<T extends BaseEntity> T refresh(T entity);
	
	<T extends BaseEntity> void delete(T entity);
	
	<T extends BaseEntity> T markAsDeleted(T entity);
	
	<T extends BaseEntity> T markAsDeleted(T entity, Session session);
	
	//<T extends BaseEntity> T loadResourceByUri(Class<T> clazz, String resourceUri) throws ResourceCouldNotBeLoadedException;
	
	<T> T loadResource(Class<T> clazz, long id) throws ResourceCouldNotBeLoadedException;
	
	<T> T loadResource(Class<T> clazz, long id, Session session) throws ResourceCouldNotBeLoadedException;
	
	<T extends BaseEntity> T loadResource(Class<T> clazz, long id, boolean unproxy, Session session)
			throws ResourceCouldNotBeLoadedException;
	
	 <T extends BaseEntity> T loadResource(Class<T> clazz, long id, boolean unproxy)
	 		throws ResourceCouldNotBeLoadedException;
	
//	<T extends BaseEntity> List<T> loadResourcesByUris(Class<T> clazz, Collection<String> resourceUris);

	<T extends BaseEntity> List<T> getAllResources(Class<T> clazz);
	
	void fullCacheClear();

	 <T extends BaseEntity> boolean deleteById(Class<T> clazz, long id, Session session)
			throws ResourceCouldNotBeLoadedException;

	 <T extends BaseEntity> T get(Class<T> clazz, long id) throws ResourceCouldNotBeLoadedException;

}
