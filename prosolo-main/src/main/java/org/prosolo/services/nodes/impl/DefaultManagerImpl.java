package org.prosolo.services.nodes.impl;

import javax.transaction.Transactional;

import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.nodes.DefaultManager")
public class DefaultManagerImpl extends AbstractManagerImpl implements DefaultManager {

	private static final long serialVersionUID = 2836440371581581765L;
	
	@Override
	@Transactional
	public void flush() {
		super.flush();
	}

}
