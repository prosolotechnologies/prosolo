package org.prosolo.services.observations.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.observations.SymptomManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.observations.SymptomManager")
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
}
