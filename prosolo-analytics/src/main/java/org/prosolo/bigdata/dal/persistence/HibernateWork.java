package org.prosolo.bigdata.dal.persistence;

import org.hibernate.Session;

/**
 * @author Zoran Jeremic, Sep 3, 2015
 *
 */
public interface HibernateWork {

	void doInTransaction(Session session);

}
