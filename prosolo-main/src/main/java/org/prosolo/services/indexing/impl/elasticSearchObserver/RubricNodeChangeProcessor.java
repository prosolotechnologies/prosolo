package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.common.event.context.LearningContextUtil;
import org.prosolo.services.context.ContextJsonParserService;
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
    private Event event;

    public RubricNodeChangeProcessor(Event event, RubricsESService rubricsESService) {
        this.rubricsESService = rubricsESService;
        this.event = event;
    }

    @Override
    public void process() {
        Rubric rubric = (Rubric) event.getObject();
        long orgId = rubric.getOrganization().getId();
        if (event.getAction() == EventType.Create) {
            rubricsESService.saveRubric(orgId, rubric);
        } else if (event.getAction() == EventType.Delete) {
            rubricsESService.deleteRubric(orgId, rubric.getId());
        } else if (event.getAction() == EventType.Edit) {
            rubricsESService.saveRubric(orgId, rubric);
        }
    }
}
