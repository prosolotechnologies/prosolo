package org.prosolo.services.logging.impl;/**
 * Created by zoran on 29/12/15.
 */

import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.logging.LogsDataManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * zoran 29/12/15
 */
@Service("org.prosolo.services.logging.LogsDataManager")
public class LogsDataManagerImpl extends AbstractManagerImpl implements LogsDataManager {
	
    private static final long serialVersionUID = -4680208114021696275L;

	@Override
    @Transactional(readOnly = true)
    public Long getSocialActivityMaker(long actorId, long targetId) {
        String query =
            "SELECT maker.id " +
            "FROM SocialActivity sa " +
            "LEFT JOIN sa.maker maker "+
            "WHERE sa.id = :targetId";
        
        return  (Long) persistence.currentManager().createQuery(query)
                .setParameter("targetId", targetId)
                .uniqueResult();
    }

    @Override
    @Transactional (readOnly = true)
    public Long getEvaluationSubmissionRequestMaker(long actorId, long objectId) {

        String query =
                "SELECT maker.id, sentTo.id " +
                "FROM EvaluationSubmission es " +
                "LEFT JOIN es.request r " +
                "LEFT JOIN r.maker maker " +
                "LEFT JOIN r.sentTo sentTo " +
                "WHERE es.id = :objectId";
        
        Object[] result = (Object[]) persistence.currentManager().createQuery(query)
                .setParameter("objectId", objectId)
                .uniqueResult();
        
		Long makerid = (Long) result[0];
		Long sentTo = (Long) result[1];
		System.out.println("ACTOR:" + actorId + " MAKER:" + makerid + " sentTo:" + sentTo);
		
		if (makerid == actorId) {
			return sentTo;
		} else {
			return makerid;
		}
    }

    @Override
    @Transactional (readOnly = true)
    public Long getRequestMaker(long actorId, long objectId) {
    	System.out.println("Get Request maker for:"+actorId+" On object:"+objectId);
    	
        String query =
            "SELECT maker.id, sentTo.id " +
            "FROM Request r " +
            "LEFT JOIN r.maker maker " +
            "LEFT JOIN r.sentTo sentTo " +
            "WHERE r.id = :objectId";
        
         Object[]  result = (Object[]) persistence.currentManager().createQuery(query)
                .setParameter("objectId", objectId)
                .uniqueResult();
         
		Long makerid = (Long) result[0];
		Long sentTo = (Long) result[1];
		System.out.println("ACTOR:" + actorId + " MAKER:" + makerid + " sentTo:" + sentTo);
		
		if (makerid == actorId) {
			return sentTo;
		} else {
			return makerid;
		}
    }
}
