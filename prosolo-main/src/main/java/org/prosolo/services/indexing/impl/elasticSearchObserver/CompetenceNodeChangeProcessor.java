package org.prosolo.services.indexing.impl.elasticSearchObserver;

import java.util.Map;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
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
	private Session session;
	
	
	public CompetenceNodeChangeProcessor(Event event, CompetenceESService competenceESService, 
			NodeOperation operation, Session session) {
		this.event = event;
		this.competenceESService = competenceESService;
		this.operation = operation;
		this.session = session;
	}
	
	@Override
	public void process() {
		Competence1 comp = (Competence1) event.getObject();
		Map<String, String> params = event.getParameters();
		if(operation == NodeOperation.Update) {
			if(event.getAction() == EventType.STATUS_CHANGED) {
				competenceESService.updateStatus(comp.getId(), comp.isPublished());
			} else if(event.getAction() == EventType.RESOURCE_VISIBILITY_CHANGE) {
				competenceESService.updateCompetenceUsersWithPrivileges(comp.getId(), session);
			} else if(event.getAction() == EventType.VISIBLE_TO_ALL_CHANGED) {
				competenceESService.updateVisibleToAll(comp.getId(), comp.isVisibleToAll());
			}
			if(params != null) {
				String jsonChangeTracker = params.get("changes");
				if(params != null) {
					Gson gson = new GsonBuilder().create();
					CompetenceChangeTracker changeTracker = gson.fromJson(jsonChangeTracker, 
							 CompetenceChangeTracker.class);
					competenceESService.updateCompetenceNode(comp, changeTracker,
							session);
				}
			}
		} else if(operation == NodeOperation.Save) {
			competenceESService.saveCompetenceNode(comp, session);
		} else if(operation == NodeOperation.Delete) {
			competenceESService.deleteNodeFromES(comp);
		}
	}

}
