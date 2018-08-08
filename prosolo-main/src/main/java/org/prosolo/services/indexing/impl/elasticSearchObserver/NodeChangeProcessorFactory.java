package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.*;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class NodeChangeProcessorFactory {

    @Inject
    private UserEntityESService userEntityESService;
    @Inject
    private NodeEntityESService nodeEntityESService;
    @Inject
    private CredentialESService credentialESService;
    @Inject
    private CompetenceESService competenceESService;
    @Inject
    private UserGroupESService userGroupESService;
    @Inject
    private UserGroupManager userGroupManager;
    @Inject
    private ESAdministration esAdministration;
    @Inject
    private OrganizationManager organizationManager;
    @Inject
    private ContextJsonParserService ctxJsonParserService;
    @Inject
    private CredentialManager credManager;
    @Inject
    private RubricsESService rubricsESService;
    @Inject
    private LearningEvidenceESService learningEvidenceESService;
    @Inject
    private AssessmentManager assessmentManager;

    public NodeChangeProcessor getNodeChangeProcessor(Event event, Session session) {
        EventType type = event.getAction();
        BaseEntity node = event.getObject();
        switch (type) {
			case Registered:
			case Account_Activated:
			case Edit_Profile:
			case ENROLL_COURSE:
			case ENROLL_COMPETENCE:
			case COURSE_WITHDRAWN:
			case ACTIVATE_COURSE:
			case ChangeProgress:
                return new UserNodeChangeProcessor(event, session, userEntityESService,
                        credentialESService, competenceESService, credManager, assessmentManager, EventUserRole.Subject);
            case Create:
            case Create_Draft:
            case Edit:
            case Edit_Draft:
            case ChangeVisibility:
            case STUDENT_ASSIGNED_TO_INSTRUCTOR:
            case STUDENT_UNASSIGNED_FROM_INSTRUCTOR:
            case INSTRUCTOR_ASSIGNED_TO_CREDENTIAL:
            case STUDENT_REASSIGNED_TO_INSTRUCTOR:
            case USER_ROLES_UPDATED:
            case INSTRUCTOR_REMOVED_FROM_CREDENTIAL:
            case RESOURCE_VISIBILITY_CHANGE:
            case VISIBLE_TO_ALL_CHANGED:
            case STATUS_CHANGED:
            case USER_ASSIGNED_TO_ORGANIZATION:
            case USER_REMOVED_FROM_ORGANIZATION:
            case ADD_USER_TO_UNIT:
            case REMOVE_USER_FROM_UNIT:
                if (node instanceof User) {
                    return new UserNodeChangeProcessor(event, session, userEntityESService,
                            credentialESService, competenceESService, credManager, assessmentManager, EventUserRole.Object);
                } else if (node instanceof Credential1) {
                    NodeOperation operation = null;
                    if (type == EventType.Create || type == EventType.Create_Draft) {
                        operation = NodeOperation.Save;
                    } else {
                        operation = NodeOperation.Update;
                    }
                    return new CredentialNodeChangeProcessor(event, credentialESService, credManager, operation, session);
                } else if (node instanceof Competence1) {
                    NodeOperation operation = null;
                    if (type == EventType.Create || type == EventType.Create_Draft) {
                        operation = NodeOperation.Save;
                    } else {
                        operation = NodeOperation.Update;
                    }
                    return new CompetenceNodeChangeProcessor(event, competenceESService, operation, session);
                } else if (node instanceof UserGroup) {
                    return new UserGroupNodeChangeProcessor(event, userGroupESService,
                            credentialESService, userGroupManager, competenceESService,
                            userEntityESService, ctxJsonParserService, session);
                } else if (node instanceof Organization) {
                    return new OrganizationNodeChangeProcessor(esAdministration, userEntityESService,
                            organizationManager, event, session);
                } else if (node instanceof Rubric) {
                    return new RubricNodeChangeProcessor(event, rubricsESService, session);
                } else if (node instanceof LearningEvidence) {
                    return new LearningEvidenceNodeChangeProcessor(event, learningEvidenceESService, session);
                } else {
                    return new RegularNodeChangeProcessor(event, nodeEntityESService, NodeOperation.Save);
                }
            case Delete:
            case Delete_Draft:
                if (node instanceof User) {
                    return new UserNodeChangeProcessor(event, session, userEntityESService,
                            credentialESService, competenceESService, credManager, assessmentManager, EventUserRole.Object);
                } else if (node instanceof Credential1) {
                    return new CredentialNodeChangeProcessor(event, credentialESService,
                            credManager, NodeOperation.Delete, session);
                } else if (node instanceof Competence1) {
                    return new CompetenceNodeChangeProcessor(event, competenceESService,
                            NodeOperation.Delete, session);
                } else if (node instanceof UserGroup) {
                    return new UserGroupNodeChangeProcessor(event, userGroupESService,
                            credentialESService, userGroupManager, competenceESService,
                            userEntityESService, ctxJsonParserService, session);
                } else if (node instanceof Rubric) {
                    return new RubricNodeChangeProcessor(event, rubricsESService, session);
                } else if (node instanceof LearningEvidence) {
                    return new LearningEvidenceNodeChangeProcessor(event, learningEvidenceESService, session);
                }
                return new RegularNodeChangeProcessor(event, nodeEntityESService, NodeOperation.Delete);
            case Attach:
                //if(event.getObject() instanceof TargetActivity && event.getTarget() instanceof TargetCompetence) {
                //	return new AttachActivityNodeChangeProcessor(event, nodeEntityESService);
                //}
                return null;
            case Bookmark:
            case RemoveBookmark:
                return new BookmarkNodeChangeProcessor(event, credentialESService, competenceESService, session);
            case Follow:
                return new FollowUserProcessor(event, userEntityESService, NodeOperation.Save, session);
            case Unfollow:
                return new FollowUserProcessor(event, userEntityESService, NodeOperation.Delete, session);
            case ADD_USER_TO_GROUP:
            case REMOVE_USER_FROM_GROUP:
            case USER_GROUP_CHANGE:
                return new UserGroupNodeChangeProcessor(event, userGroupESService, credentialESService,
                        userGroupManager, competenceESService, userEntityESService, ctxJsonParserService, session);
            case ARCHIVE:
                if (node instanceof Competence1) {
                    return new CompetenceNodeChangeProcessor(event, competenceESService,
                            NodeOperation.Archive, session);
                } else if (node instanceof Credential1) {
                    return new CredentialNodeChangeProcessor(event, credentialESService,
                            credManager, NodeOperation.Archive, session);
                }
                break;
            case RESTORE:
                if (node instanceof Competence1) {
                    return new CompetenceNodeChangeProcessor(event, competenceESService,
                            NodeOperation.Restore, session);
                } else if (node instanceof Credential1) {
                    return new CredentialNodeChangeProcessor(event, credentialESService,
                            credManager, NodeOperation.Restore, session);
                }
                break;
            case OWNER_CHANGE:
                if (node instanceof Competence1) {
                    return new CompetenceNodeChangeProcessor(event, competenceESService,
                            NodeOperation.Update, session);
                } else if (node instanceof Credential1) {
                    return new CredentialNodeChangeProcessor(event, credentialESService,
                            credManager, NodeOperation.Update, session);
                }
                break;
            case ADD_CREDENTIAL_TO_UNIT:
            case REMOVE_CREDENTIAL_FROM_UNIT:
                return new CredentialNodeChangeProcessor(event, credentialESService,
                        credManager, NodeOperation.Update, session);
            case ADD_COMPETENCE_TO_UNIT:
            case REMOVE_COMPETENCE_FROM_UNIT:
                return new CompetenceNodeChangeProcessor(event, competenceESService,
                        NodeOperation.Update, session);
			case UPDATE_DELIVERY_TIMES:
				return new CredentialNodeChangeProcessor(event, credentialESService, credManager, NodeOperation.Update, session);
            case LEARNING_STAGE_UPDATE:
                if (node instanceof Competence1) {
                    return new CompetenceNodeChangeProcessor(event, competenceESService,
                            NodeOperation.Update, session);
                } else if (node instanceof Credential1) {
                    return new CredentialNodeChangeProcessor(event, credentialESService,
                            credManager, NodeOperation.Update, session);
                }
                break;
            case CREDENTIAL_CATEGORY_UPDATE:
                return new CredentialNodeChangeProcessor(event, credentialESService,
                        credManager, NodeOperation.Update, session);
            case AssessmentRequested:
            case ASSESSED_BY_AUTO_GRADING:
            case GRADE_ADDED:
                if (node instanceof CredentialAssessment) {
                    return new UserNodeChangeProcessor(event, session, userEntityESService,
                            credentialESService, competenceESService, credManager, assessmentManager, EventUserRole.Subject);
                }
                return null;
            default:
                return null;
        }
        return null;
    }
}
