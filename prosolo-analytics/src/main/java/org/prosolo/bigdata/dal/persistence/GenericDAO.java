package org.prosolo.bigdata.dal.persistence;

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
	Session getSession();
	// load(Class clazz, long id, boolean unproxy);
	<T extends BaseEntity> T load(Class<T> clazz, long id) throws ResourceCouldNotBeLoadedException ;

}
