package org.prosolo.services.studentProfile.observations;

import java.util.List;

import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.services.common.exception.DbConnectionException;

public interface SymptomManager {

	List<Symptom> getAllSymptoms() throws DbConnectionException;

	public Symptom saveSymptom(long id, String symptom) throws DbConnectionException;
	
	public void deleteSymptom(long id) throws DbConnectionException;
	
	public boolean isSymptomUsed(long symptomId) throws DbConnectionException;
	
	public List<Long> getSymptomIdsWithName(String name) throws DbConnectionException;
	
	public String getSymptomNameForId(long id) throws DbConnectionException;
}