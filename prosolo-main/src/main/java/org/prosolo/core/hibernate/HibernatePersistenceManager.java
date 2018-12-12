package org.prosolo.core.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.internal.SessionFactoryImpl;
import org.prosolo.core.persistance.PersistenceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Repository("org.prosolo.core.persistance.PersistenceManager")
public class HibernatePersistenceManager implements PersistenceManager<Session> {
	
	private SessionFactory sessionFactory;
	
	// Spring will automatically inject reference to the proper session factory
	@Autowired
	public HibernatePersistenceManager(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	public Session currentManager() {
		Session session= sessionFactory.getCurrentSession();
		// session.setFlushMode(FlushMode.COMMIT);
		 return session;
	 
	}
	public Session openSession(){
		return sessionFactory.openSession();
	}
	public Session getSession(boolean newSession){
		if(newSession){
			return openSession();
		}else{
			return currentManager();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> get(Class<T> searchForType) {
		Session session = currentManager();
		Criteria criteria = session.createCriteria(searchForType);
		return criteria.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> get(Class<T> searchForType, int firstResult,
			int maxResults) {
		Session session = currentManager();
		Criteria criteria = session.createCriteria(searchForType);
		criteria.setFirstResult(firstResult);
		criteria.setMaxResults(maxResults);
		return criteria.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Serializable id, Class<T> searchForType) {
		Session session = currentManager();
		Criteria criteria = session.createCriteria(searchForType);
		criteria.add(Restrictions.idEq(id)).setMaxResults(1);
		return (T) criteria.list().get(0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> get(Class<T> searchForType, T example) {
		Session session = currentManager();
		Criteria criteria = session.createCriteria(searchForType);
		criteria.add(Example.create(example));
		return criteria.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> get(Class<T> searchForType, T example, int firstResult,
			int maxResults) {
		Session session = currentManager();
		Criteria criteria = session.createCriteria(searchForType);
		criteria.add(Example.create(example));
		criteria.setFirstResult(firstResult);
		criteria.setMaxResults(maxResults);
		return criteria.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> get(T example) {
		return get((Class<T>) example.getClass(), example);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> get(T example, int firstResult, int maxResults) {
		return get((Class<T>) example.getClass(), example, firstResult,
				maxResults);
	}
	@Override
	public void save(Object object) {
		save(object,false);
	}
	@Override
	public void save(Object object,boolean inNewSession) {
		Session session = getSession(inNewSession);
		session.saveOrUpdate(object);
	//	session.evict(object);
		flush();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T merge(T object) {
		Session session = currentManager();
		T mergedObject = (T) session.merge(object);
		//session.evict(object);
		flush();
		return mergedObject;
	}
	
	@Override
	public <T> T refresh(T object) {
		Session session = currentManager();
		session.refresh(object);
		return object;
	}
	@Override
	public void delete(Object object){
		delete(object, false);
	}
	
	@Override
	public void delete(Object object, boolean inNewSession) {
		// for some reason, Spring sometimes converts original parameter and pass it here as ArrayList
		if (object instanceof Collection<?>) {
			Iterator<?> it = ((Collection<?>) object).iterator();
			object = it.next();
		}
		Session session = getSession(inNewSession);
		session.delete(object);
//		session.evict(object);
		session.flush();
		 // flush();
	}

//	@Override
//	public void delete(Serializable id) {
//		Object toBeDeleted = get(id);
//		delete(toBeDeleted);	
//		//flush();
//	}

	@Override
	public void deleteAll(Class<?> clazz) {
		Session session = currentManager();
		session.delete("from " + clazz.getName());
		//flush();
	}

	@Override
	public void update(Object o) {
 		Session session = currentManager();
		session.update(o);
		session.evict(o);
		//flush();
	}

	@Override
	public <T> T write(T input) {
		currentManager().saveOrUpdate(input);
		//flush();
		return input;
	}
	
	@Override
	public List<?> runQuery(String queryString){
		Session session = currentManager();		
		Query query = session.createQuery(queryString);
		List<?> list = query.list();		
		return list;		
	}
	
	@Override
	public void flush() {
		  Session session = currentManager();
		  session.flush();
	}
	
	@Override
	public void clear() {
		Session session = currentManager();
		session.clear();
	}
	
 
	
	@Override
	public void fullCacheClear(){
		Session session = currentManager();
		session.clear();
		sessionFactory.getCache().evictCollectionRegions();
		sessionFactory.getCache().evictEntityRegions();
		sessionFactory.getCache().evictQueryRegions();
		sessionFactory.getCache().evictDefaultQueryRegion();
	}

	public Optional<DataSource> getDataSource() {
		ConnectionProvider cp = ((SessionFactoryImpl) sessionFactory).getConnectionProvider();
		return cp.isUnwrappableAs(DataSource.class)
				? Optional.of(cp.unwrap(DataSource.class))
				: Optional.empty();

	}
 

}
