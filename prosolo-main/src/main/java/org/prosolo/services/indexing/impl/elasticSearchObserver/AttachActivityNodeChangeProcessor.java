package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.NodeEntityESService;

public class AttachActivityNodeChangeProcessor implements NodeChangeProcessor {

	private static Logger logger = Logger.getLogger(AttachActivityNodeChangeProcessor.class);
	
	private Event event;
	private NodeEntityESService nodeEntityESService;
	
	public AttachActivityNodeChangeProcessor(Event event, NodeEntityESService nodeEntityESService) {
		this.event = event;
		this.nodeEntityESService = nodeEntityESService;
	}
	
	@Override
	public void process() {
		logger.info("attaching targetActivity to target Competence...Should add competence to activity");
		Activity activityToUpdate=((TargetActivity) event.getObject()).getActivity();
		nodeEntityESService.saveNodeToES(activityToUpdate);
	}

}
