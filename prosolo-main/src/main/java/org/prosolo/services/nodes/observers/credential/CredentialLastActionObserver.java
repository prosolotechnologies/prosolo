package org.prosolo.services.nodes.observers.credential;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.EventObserver;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.core.db.hibernate.HibernateUtil;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service("org.prosolo.services.nodes.observers.credential.CredentialLastActionObserver")
public class CredentialLastActionObserver extends EventObserver {

	private static Logger logger = Logger.getLogger(CredentialLastActionObserver.class.getName());
	
	@Inject private ContextJsonParserService contextJsonParserService;
	@Inject private CredentialManager credManager;
	@Inject private DefaultManager defaultManager;

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
		long credId = Context.getIdFromSubContextWithName(ctx, ContextName.CREDENTIAL);
		Session session = null;
		Transaction transaction = null;
		try {
			if (credId > 0) {
				session = (Session) defaultManager.getPersistence().openSession();
				transaction = session.beginTransaction();
				TargetActivity1 ta = (TargetActivity1) session.load(TargetActivity1.class, event.getObject().getId());
				credManager.updateTargetCredentialLastAction(ta.getTargetCompetence().getUser().getId(), credId, session);
				transaction.commit();
			}
		} catch (Exception e) {
			logger.error("Error", e);
			transaction.rollback();
		} finally {
			HibernateUtil.close(session);
		}
	}
	
}
