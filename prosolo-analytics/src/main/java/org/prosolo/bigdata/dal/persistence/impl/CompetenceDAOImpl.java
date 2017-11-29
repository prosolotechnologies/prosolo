package org.prosolo.bigdata.dal.persistence.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.dal.persistence.CompetenceDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.bigdata.es.impl.CompetenceIndexerImplImpl;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompetenceDAOImpl extends GenericDAOImpl implements CompetenceDAO {

	private static Logger logger = Logger
			.getLogger(CompetenceDAOImpl.class);
	
	public CompetenceDAOImpl() {
		setSession(HibernateUtil.getSessionFactory().openSession());
	}
	
	@Override
	public void changeVisibilityForCompetence(long compId) throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) session.load(Competence1.class, compId);
			if(comp.isPublished()) {
				comp.setPublished(false);
			} else {
				comp.setPublished(true);
			}
			
			CompetenceIndexerImplImpl.getInstance().updateVisibility(compId, comp.isPublished());
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competence visibility");
		}
	}
	
	@Override
	public Date getScheduledVisibilityUpdateDate(long compId) {
		String query = 
			"SELECT comp.scheduledPublicDate " +
			"FROM Competence1 comp " +
			"WHERE comp.id = :compId";
		try {
			 return (Date) session.createQuery(query)
					 .setParameter("compId", compId)
					 .uniqueResult();
		} catch(Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void publishCompetences(long credId, long userId) 
			throws DbConnectionException, IllegalDataStateException {
		try {
			//get all draft competences
			List<Competence1> comps = getDraftCompetencesForCredential(credId);
			//publish competences that this user can edit only
			//store ids of competences that can be edited and that are published so list can be 
			//passed to publishActivities method
			List<Long> publishedComps = new ArrayList<>();
			for(Competence1 c : comps) {
				/*
				 * check if competence has at least one activity - if not, it can't be published
				 */
				int numberOfActivities = c.getActivities().size();
				if(numberOfActivities == 0) {
					throw new IllegalDataStateException("Can not publish competency without activities.");
				}
				//check if user can edit this competence
				UserGroupPrivilege priv = getUserPrivilegeForCompetence(credId, c.getId(), userId);
				if (priv == UserGroupPrivilege.Edit) {
					c.setPublished(true);
					CompetenceIndexerImplImpl.getInstance().updateVisibility(c.getId(), true);
					publishedComps.add(c.getId());
				}
			}
			
			new ActivityDAOImpl().publishActivitiesForCompetences(publishedComps);
			session.flush();
			for(long id : publishedComps) {
				Competence1 comp = (Competence1) session
						.load(Competence1.class, id);
				comp.setDuration(getRecalculatedDuration(id));
			}
		} catch (IllegalDataStateException e) {
			logger.error(e);
			//cee.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while publishing competences");
		}
	}
	
	private List<Competence1> getDraftCompetencesForCredential(long credId) {
		StringBuilder queryB = new StringBuilder("SELECT comp FROM CredentialCompetence1 credComp " +
					   "WHERE credComp.credential.id = :credId " +
					   "AND comp.published = :published ");
		
		Query q = session
				.createQuery(queryB.toString())
				.setLong("credId", credId)
				.setBoolean("published", false);
		
		@SuppressWarnings("unchecked")
		List<Competence1> comps = q.list();
		return comps != null ? comps : new ArrayList<>();
	}
	
	private UserGroupPrivilege getUserPrivilegeForCompetence(long credId, long compId, long userId) 
			throws DbConnectionException {
		try {
			if(credId > 0) {
				return new CourseDAOImpl(true).getUserPrivilegeForCredential(credId, userId);
			}
			String query = "SELECT compUserGroup.privilege, comp.createdBy.id, comp.visibleToAll " +
					"FROM CompetenceUserGroup compUserGroup " +
					"INNER JOIN compUserGroup.userGroup userGroup " +
					"RIGHT JOIN compUserGroup.competence comp " +
					"INNER JOIN userGroup.users user " +
						"WITH user.user.id = :userId " +
					"WHERE comp.id = :compId " +
					"ORDER BY CASE WHEN compUserGroup.privilege = :editPriv THEN 1 WHEN compUserGroup.privilege = :viewPriv THEN 2 ELSE 3 END";
			
			Object[] res = (Object[]) session
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("compId", compId)
					.setParameter("editPriv", UserGroupPrivilege.Edit)
					.setParameter("viewPriv", UserGroupPrivilege.Learn)
					.setMaxResults(1)
					.uniqueResult();
			
			if(res == null) {
				return UserGroupPrivilege.None;
			}
			UserGroupPrivilege priv = (UserGroupPrivilege) res[0];
			if(priv == null) {
				priv = UserGroupPrivilege.None;
			}
			long owner = (long) res[1];
			boolean visibleToAll = (boolean) res[2];
			return owner == userId 
				? UserGroupPrivilege.Edit
				: priv == UserGroupPrivilege.None && visibleToAll ? UserGroupPrivilege.Learn : priv;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while trying to retrieve user privilege for competence");
		}
	}
	
	private long getRecalculatedDuration(long compId) {
		String query = "SELECT sum(a.duration) FROM CompetenceActivity1 ca " +
					   "INNER JOIN ca.activity a " +
					   "WHERE ca.competence.id = :compId " +
					   "AND a.published = :published";
		Long res = (Long) session
				.createQuery(query)
				.setLong("compId", compId)
				.setBoolean("published", true)
				.uniqueResult();
		
		return res != null ? res : 0;
	}
}
