package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.Event;
import org.prosolo.services.indexing.LearningEvidenceESService;

/**
 * @author stefanvuckovic
 * @date 2017-12-07
 * @since 1.2.0
 */
public class LearningEvidenceNodeChangeProcessor implements NodeChangeProcessor {

    private static Logger logger = Logger.getLogger(RubricNodeChangeProcessor.class);

    private Event event;
    private LearningEvidenceESService learningEvidenceESService;
    private Session session;

    public LearningEvidenceNodeChangeProcessor(Event event, LearningEvidenceESService learningEvidenceESService, Session session) {
        this.event = event;
        this.learningEvidenceESService = learningEvidenceESService;
        this.session = session;
    }

    @Override
    public void process() {
        long evidenceId = event.getObject().getId();

        if (event.getAction() == EventType.Create || event.getAction() == EventType.Edit) {
            LearningEvidence le = (LearningEvidence) session.get(LearningEvidence.class, evidenceId);
            learningEvidenceESService.saveEvidence(le);
        } else if (event.getAction() == EventType.Delete) {
            learningEvidenceESService.deleteEvidence(event.getOrganizationId(), evidenceId);
        }
    }
}
