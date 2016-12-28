package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.UserGroupESService;

public class UserGroupNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private UserGroupESService groupESService;
	private NodeOperation operation;
	
	
	public UserGroupNodeChangeProcessor(Event event, UserGroupESService groupESService, 
			NodeOperation operation) {
		this.event = event;
		this.groupESService = groupESService;
		this.operation = operation;
	}
	
	@Override
	public void process() {
		UserGroup group = (UserGroup) event.getObject();
		if(operation == NodeOperation.Save || operation == NodeOperation.Update) {
			groupESService.saveUserGroup(group);
		} else if(operation == NodeOperation.Delete) {
			groupESService.deleteNodeFromES(group);
		}
	}

}
