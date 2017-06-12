package org.prosolo.services.indexing.impl;

import org.springframework.stereotype.Service;

/**
 * 
 * @author Zoran Jeremic
 * @deprecated since 0.7
 */
@Deprecated
@Service("org.prosolo.services.annotation.impl.UploadsObserver")
public class UploadsObserver 
	//extends EventObserver
{
	
//	private static Logger logger = Logger.getLogger(UploadsObserver.class);
//	
//	@Autowired private ESIndexer esIndexer;
//	@Autowired private DefaultManager defaultManager;
//	@Override
//	public EventType[] getSupportedEvents() {
//		return new EventType[] { 
//			EventType.FileUploaded,
//			EventType.LinkAdded,
//			EventType.Post,
//			EventType.AssignmentRemoved
//		};
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public Class<? extends BaseEntity>[] getResourceClasses() {
//		 return new Class[] { 
//			RichContent.class,
//			TargetActivity.class
//		}; 
//	}
//
//	@Override
//	public void handleEvent(Event event) {
//		Session session = (Session) defaultManager.getPersistence().openSession();
//		try{
//			BaseEntity object = event.getObject();
//			long userId = event.getActorId();
//			try {
//				if (object instanceof RichContent) {
//					esIndexer.indexPost(event);
//				} else if (object instanceof TargetActivity) {
//					if (event.getAction().equals(EventType.AssignmentRemoved)) {
//						esIndexer.removeFileUploadedByTargetActivity((TargetActivity) object, userId);
//					} else {
//
//						esIndexer.indexFileUploadedByTargetActivity((TargetActivity) object, userId);
//					}
//				}
//			} catch (Exception e) {
//				logger.error(e);
//			}
//			session.flush();
//		} catch (Exception e) {
//			logger.error("Exception in handling message", e);
//		} finally {
//			HibernateUtil.close(session);
//		}
//	}
	
}
