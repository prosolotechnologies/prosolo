package org.prosolo.services.nodes.observers.learningResources;

import java.util.Map;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.event.Event;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service
public class LearningResourceChangeProcessorFactory {
			
	@Inject
	private CredentialManager credentialManager;
	@Inject
	private Competence1Manager competenceManager;
	@Inject
	private Activity1Manager activityManager;
	
	public LearningResourceChangeProcessor getProcessor(Event event) {
		BaseEntity node = event.getObject();
		Map<String, String> params = event.getParameters();
		String jsonChangeTracker = params.get("changes");
		if(jsonChangeTracker == null) {
			return null;
		}
		Gson gson = new GsonBuilder().create();
		
		if(node instanceof Credential1) {
			CredentialChangeTracker changeTracker = gson.fromJson(jsonChangeTracker, 
						CredentialChangeTracker.class);
			return new CredentialChangeProcessor((Credential1) node, changeTracker, credentialManager);
		} else if(node instanceof Competence1) {
			CompetenceChangeTracker changeTracker = gson.fromJson(jsonChangeTracker, 
					CompetenceChangeTracker.class);
			return new CompetenceChangeProcessor((Competence1) node, changeTracker, competenceManager);
		} else if(node instanceof Activity1) {
			ActivityChangeTracker changeTracker = gson.fromJson(jsonChangeTracker, 
					ActivityChangeTracker.class);
			return new ActivityChangeProcessor((Activity1) node, changeTracker, activityManager);
		}
		
		return null;
	}
	
}
