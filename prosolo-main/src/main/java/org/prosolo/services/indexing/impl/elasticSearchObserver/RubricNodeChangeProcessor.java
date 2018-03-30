package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.RubricsESService;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-25
 * @since 1.0.0
 */
public class RubricNodeChangeProcessor implements NodeChangeProcessor {

    private static Logger logger = Logger.getLogger(RubricNodeChangeProcessor.class);

    private RubricsESService rubricsESService;
    private Event event;
    private Session session;

    public RubricNodeChangeProcessor(Event event, RubricsESService rubricsESService, Session session) {
        this.rubricsESService = rubricsESService;
        this.event = event;
        this.session = session;
    }

    @Override
    public void process() {
        long rubricId = event.getObject().getId();
        long orgId = event.getOrganizationId();

        if (event.getAction() == EventType.Create || event.getAction() == EventType.Edit) {
            Rubric rubric = (Rubric) session.get(Rubric.class, rubricId);
            rubricsESService.saveRubric(orgId, rubric);
        } else if (event.getAction() == EventType.Delete) {
            rubricsESService.deleteRubric(orgId, rubricId);
        }
    }
}
