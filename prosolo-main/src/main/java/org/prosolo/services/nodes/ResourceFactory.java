package org.prosolo.services.nodes;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.content.RichContent1;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialBookmark;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.common.domainmodel.user.AnonUser;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;
import org.prosolo.web.competences.data.ActivityFormData;
import org.prosolo.web.competences.data.ActivityType;

public interface ResourceFactory extends AbstractManager {

    public Role createNewRole(String name, String description, boolean systemDefined, List<Long> capabilities);

    LearningGoal createNewLearningGoal(long userId, String name, String description, Date deadline, 
            Collection<Tag> keywords, Collection<Tag> hashtags) throws EventException;

    TargetLearningGoal createNewTargetLearningGoal(LearningGoal goal, User currentUser, boolean progressActivityDependent) throws EventException;

    
    TargetCompetence createNewTargetCompetence(long userId, Competence comp, VisibilityType visibilityType) throws ResourceCouldNotBeLoadedException;
    
    Activity createNewResourceActivity(long userId, String title,
            String description, AttachmentPreview attachmentPreview, VisibilityType vis,
            Collection<Tag> tags, boolean save) throws EventException, ResourceCouldNotBeLoadedException;

    Competence createCompetence(long userId, String title, String description, int validity, int duration, 
            Collection<Tag> tags, List<Competence> prerequisites, List<Competence> corequisites, Date dateCreated) throws ResourceCouldNotBeLoadedException;

    Course createCourse(String title, String description, Course basedOn, List<CourseCompetence> competences, 
            Collection<Tag> tags, Collection<Tag> hashtags, long makerId, CreatorType creatorType, boolean studentsCanAddNewCompetences, boolean pubilshed) throws ResourceCouldNotBeLoadedException;

    Course updateCourse(Course course, String title, String description, List<CourseCompetence> competences, 
            Collection<Tag> tags, Collection<Tag> hashtags, List<String> blogs, boolean studentsCanAddNewCompetences, boolean pubilshed) throws EventException;

    AnonUser createAnonUser(String nickname, String name, String avatarUrl, String profileUrl, ServiceType twitter);

    TargetActivity createNewTargetActivity(Activity activity, long userId);

    TargetActivity createNewTargetActivity(User maker, String title, String description, AttachmentPreview attachmentPreview, VisibilityType vis,
            Collection<Tag> tags, boolean save) throws EventException;

    Activity createNewActivity(User currentUser, String title, String description, ActivityType activityType, 
            boolean mandatory, AttachmentPreview attachmentPreview, int maxNumberOfFiles, boolean uploadsVisibility, 
            int duration, VisibilityType vis) throws EventException;

    Event generateEvent(EventType eventType, User actor, Node object, Node target, Node reason,
            Class<? extends EventObserver>[] observersToExclude) throws EventException;

    User createNewUser(String name, String lastname, String emailAddress, boolean emailVerified, String password, 
            String position, boolean system, InputStream imageInputStream, String avatarFilename) throws EventException;

    Activity createNewActivity(long userId,
            ActivityFormData activityFormData, VisibilityType vis)
            throws EventException, ResourceCouldNotBeLoadedException;

    SimpleOutcome createSimpleOutcome(double resultValue);
    
    Map<String, Object> enrollUserInCourse(long userId, Course course, TargetLearningGoal targetGoal, String context) throws ResourceCouldNotBeLoadedException;
    
    Map<String, Object> enrollUserInCourseInSameTransaction(long userId, Course course, TargetLearningGoal targetGoal, String context) throws ResourceCouldNotBeLoadedException;
    
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

	Credential1 createCredential(String title, String description, String tagsString, String hashtagsString, 
			long creatorId, LearningResourceType type, boolean compOrderMandatory, boolean published, 
			long duration, boolean manuallyAssign, List<CompetenceData1> comps);

	/**
	 * Returns Result with saved competence that can be accessed using {@link Result#getResult()} method
	 * and events data that can be accessed using {@link Result#getEvents()}
	 * @param title
	 * @param description
	 * @param tagsString
	 * @param creatorId
	 * @param studentAllowedToAddActivities
	 * @param type
	 * @param published
	 * @param duration
	 * @param activities
	 * @param credentialId
	 * @return
	 */
	Result<Competence1> createCompetence(String title, String description, String tagsString, long creatorId,
			boolean studentAllowedToAddActivities, LearningResourceType type, boolean published, 
			long duration, List<org.prosolo.services.nodes.data.ActivityData> activities, 
			long credentialId);

	Result<Credential1> updateCredential(CredentialData data, long creatorId, 
    		org.prosolo.services.nodes.data.Role role);

	Competence1 updateCompetence(CompetenceData1 data);
	
	long deleteCredentialBookmark(long credId, long userId);
	
	CredentialBookmark bookmarkCredential(long credId, long userId) 
			throws DbConnectionException;

	/**
	 * Returns Result with saved activity that can be accessed using {@link Result#getResult()} method
	 * and events data that can be accessed using {@link Result#getEvents()}
	 * @param activityData
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	Result<Activity1> createActivity(org.prosolo.services.nodes.data.ActivityData activityData, 
    		long userId) throws DbConnectionException;

	Activity1 updateActivity(org.prosolo.services.nodes.data.ActivityData data, long userId) 
			throws DbConnectionException;
	
	Comment1 saveNewComment(CommentData data, long userId, CommentedResourceType resource) 
			throws DbConnectionException;

	User updateUserAvatar(User user, InputStream imageInputStream, String avatarFilename);
	
	PostSocialActivity1 createNewPost(long userId, String text, RichContent1 richContent) 
			throws DbConnectionException;
	
	PostSocialActivity1 updatePost(long postId, String newText) throws DbConnectionException;

}