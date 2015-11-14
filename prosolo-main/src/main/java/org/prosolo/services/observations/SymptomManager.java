package org.prosolo.services.observations;

import java.util.List;

import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.springframework.transaction.annotation.Transactional;

public interface SymptomManager {

	List<Symptom> getAllSymptoms() throws DbConnectionException;

}