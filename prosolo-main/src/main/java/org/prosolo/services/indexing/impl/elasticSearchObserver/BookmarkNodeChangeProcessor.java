package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.CredentialESService;

public class BookmarkNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private CredentialESService credentialESService;
	private NodeOperation operation;
	
	
	public BookmarkNodeChangeProcessor(Event event, CredentialESService credentialESService, 
			NodeOperation operation) {
		this.event = event;
		this.credentialESService = credentialESService;
		this.operation = operation;
	}
	
	@Override
	public void process() {
		Credential1 cred = (Credential1) event.getTarget();
		long actorId = event.getActorId();
		/*
		 * for now until scripts are enabled and working, bookmarks are updated by updating
		 * whole nested document
		 */
		//credentialESService.updateCredentialBookmarks(cred.getId());
		if(operation == NodeOperation.Save) {
			credentialESService.addBookmarkToCredentialIndex(cred.getId(), actorId);
		} else if(operation == NodeOperation.Delete) {
			credentialESService.removeBookmarkFromCredentialIndex(cred.getId(), actorId);
		}
	}

}
