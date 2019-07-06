package org.prosolo.common.event.context;

import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;

public enum ContextName {

    CREDENTIAL(Credential1.class),
    GOAL_WALL,
    TARGET_COMPETENCE(TargetCompetence1.class),
    TARGET_ACTIVITY(TargetActivity1.class),
    POST_DIALOG(PostSocialActivity1.class),
    USER(User.class),
    ACTIVITY_SEARCH(Activity1.class),
    NODE_COMMENT(Comment1.class),
    SOCIAL_ACTIVITY_COMMENT(Comment1.class),
    SOCIAL_ACTIVITY(SocialActivity1.class),
    STATUS_WALL,
    NEW_POST,
    COMPETENCE_PROGRESS,
    BREADCRUMBS,
    ASSESSMENT_DIALOG,
    ASSESSMENT,
    LTI_LAUNCH,
    LTI_TOOL(LtiTool.class),
    ADD_ACTIVITY_DIALOG(Activity1.class),
    COMPETENCE(Competence1.class),
    DELETE_COMPETENCE_ACTIVITY_DIALOG(CompetenceActivity1.class),
    COMMENT(Comment1.class),
    LIBRARY,
    ACTIVITY(Activity1.class),
    TARGET_CREDENTIAL(TargetCredential1.class),
    ACTIVITY_LINK(ResourceLink.class),
    ACTIVITY_FILE(ResourceLink.class),
    DELETE_DIALOG,
    PROFILE(User.class),
    WHO_TO_FOLLOW(User.class),
    POST_SHARE_DIALOG,
    PREVIEW,
    NEW_COMPETENCE,
    NEW_ACTIVITY,
    SETTINGS_PERSONAL(User.class),
    SETTINGS_EMAIL(User.class),
    SETTINGS_PASSWORD(User.class),
    MESSAGES,
    UPLOAD_RESULT_DIALOG,
    RESULT(TargetActivity1.class),
    RESULTS,
    RESULT_PRIVATE_CONVERSATION_DIALOG,
    STUDENTS,
    EDIT_DIALOG,
    USER_GROUPS_DIALOG(User.class),
    JOIN_BY_URL_GROUP_DIALOG,
    MANAGE_VISIBILITY_DIALOG,
    PUBLISH_RESOURCE_DIALOG,
    ACTIVITY_ASSESSMENT(ActivityAssessment.class),
    COMPETENCE_ASSESSMENT(CompetenceAssessment.class),
    CREDENTIAL_ASSESSMENT(CredentialAssessment.class),
    ACTIVITY_GRADE_DIALOG,
    ASSESSMENT_COMMENTS,
    COMMENTS_ON_ACTIVITY_SUBMISSION(TargetActivity1.class),    // used when assessing an activity and instructor clicks the link that displays comments on activity submissions by other students
    ASK_FOR_ASSESSMENT_DIALOG,
    EXTERNAL_ACTIVITY_GRADE(TargetActivity1.class),
    AUTOGRADE(TargetActivity1.class),
    ARCHIVE_DIALOG,
    DUPLICATE_DIALOG,
    RESTORE_DIALOG,
    START_DELIVERY,
    ORGANIZATION(Organization.class),
    NEW_UNIT,
    UPDATE_UNIT,
    UNIT(Unit.class),
    USER_GROUP(UserGroup.class),
    ADD_USERS_DIALOG(User.class),
    IMPORT_USERS_DIALOG,
    RUBRIC(Rubric.class),
    MAKE_OWNER_DIALOG,
    GROUP_JOIN(UserGroup.class),
    USERS,
    NEXT_LEARNING_STAGE_DIALOG(LearningStage.class),
    EVIDENCE(LearningEvidence.class),
    NEW_EVIDENCE,
    DELIVERY(Credential1.class),
    DELIVERY_INSTRUCTORS,
    DELIVERY_INSTRUCTORS_REMOVE(CredentialInstructor.class),
    DELIVERY_INSTRUCTORS_ADD(User.class),
    ;

    private Class objectType;

    ContextName() {
        this.objectType = null;
    }

    ContextName(Class objectType) {
        this.objectType = objectType;
    }

    public Class getObjectType() {
        return objectType;
    }

}
