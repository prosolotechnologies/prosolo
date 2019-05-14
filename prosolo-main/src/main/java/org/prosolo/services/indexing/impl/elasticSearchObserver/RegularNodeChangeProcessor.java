package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.event.Event;
import org.prosolo.services.indexing.NodeEntityESService;

public class RegularNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private NodeEntityESService nodeEntityESService;
	private NodeOperation operation;
	
	
	public RegularNodeChangeProcessor(Event event, NodeEntityESService nodeEntityESService, 
			NodeOperation operation) {
		this.event = event;
		this.nodeEntityESService = nodeEntityESService;
		this.operation = operation;
	}
	
	@Override
	public void process() {
		BaseEntity node = event.getObject();
		if(operation == NodeOperation.Save) {
			nodeEntityESService.saveNodeToES(node);
		} else if(operation == NodeOperation.Delete) {
			nodeEntityESService.deleteNodeFromES(node);
		}
	}

}
