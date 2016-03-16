package org.prosolo.services.nodes;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.OrganizationalUnit;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.common.domainmodel.user.AnonUser;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.ServiceType;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;
import org.prosolo.web.competences.data.ActivityFormData;
import org.prosolo.web.competences.data.ActivityType;

public interface ResourceFactory extends AbstractManager {

    Organization createNewOrganization(User currentUser, String name, String abbreviatedName, String description);

    public Role createNewRole(String name, String description, boolean systemDefined, List<Long> capabilities);

    LearningGoal createNewLearningGoal(User currentUser, String name, String description, Date deadline, 
            Collection<Tag> keywords, Collection<Tag> hashtags) throws EventException;

    TargetLearningGoal createNewTargetLearningGoal(LearningGoal goal, User currentUser, boolean progressActivityDependent) throws EventException;

    
    TargetCompetence createNewTargetCompetence(User currentUser, Competence comp, VisibilityType visibilityType);
    
    Activity createNewResourceActivity(User maker, String title,
            String description, AttachmentPreview attachmentPreview, VisibilityType vis,
            Collection<Tag> tags, boolean save) throws EventException;

    OrganizationalUnit createNewOrganizationalUnit(Organization organization, String name, String description, boolean system);

    // LearningGoal createLearningGoal(User currentUser,
    // CompletedGoal completedGoal) throws EventException;

    Competence createCompetence(User user, String title, String description, int validity, int duration, 
            Collection<Tag> tags, List<Competence> prerequisites, List<Competence> corequisites, Date dateCreated);

    Course createCourse(String title, String description, Course basedOn, List<CourseCompetence> competences, 
            Collection<Tag> tags, Collection<Tag> hashtags, User maker, CreatorType creatorType, boolean studentsCanAddNewCompetences, boolean pubilshed);

    Course updateCourse(Course course, String title, String description, List<CourseCompetence> competences, 
            Collection<Tag> tags, Collection<Tag> hashtags, List<String> blogs, boolean studentsCanAddNewCompetences, boolean pubilshed) throws EventException;

    AnonUser createAnonUser(String nickname, String name, String avatarUrl, String profileUrl, ServiceType twitter);

    TargetActivity createNewTargetActivity(Activity activity, User maker);

    TargetActivity createNewTargetActivity(User maker, String title, String description, AttachmentPreview attachmentPreview, VisibilityType vis,
            Collection<Tag> tags, boolean save) throws EventException;

    Activity createNewActivity(User currentUser, String title, String description, ActivityType activityType, 
            boolean mandatory, AttachmentPreview attachmentPreview, int maxNumberOfFiles, boolean uploadsVisibility, 
            int duration, VisibilityType vis) throws EventException;

    Event generateEvent(EventType eventType, User actor, Node object, Node target, Node reason,
            Class<? extends EventObserver>[] observersToExclude) throws EventException;

    User createNewUser(String name, String lastname, String emailAddress, boolean emailVerified, String password, 
            Organization organization, String position, boolean system) throws EventException;

    Activity createNewActivity(User currentUser,
            ActivityFormData activityFormData, VisibilityType vis)
            throws EventException;

    SimpleOutcome createSimpleOutcome(double resultValue);
    
    Map<String, Object> enrollUserInCourse(User user, Course course, TargetLearningGoal targetGoal, String context);
    
    Map<String, Object> enrollUserInCourseInSameTransaction(User user, Course course, TargetLearningGoal targetGoal, String context);
    
    Map<String, Object> assignStudentsToInstructorAutomatically(long courseId, List<Long> courseEnrollmentIds,
            long instructorToExcludeId);
    
    Map<String, Object> enrollUserInCourse(User user, Course course) throws EventException, ResourceCouldNotBeLoadedException;
    
    Course updateCourse(long courseId, String title, String description, Collection<Tag> tags, 
            Collection<Tag> hashtags, boolean published) throws DbConnectionException;

    Activity createNewActivity(ActivityData activityData) throws DbConnectionException;
    
    void deleteCompetenceActivityInSeparateTransaction(long competenceActivityId) 
    		throws DbConnectionException;
    
    String getLinkForObjectType(String simpleClassName, long id, String linkField) 
			throws DbConnectionException;

}