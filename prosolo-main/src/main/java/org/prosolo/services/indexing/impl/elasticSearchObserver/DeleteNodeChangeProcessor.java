package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.NodeEntityESService;

@Deprecated
public class DeleteNodeChangeProcessor implements NodeChangeProcessor {

	private Event event;
	private NodeEntityESService nodeEntityESService;
	
	
	public DeleteNodeChangeProcessor(Event event, NodeEntityESService nodeEntityESService) {
		this.event = event;
		this.nodeEntityESService = nodeEntityESService;
	}
	
	@Override
	public void process() {
		BaseEntity node = event.getObject();
		nodeEntityESService.saveNodeToES(node);
	}

}
