package org.prosolo.services.studentProfile.observations.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.studentProfile.observations.SymptomManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.studentProfile.observations.SymptomManager")
public class SymptomManagerImpl extends AbstractManagerImpl implements SymptomManager{

	private static final long serialVersionUID = 3794586060152562963L;
	
	@Override
	@Transactional(readOnly = true)
	public List<Symptom> getAllSymptoms() throws DbConnectionException {
		try {
			Session session = persistence.currentManager();
			Criteria criteria = session.createCriteria(Symptom.class);
			return criteria.list();
		} catch (Exception e) {
			throw new DbConnectionException();
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public Symptom saveSymptom(long id, String symptom) throws DbConnectionException {
		try {
			Symptom s = new Symptom();
			if(id > 0) {
				s.setId(id);
			}
			s.setDescription(symptom);
			return saveEntity(s);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving symptom");
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public void deleteSymptom(long id) throws DbConnectionException {
		try{
			Symptom s = new Symptom();
			s.setId(id);
			persistence.currentManager().delete(s);
		}catch(Exception e){
			throw new DbConnectionException("Error while deleting symptom");
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isSymptomUsed(long symptomId) throws DbConnectionException {
		try{
			String query = 
				"SELECT COUNT(o) " +
				"FROM Observation o " +
				"INNER JOIN o.symptoms s " +
				"WHERE s.id = :id";
			
			Long result = (Long) persistence.currentManager().createQuery(query)
				.setLong("id", symptomId)
				.uniqueResult();
			
			return result > 0;
		}catch(Exception e){
			throw new DbConnectionException("Error. Please try again.");
		}
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<Long> getSymptomIdsWithName(String name) throws DbConnectionException{
		try{
			String query = 
					"SELECT s.id " +
					"FROM Symptom s " +
					"WHERE s.description = :name";
			
			@SuppressWarnings("unchecked")
			List<Long> result = persistence.currentManager().createQuery(query)
				.setString("name", name)
				.list();
			
			if (result != null && !result.isEmpty()) {
				return result;
			}
	
			return new ArrayList<Long>();
		}catch(Exception e){
			throw new DbConnectionException("Error while validating symptom name");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getSymptomNameForId(long id) throws DbConnectionException {
		try{
			String query = 
				"SELECT s.description " +
				"FROM Symptom s " +
				"WHERE s.id = :id";
			
			return (String) persistence.currentManager().createQuery(query)
				.setLong("id", id)
				.uniqueResult();
			
		}catch(Exception e){
			throw new DbConnectionException("Error. Please try again.");
		}
	}
}
