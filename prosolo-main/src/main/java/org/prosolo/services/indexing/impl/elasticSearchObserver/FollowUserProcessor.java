package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.UserEntityESService;

public class FollowUserProcessor implements NodeChangeProcessor {

	private Event event;
	private UserEntityESService userEntityESService;
	private NodeOperation operation;
	
	public FollowUserProcessor(Event event, UserEntityESService userEntityESService, NodeOperation operation) {
		this.event = event;
		this.userEntityESService = userEntityESService;
		this.operation = operation;
	}

	@Override
	public void process() {
		long followerId = event.getActorId();
		long followedUserId = ((User) event.getObject()).getId();
		
		if (operation == NodeOperation.Save) {
			userEntityESService.addFollowerIndex(followedUserId, followerId);
		} else if (operation == NodeOperation.Delete) {
			userEntityESService.removeFollowerIndex(followedUserId, followerId);
		}
	}

}
