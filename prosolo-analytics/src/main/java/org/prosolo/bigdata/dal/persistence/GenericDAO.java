package org.prosolo.bigdata.dal.persistence;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
 

public interface GenericDAO {

	//Session openSession();

	//void save(Object o);
	//void save(Object o, Session session);

	Object merge(Object entity);
	void setSession(Session s);

	//T save(T o);
	Object save(Object entity);
	//Session getSession();
	// load(Class clazz, long id, boolean unproxy);
	<T extends BaseEntity> T load(Class<T> clazz, long id) throws ResourceCouldNotBeLoadedException ;
	//void saveInBatch(List<Object> entities);
	<T extends BaseEntity> void saveInBatch(List<T> entities);
	void execute(HibernateWork hibernateWork);
	Object save(Object entity, Session session);

}
