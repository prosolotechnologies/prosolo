package org.prosolo.services.indexing.impl.elasticSearchObserver;

import java.util.Map;

import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CompetenceNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private CompetenceESService competenceESService;
	private NodeOperation operation;
	
	
	public CompetenceNodeChangeProcessor(Event event, CompetenceESService competenceESService, 
			NodeOperation operation) {
		this.event = event;
		this.competenceESService = competenceESService;
		this.operation = operation;
	}
	
	@Override
	public void process() {
		Competence1 comp = (Competence1) event.getObject();
		if(operation == NodeOperation.Update) {
			Map<String, String> params = event.getParameters();
			CompetenceChangeTracker changeTracker = null;
			if(params != null) {
				String jsonChangeTracker = params.get("changes");
				if(params != null) {
					Gson gson = new GsonBuilder().create();
					changeTracker = gson.fromJson(jsonChangeTracker, 
							 CompetenceChangeTracker.class);
				}
			}
			competenceESService.updateCompetenceNode(comp, changeTracker);
		} else if(operation == NodeOperation.Save) {
			competenceESService.saveCompetenceNode(comp);
		} else if(operation == NodeOperation.Delete) {
			competenceESService.deleteNodeFromES(comp);
		}
	}

}
