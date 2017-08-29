package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.ESAdministration;
import org.prosolo.services.indexing.RubricsESService;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.RubricManager;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-25
 * @since 1.0.0
 */
public class RubricNodeChangeProcessor implements NodeChangeProcessor {

    private static Logger logger = Logger.getLogger(RubricNodeChangeProcessor.class);

    private RubricsESService rubricsESService;
    private UserEntityESService userEntityESService;
    private RubricManager rubricManager;
    private Event event;
    private Session session;


    public RubricNodeChangeProcessor(Event event, RubricsESService rubricsESService, UserEntityESService userEntityESService,
                                     RubricManager rubricManager, Session session) {
        this.rubricsESService = rubricsESService;
        this.userEntityESService = userEntityESService;
        this.rubricManager = rubricManager;
        this.event = event;
        this.session = session;
    }

    @Override
    public void process() {
        if (event.getAction() == EventType.Create) {
            try {
                long rubricId = event.getObject().getId();
                rubricsESService.saveRubric(rubricId);
            } catch (Exception e) {
                //TODO handle es exceptions somehow
                logger.error("Error", e);
            }
        }
    }
}
