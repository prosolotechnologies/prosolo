package org.prosolo.services.indexing.impl.elasticSearchObserver;

import java.util.Map;

import org.hibernate.Session;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.indexing.ESIndexNames;
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
		long originalVersionId = 0;
		if(comp.isDraft()) {
			originalVersionId = Long.parseLong(params.get("originalVersionId"));
		}
		if(operation == NodeOperation.Update) {
			if(params != null) {
				String jsonChangeTracker = params.get("changes");
				if(params != null) {
					Gson gson = new GsonBuilder().create();
					CompetenceChangeTracker changeTracker = gson.fromJson(jsonChangeTracker, 
							 CompetenceChangeTracker.class);
					competenceESService.updateCompetenceNode(comp, originalVersionId, changeTracker,
							session);
					/*
					 * this means that draft version is published so draft version should
					 * be deleted
					 */
					if(changeTracker.isVersionChanged()) {
						String id = params.get("draftVersionId");
						if(id != null) {
							competenceESService.delete(id, ESIndexNames.INDEX_NODES, 
									ESIndexTypes.COMPETENCE1);
						}
					}
				}
			}
		} else if(operation == NodeOperation.Save) {
			competenceESService.saveCompetenceNode(comp, originalVersionId, session);
			/*
			 * if draft version is created original version hasDraft property
			 * should be updated to true
			 */
			if(event.getAction() == EventType.Create_Draft && comp.isDraft()) {
				competenceESService.updateCompetenceDraftVersionCreated(originalVersionId + "");
			}
		} else if(operation == NodeOperation.Delete) {
			competenceESService.deleteNodeFromES(comp);
			/*
			 * if competence had draft version when it is deleted
			 * draft version id is passed as a parameter to event 
			 * and that id should be used to delete draft version from
			 * index also.
			 */
			if(params != null) {
				String id = params.get("draftVersionId");
				if(id != null) {
					competenceESService.delete(id, ESIndexNames.INDEX_NODES, ESIndexTypes.COMPETENCE1);
				}
			}
		}
	}

}
