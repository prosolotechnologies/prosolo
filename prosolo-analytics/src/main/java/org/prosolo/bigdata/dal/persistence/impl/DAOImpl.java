package org.prosolo.bigdata.dal.persistence.impl;

import javax.persistence.EntityManager;

import org.prosolo.bigdata.dal.persistence.DAO;
import org.prosolo.bigdata.dal.persistence.EntityManagerUtil;
import org.prosolo.common.domainmodel.general.BaseEntity;

/**
 * @author zoran Jul 29, 2015
 */

public class DAOImpl implements DAO {
	private EntityManager em;

	public DAOImpl() {
		em = EntityManagerUtil.getEntityManager();
	}

	@Override
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}
	@Override
	public EntityManager getEntityManager() {
		return em;
	}
	@Override
	public <T> T persist(T o) {
		try {
			
			em.getTransaction().begin();
			em.persist(o);
			em.getTransaction().commit();
		} catch (RuntimeException re) {
			re.printStackTrace();
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
		}

		return o;
	}
	@Override
	public <T> T merge(T o) {
		try {
			em.getTransaction().begin();
			em.merge(o);
			em.getTransaction().commit();
		} catch (RuntimeException re) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
		}
		return o;
	}
}
