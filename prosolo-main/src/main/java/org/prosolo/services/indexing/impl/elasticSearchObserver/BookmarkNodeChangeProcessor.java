package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.indexing.CredentialESService;

public class BookmarkNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private CredentialESService credentialESService;
	private CompetenceESService compESService;
	private NodeOperation operation;
	
	
	public BookmarkNodeChangeProcessor(Event event, CredentialESService credentialESService, 
			CompetenceESService compESSerivce, NodeOperation operation) {
		this.event = event;
		this.credentialESService = credentialESService;
		this.compESService = compESSerivce;
		this.operation = operation;
	}
	
	@Override
	public void process() {
		BaseEntity target = event.getTarget();
		long actorId = event.getActorId();
		
		if(target instanceof Credential1) {
			if(operation == NodeOperation.Save) {
				credentialESService.addBookmarkToCredentialIndex(target.getId(), actorId);
			} else if(operation == NodeOperation.Delete) {
				credentialESService.removeBookmarkFromCredentialIndex(target.getId(), actorId);
			}
		} else if(target instanceof Competence1) {
			if(operation == NodeOperation.Save) {
				compESService.addBookmarkToCompetenceIndex(target.getId(), actorId);
			} else if(operation == NodeOperation.Delete) {
				compESService.removeBookmarkFromCompetenceIndex(target.getId(), actorId);
			}
		}
	}

}
