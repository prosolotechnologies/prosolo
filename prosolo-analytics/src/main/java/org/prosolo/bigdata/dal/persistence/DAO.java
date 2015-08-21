package org.prosolo.bigdata.dal.persistence;

import javax.persistence.EntityManager;

import org.prosolo.common.domainmodel.general.BaseEntity;

/**
 * @author zoran Jul 29, 2015
 */

public interface DAO {

	//BaseEntity merge(BaseEntity o);

	//BaseEntity persist(BaseEntity o);

	void setEntityManager(EntityManager em);

	EntityManager getEntityManager();

	<T> T persist(T o);
	<T> T merge(T o);
}
