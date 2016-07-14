package org.prosolo.services.nodes.observers.credential;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.event.context.Context;
import org.prosolo.services.event.context.ContextName;
import org.prosolo.services.nodes.CredentialManager;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.nodes.observers.credential.CredentialLastActionObserver")
public class CredentialLastActionObserver extends EventObserver {

	private static Logger logger = Logger.getLogger(CredentialLastActionObserver.class.getName());
	
	@Inject private ContextJsonParserService contextJsonParserService;
	@Inject private CredentialManager credManager;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
				EventType.AssignmentUploaded,
				EventType.AssignmentRemoved,
				EventType.Completion
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] {
				TargetActivity1.class
		};
	}

	public void handleEvent(Event event) {
		logger.info("CredentialLastActionObserver started");
		String lContext = event.getContext();
		Context ctx = contextJsonParserService.parseContext(lContext);
		long credId = getCredentialIdFromContext(ctx);
		try {
			if(credId > 0) {
				long userId = event.getActorId();
				credManager.updateTargetCredentialLastAction(userId, credId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	private long getCredentialIdFromContext(Context ctx) {
		if(ctx == null) {
			return 0;
		}
		if(ctx.getName() == ContextName.CREDENTIAL) {
			return ctx.getId();
		}
		return getCredentialIdFromContext(ctx.getContext());
	}

}
