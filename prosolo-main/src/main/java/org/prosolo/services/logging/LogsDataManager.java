package org.prosolo.services.logging;/**
 * Created by zoran on 29/12/15.
 */

import org.springframework.transaction.annotation.Transactional;

/**
 * zoran 29/12/15
 */
public interface LogsDataManager {
    @Transactional(readOnly = true)
    Long getPostMaker(long actorId, long postId);

    @Transactional(readOnly = true)
    Long getActivityMakerForTargetActivity(long actorId, long targetId);

    Long getSocialActivityMaker(long actorId, long targetId);

    Long getEvaluationSubmissionRequestMaker(long actorId, long objectId);

    Long getRequestMaker(long actorId, long objectId);
}
