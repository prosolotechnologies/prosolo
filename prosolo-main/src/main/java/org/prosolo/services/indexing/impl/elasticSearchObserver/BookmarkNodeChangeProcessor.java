package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.hibernate.Session;
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
	private Session session;
	
	
	public BookmarkNodeChangeProcessor(Event event, CredentialESService credentialESService, 
			CompetenceESService compESSerivce, Session session) {
		this.event = event;
		this.credentialESService = credentialESService;
		this.compESService = compESSerivce;
		this.session = session;
	}
	
	@Override
	public void process() {
		BaseEntity target = event.getTarget();

		if(target instanceof Credential1) {
			credentialESService.updateCredentialBookmarks(event.getOrganizationId(), target.getId(), session);
		} else if(target instanceof Competence1) {
			compESService.updateCompetenceBookmarks(event.getOrganizationId(), target.getId(), session);
		}
	}

}
