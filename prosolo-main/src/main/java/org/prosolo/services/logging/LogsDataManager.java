package org.prosolo.services.logging;/**
 * Created by zoran on 29/12/15.
 */

/**
 * zoran 29/12/15
 */
public interface LogsDataManager {
    Long getSocialActivityMaker(long actorId, long targetId);

    Long getEvaluationSubmissionRequestMaker(long actorId, long objectId);

    Long getRequestMaker(long actorId, long objectId);
}
