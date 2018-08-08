package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.hibernate.Session;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.UserEntityESService;

public class FollowUserProcessor implements NodeChangeProcessor {

	private Event event;
	private UserEntityESService userEntityESService;
	private NodeOperation operation;
	private Session session;
	
	public FollowUserProcessor(Event event, UserEntityESService userEntityESService, NodeOperation operation, Session session) {
		this.event = event;
		this.userEntityESService = userEntityESService;
		this.operation = operation;
		this.session = session;
	}

	@Override
	public void process() {
		userEntityESService.updateFollowers(event.getOrganizationId(), event.getObject().getId(), session);
		userEntityESService.updateFollowingUsers(event.getOrganizationId(), event.getActorId(), session);
	}

}
