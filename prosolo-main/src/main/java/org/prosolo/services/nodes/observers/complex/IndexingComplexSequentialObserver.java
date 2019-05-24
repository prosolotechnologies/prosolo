package org.prosolo.services.nodes.observers.complex;

import org.prosolo.common.event.EventObserver;
import org.prosolo.services.event.ComplexSequentialObserver;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.nodes.observers.privilege.UserPrivilegePropagationObserver;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service("org.prosolo.services.nodes.observers.complex.IndexingComplexSequentialObserver")
public class IndexingComplexSequentialObserver extends ComplexSequentialObserver {

	@Inject private NodeChangeObserver nodeChangeObserver;
	@Inject private UserPrivilegePropagationObserver userPrivilegePropagationObserver;
	
	@Override
	protected EventObserver[] getOrderedObservers() {
		return new EventObserver[] {
				nodeChangeObserver,
				userPrivilegePropagationObserver
		};
	}

	
}
