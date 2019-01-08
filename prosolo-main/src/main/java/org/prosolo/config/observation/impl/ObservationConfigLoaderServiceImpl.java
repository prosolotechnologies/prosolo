package org.prosolo.config.observation.impl;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.config.observation.ObservationConfig;
import org.prosolo.config.observation.ObservationConfigLoader;
import org.prosolo.config.observation.ObservationConfigLoaderService;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.studentProfile.observations.SuggestionManager;
import org.prosolo.services.studentProfile.observations.SymptomManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.config.observation.ObservationConfigLoaderService")
public class ObservationConfigLoaderServiceImpl extends AbstractManagerImpl 
	implements ObservationConfigLoaderService {

	private static final long serialVersionUID = -8466390975928175412L;

	private static Logger logger = Logger.getLogger(ObservationConfigLoaderServiceImpl.class);

	@Inject
	private SymptomManager symptomManager;
	@Inject
	private SuggestionManager suggestionManager;

	@Override
	@Transactional
	public void initializeObservationConfig() {
		logger.info("Importing observation config");
		
		try {
			ObservationConfig oc = ObservationConfigLoader.loadObservationConfig();
			if(oc != null) {
				List<String> symptoms = oc.getSymptoms();
				symptomManager.saveSymptoms(symptoms);
				List<String> suggestions = oc.getSuggestions();
				suggestionManager.saveSuggestions(suggestions);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DbConnectionException("Error initializing observation config");
		}
	}

}
