package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.domainmodel.workflow.Scale;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.ScaleManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.ScaleManager")
public class ScaleManagerImpl extends AbstractManagerImpl implements ScaleManager {
	
	private static final long serialVersionUID = -3250620866741152793L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ScaleManager.class);
	
	@Override
	@Transactional (readOnly = true)
	public Scale getOrCreateScale(float minValue,
			float maxValue) {
		Session session = persistence.currentManager();
		
		String query = 
				"SELECT scale " +
				"FROM Scale scale " +
				"WHERE scale.minValue = :minValue " +
					"AND scale.maxValue = :maxValue";
		
		Scale result = (Scale) session.createQuery(query).
				setFloat("minValue", minValue).
				setFloat("maxValue", maxValue).
				uniqueResult();
		
		if (result != null) {
			return result;
		}

		return createScale(minValue, maxValue);
	}

	@Override
	@Transactional
	public Scale createScale(float minValue, float maxValue) {
		Scale scale = new Scale();
		scale.setMinValue(minValue);
		scale.setMaxValue(maxValue);
		
		return saveEntity(scale);
	}

	@Override
	public Scale getScale0To1() {
		return getOrCreateScale(0, 1);
	}
	
	@Override
	public Scale getScale0To5() {
		return getOrCreateScale(0, 5);
	}

	@Override
	@Transactional
	public Scale getScale0To100() {
		return getOrCreateScale(0, 100);
	}

}
