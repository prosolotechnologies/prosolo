package org.prosolo.bigdata.dal.persistence;

import org.hibernate.Session;

public interface GenericDAO {

	//Session openSession();

	//void save(Object o);
	//void save(Object o, Session session);

	Object merge(Object entity);
	void setSession(Session s);

	//T save(T o);
	Object save(Object entity);

}
