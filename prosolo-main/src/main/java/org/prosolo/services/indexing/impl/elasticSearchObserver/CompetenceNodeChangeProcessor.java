package org.prosolo.services.indexing.impl.elasticSearchObserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.Event;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;

import java.util.Map;

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
		Competence1 comp = (Competence1) session.load(Competence1.class, event.getObject().getId());
		Map<String, String> params = event.getParameters();
		if(operation == NodeOperation.Update) {
			if (event.getAction() == EventType.ADD_COMPETENCE_TO_UNIT || event.getAction() == EventType.REMOVE_COMPETENCE_FROM_UNIT) {
				competenceESService.updateUnits(event.getOrganizationId(), event.getObject().getId(), session);
			} else if (event.getAction() == EventType.STATUS_CHANGED) {
				competenceESService.updateStatus(event.getOrganizationId(), comp);
			} else if (event.getAction() == EventType.OWNER_CHANGE) {
				competenceESService.updateCompetenceOwner(event.getOrganizationId(), comp.getId(), comp.getCreatedBy().getId());
			} else if(event.getAction() == EventType.RESOURCE_VISIBILITY_CHANGE) {
				competenceESService.updateCompetenceUsersWithPrivileges(event.getOrganizationId(), comp.getId(), session);
			} else if (event.getAction() == EventType.VISIBLE_TO_ALL_CHANGED) {
				competenceESService.updateVisibleToAll(event.getOrganizationId(), comp.getId(), comp.isVisibleToAll());
			} else if (event.getAction() == EventType.LEARNING_STAGE_UPDATE) {
				competenceESService.updateLearningStageInfo(comp);
			} else {
				if (params != null) {
					String jsonChangeTracker = params.get("changes");
					if (params != null) {
						Gson gson = new GsonBuilder().create();
						CompetenceChangeTracker changeTracker = gson.fromJson(jsonChangeTracker, 
								 CompetenceChangeTracker.class);
						competenceESService.updateCompetenceNode(comp, changeTracker,
								session);
					}
				}
			}
		} else if (operation == NodeOperation.Save) {
			competenceESService.saveCompetenceNode(comp, session);
		} else if (operation == NodeOperation.Delete) {
			competenceESService.deleteNodeFromES(comp);
		} else if (operation == NodeOperation.Archive || operation == NodeOperation.Restore) {
			competenceESService.updateArchived(event.getOrganizationId(), comp.getId(), comp.isArchived());
		}
	}

}
