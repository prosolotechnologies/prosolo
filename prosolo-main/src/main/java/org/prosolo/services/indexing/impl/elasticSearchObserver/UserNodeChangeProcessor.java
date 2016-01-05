package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.UserEntityESService;

public class UserNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private Session session;
	private UserEntityESService userEntityESService;
	private EventUserRole userRole;
	
	public UserNodeChangeProcessor(Event event, Session session, UserEntityESService userEntityESService,
			EventUserRole userRole) {
		this.event = event;
		this.session = session;
		this.userEntityESService = userEntityESService;
		this.userRole = userRole;
	}
	
	@Override
	public void process() {
		User user = null;
		if(userRole == EventUserRole.Subject) {
			long userId = event.getActor().getId();
			user = (User) session.load(User.class, userId);
		} else if(userRole == EventUserRole.Object) {
			BaseEntity node = event.getObject();
			user = (User) session.load(User.class, node.getId());
		}
	
		userEntityESService.saveUserNode(user, session);
	}

}
