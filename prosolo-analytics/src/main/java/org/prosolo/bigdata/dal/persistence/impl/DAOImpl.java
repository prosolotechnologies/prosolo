package org.prosolo.bigdata.dal.persistence.impl;

import javax.persistence.EntityManager;

import org.prosolo.bigdata.dal.persistence.DAO;
import org.prosolo.bigdata.dal.persistence.EntityManagerUtil;
import org.prosolo.common.domainmodel.general.BaseEntity;

/**
 * @author zoran Jul 29, 2015
 */

public class DAOImpl implements DAO{
private EntityManager em;
	
	public DAOImpl(){
		em=EntityManagerUtil.getEntityManager();
	}
	public void setEntityManager(EntityManager em){
		this.em = em;
	}
	
	public EntityManager getEntityManager() {
		return em;
	}
	
	public BaseEntity persist(BaseEntity o) {
		try{
			em.getTransaction().begin();
			em.persist(o);
			em.getTransaction().commit();
		}catch(RuntimeException re){
			re.printStackTrace();
			if(em.getTransaction().isActive()){
				em.getTransaction().rollback();
			}
		}
		
		return o;
	}
	public BaseEntity merge(BaseEntity o) {
		try{
		em.getTransaction().begin();
		em.merge(o);
		em.getTransaction().commit();
		}catch(RuntimeException re){
			if(em.getTransaction().isActive()){
				em.getTransaction().rollback();
			}
		}
		return o;
	}
}
