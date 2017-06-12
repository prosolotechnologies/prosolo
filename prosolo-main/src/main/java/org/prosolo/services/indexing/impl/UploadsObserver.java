package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//import org.prosolo.services.annotation.Indexer;

/**
zoran
 */
@Service("org.prosolo.services.annotation.impl.UploadsObserver")
public class UploadsObserver extends EventObserver{
	
	private static Logger logger = Logger.getLogger(UploadsObserver.class);
	
	@Autowired private ESIndexer esIndexer;
	@Autowired private DefaultManager defaultManager;
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
			EventType.FileUploaded,
			EventType.LinkAdded,
			EventType.Post,
			EventType.AssignmentRemoved
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		 return new Class[] { 
			RichContent.class,
			TargetActivity.class
		}; 
	}

	@Override
	public void handleEvent(Event event) {
		Session session = (Session) defaultManager.getPersistence().openSession();
		try{
			BaseEntity object = event.getObject();
			long userId = event.getActorId();
			try {
				if (object instanceof RichContent) {
					esIndexer.indexPost(event);
				} else if (object instanceof TargetActivity) {
					if (event.getAction().equals(EventType.AssignmentRemoved)) {
						esIndexer.removeFileUploadedByTargetActivity((TargetActivity) object, userId);
					} else {

						esIndexer.indexFileUploadedByTargetActivity((TargetActivity) object, userId);
					}
				}
			} catch (Exception e) {
				logger.error(e);
			}
			session.flush();
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
	}
	
}
