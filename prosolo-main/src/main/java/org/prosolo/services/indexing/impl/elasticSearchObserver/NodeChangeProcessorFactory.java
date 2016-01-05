package org.prosolo.services.indexing.impl.elasticSearchObserver;

import javax.inject.Inject;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.NodeEntityESService;
import org.prosolo.services.indexing.UserEntityESService;
import org.springframework.stereotype.Service;

@Service
public class NodeChangeProcessorFactory {
			
	@Inject
	private UserEntityESService userEntityESService;
	@Inject
	private NodeEntityESService nodeEntityESService;
	
	public NodeChangeProcessor getNodeChangeProcessor(Event event, Session session) {
		EventType type = event.getAction();
		BaseEntity node = event.getObject();
		switch (type) {
			case Registered:
			case Edit_Profile:
			case ENROLL_COURSE:
				 return new UserNodeChangeProcessor(event, session, userEntityESService, EventUserRole.Subject);
			case Create:
			case Edit:
			case ChangeVisibility:
				if (node instanceof User) {
					return new UserNodeChangeProcessor(event, session, userEntityESService, EventUserRole.Object);
				} else if (node instanceof TargetLearningGoal) {
					return new UserNodeChangeProcessor(event, session, userEntityESService, EventUserRole.Subject);
				} else {
					return new RegularNodeChangeProcessor(event, nodeEntityESService, NodeOperation.Save);
				}
			case Delete:
				return new RegularNodeChangeProcessor(event, nodeEntityESService, NodeOperation.Delete);
			case Attach:
				if(event.getObject() instanceof TargetActivity && event.getTarget() instanceof TargetCompetence) {
					return new AttachActivityNodeChangeProcessor(event, nodeEntityESService);
				}
			default:
				return null;
		}
	}
	
}
