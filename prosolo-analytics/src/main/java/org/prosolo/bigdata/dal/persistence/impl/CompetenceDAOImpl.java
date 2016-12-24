package org.prosolo.bigdata.dal.persistence.impl;

import java.util.Date;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.dal.persistence.CompetenceDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.bigdata.es.impl.CompetenceIndexerImpl;
import org.prosolo.common.domainmodel.credential.Competence1;

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
			comp.setScheduledPublicDate(null);
			
			CompetenceIndexerImpl.getInstance().updateVisibility(compId, comp.isPublished());
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
	
	
}
