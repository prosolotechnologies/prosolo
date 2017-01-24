package org.prosolo.services.nodes.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.common.domainmodel.activities.ExternalToolActivity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.PostReshareSocialActivity;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.CompetenceType;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.content.RichContent1;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.course.CourseInstructor;
import org.prosolo.common.domainmodel.course.CoursePortfolio;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.course.Status;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialBookmark;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.credential.ResourceLink;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivity1;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.common.domainmodel.user.AnonUser;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.spring.TransactionDebugUtil;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.authentication.PasswordEncrypter;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.feeds.FeedSourceManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ResourceLinkData;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;
import org.prosolo.services.nodes.data.activity.mapper.ActivityMapperFactory;
import org.prosolo.services.nodes.data.activity.mapper.activity.ActivityMapper;
import org.prosolo.services.nodes.factory.ActivityDataFactory;
import org.prosolo.services.upload.AvatarProcessor;
import org.prosolo.web.competences.data.ActivityFormData;
import org.prosolo.web.competences.data.ActivityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nikola Milikic
 * 
 */
@Service("org.prosolo.services.nodes.ResourceFactory")
public class ResourceFactoryImpl extends AbstractManagerImpl implements ResourceFactory {
    
    private static final long serialVersionUID = 2968104792929090003L;

    @Autowired private PostManager postManager;
    @Autowired private PasswordEncrypter passwordEncrypter;
    @Autowired private RoleManager roleManager;
    @Autowired private FeedSourceManager feedSourceManager;
    @Inject private CourseManager courseManager;
    @Inject private CredentialManager credentialManager;
    @Inject private Competence1Manager competenceManager;
    @Inject private Activity1Manager activityManager;
    @Inject private ActivityDataFactory activityFactory;
    @Inject private TagManager tagManager;
    @Inject private AvatarProcessor avatarProcessor;
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Role createNewRole(String name, String description, boolean systemDefined){
        Role role = new Role();
        role.setTitle(name);
        role.setDescription(description);
        role.setDateCreated(new Date());
        role.setSystem(systemDefined);
        role = saveEntity(role);
      
        return role;
    }

    @Override
//  @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Transactional(readOnly = false)
    public LearningGoal createNewLearningGoal(long currentUserId,
            String name, String description, Date deadline, 
            Collection<Tag> tags,
            Collection<Tag> hashtags) throws EventException {
            
        TransactionDebugUtil.transactionRequired("ResourceFactory.createNewLearningGoal");
        
        Date currentTime = new Date();
        
        LearningGoal newLearningGoal = new LearningGoal();
        newLearningGoal.setTitle(name);
        newLearningGoal.setDescription(description);
//        newLearningGoal.setMaker(currentUser);
        newLearningGoal.setDateCreated(currentTime);
        newLearningGoal.setDeadline(deadline);
        
        Set<Tag> tagsSet = new HashSet<Tag>();
        if (tags != null) {
            tagsSet.addAll(tags);
        }
        newLearningGoal.setTags(tagsSet);
        
        Set<Tag> hashtagsSet = new HashSet<Tag>();
        if (hashtags != null) {
            hashtagsSet.addAll(hashtags);
        }
        newLearningGoal.setHashtags(hashtagsSet);
        newLearningGoal.setVisibility(VisibilityType.PUBLIC);
        return saveEntity(newLearningGoal);
    }
    
    @Override
    @Transactional (readOnly = false)
    public TargetLearningGoal createNewTargetLearningGoal(LearningGoal goal, User currentUser, 
            boolean progressActivityDependent) throws EventException {
        
        TargetLearningGoal targetLearningGoal = new TargetLearningGoal();
        targetLearningGoal.setLearningGoal(goal);
        targetLearningGoal.setDateCreated(new Date());
        targetLearningGoal.setProgress(0);
        targetLearningGoal.setTitle(goal.getTitle());
        targetLearningGoal.setProgressActivityDependent(progressActivityDependent);
        targetLearningGoal.setVisibility(VisibilityType.PUBLIC);
        
        return saveEntity(targetLearningGoal);
    }
    
    @Override
    @Transactional (readOnly = false)
    public TargetCompetence createNewTargetCompetence(long userId, Competence comp, VisibilityType visibility) throws ResourceCouldNotBeLoadedException {
        comp = merge(comp);
        
        TargetCompetence newTargetCompetence = new TargetCompetence();
        newTargetCompetence.setDateCreated(new Date());
        newTargetCompetence.setDateStarted(new Date());
        newTargetCompetence.setDateCreated(new Date());
        newTargetCompetence.setCompetence(comp);
        newTargetCompetence.setMaker(loadResource(User.class, userId));
        newTargetCompetence.setVisibility(visibility);
        newTargetCompetence.setTitle(comp.getTitle());
        newTargetCompetence.setDescription(comp.getDescription());
        newTargetCompetence = saveEntity(newTargetCompetence);
        
        List<CompetenceActivity> activities = comp.getActivities();
        
//        if (activities != null && !activities.isEmpty()) {
//            for (CompetenceActivity compActivity : activities) {
//                TargetActivity targetActivity = createNewTargetActivity(compActivity.getActivity(), user);
//                targetActivity.setTaPosition(compActivity.getActivityPosition());
//                targetActivity.setParentCompetence(newTargetCompetence);
//                targetActivity = saveEntity(targetActivity);
//                
//                newTargetCompetence.getTargetActivities().add(targetActivity);
//            }
//        }
//        
//        return saveEntity(newTargetCompetence);
        return null;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Competence createCompetence(long userId, String title,
            String description, int validity, int duration, Collection<Tag> tags, 
            List<Competence> prerequisites, List<Competence> corequisites, Date dateCreated) throws ResourceCouldNotBeLoadedException {
        Competence newCompetence = new Competence();
        newCompetence.setMaker(loadResource(User.class, userId));
        newCompetence.setType(CompetenceType.USER_DEFINED);
        newCompetence.setDateCreated(dateCreated);
        newCompetence.setTitle(title);
        newCompetence.setDescription(description);
        newCompetence.setValidityPeriod(validity);
        newCompetence.setDuration(duration);
        
        Set<Tag> tagsSet = new HashSet<Tag>();
        if (tags != null) {
            tagsSet.addAll(tags);
        }
        newCompetence.setTags(tagsSet);
        newCompetence.setPrerequisites(prerequisites);
        newCompetence.setCorequisites(corequisites);
        return saveEntity(newCompetence);
    }
    
    @Override
    @Transactional
    public TargetActivity createNewTargetActivity(Activity activity, long makerId) {
//        TargetActivity targetActivity = new TargetActivity();
//        targetActivity.setMaker(maker);
//        targetActivity.setActivity(activity);
//        targetActivity.setDateCreated(new Date());
//        targetActivity.setTitle(activity.getTitle());
//        
//        if (activity instanceof UploadAssignmentActivity) {
//            UploadAssignmentActivity uploadActivity = (UploadAssignmentActivity) activity;
//            
//            targetActivity.setVisibility(uploadActivity.isVisibleToEveryone() ? VisibilityType.PUBLIC : VisibilityType.PRIVATE);
//        }
//        return saveEntity(targetActivity);
        
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Activity createNewResourceActivity(long userId, 
            String title, String description,
            AttachmentPreview attachmentPreview,
            VisibilityType vis,
            Collection<Tag> tags,
            boolean save) throws EventException, ResourceCouldNotBeLoadedException {
        
        RichContent richContent = postManager.createRichContent(attachmentPreview);
        
        ResourceActivity activity = new ResourceActivity();
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setRichContent(richContent);
        activity.setMaker(loadResource(User.class, userId));
        activity.setDateCreated(new Date());
            
        Set<Tag> tagsSet = new HashSet<Tag>();
        if (tags != null) {
            tagsSet.addAll(tags);
        }
        activity.setTags(tagsSet);
        
        if (vis != null) {
            activity.setVisibility(vis);
        } else {
            activity.setVisibility(VisibilityType.PRIVATE);
        }
        
        if (save)
            activity = saveEntity(activity);
        
        return activity;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Activity createNewActivity(long userId,  ActivityFormData activityFormData, VisibilityType vis)
            throws EventException, ResourceCouldNotBeLoadedException {

        String title = activityFormData.getTitle();
        String description = activityFormData.getDescription();
        ActivityType activityType = activityFormData.getType();
        boolean mandatory = activityFormData.isMandatory();
        AttachmentPreview attachmentPreview = activityFormData.getAttachmentPreview();
        int maxNumberOfFiles = activityFormData.getMaxNumberOfFiles();
        boolean uploadsVisibility = activityFormData.isVisibleToEveryone();
        int duration = activityFormData.getDuration();
        String launchUrl = activityFormData.getLaunchUrl();
        String sharedSecret = activityFormData.getSharedSecret();
        String consumerKey = activityFormData.getConsumerKey();
        
        if (consumerKey == null || consumerKey.equals("")) {
            consumerKey = Settings.getInstance().config.init.bcName;
        }
        boolean acceptsGrades = activityFormData.isAcceptGrades();
        
        Activity activity = null;
        
        if (activityType.equals(ActivityType.RESOURCE)) {
            activity = new ResourceActivity();
        } else if (activityType.equals(ActivityType.ASSIGNMENTUPLOAD)) {
            activity = new UploadAssignmentActivity();
        } else if (activityType.equals(ActivityType.EXTERNALTOOL)) {
            activity = new ExternalToolActivity();
        }
        
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setMandatory(mandatory);
        activity.setMaker(loadResource(User.class, userId));
        activity.setDateCreated(new Date());
        
        if (vis != null) {
            activity.setVisibility(vis);
        } else {
            activity.setVisibility(VisibilityType.PRIVATE);
        }

        if (activityType.equals(ActivityType.RESOURCE)) {
            RichContent richContent = postManager.createRichContent(attachmentPreview);
            ((ResourceActivity) activity).setRichContent(richContent);
            
        } else if (activityType.equals(ActivityType.ASSIGNMENTUPLOAD)) {
            ((UploadAssignmentActivity) activity).setDuration(duration);
            ((UploadAssignmentActivity) activity).setVisibleToEveryone(uploadsVisibility);
            ((UploadAssignmentActivity) activity).setMaxFilesNumber(maxNumberOfFiles);
        } else if (activityType.equals(ActivityType.EXTERNALTOOL)) {
            ((ExternalToolActivity) activity).setLaunchUrl(launchUrl);
            ((ExternalToolActivity) activity).setSharedSecret(sharedSecret);
            ((ExternalToolActivity) activity).setAcceptGrades(acceptsGrades);
            ((ExternalToolActivity) activity).setConsumerKey(consumerKey);
        }
        activity = saveEntity(activity);
        return activity;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Deprecated
    public Activity createNewActivity(User currentUser, String title,
            String description, ActivityType activityType, boolean mandatory,
            AttachmentPreview attachmentPreview, int maxNumberOfFiles,
            boolean uploadsVisibility, int duration, VisibilityType vis)
            throws EventException {
        
        Activity activity = null;
        
        if (activityType.equals(ActivityType.RESOURCE)){
            activity = new ResourceActivity();
        } else if(activityType.equals(ActivityType.ASSIGNMENTUPLOAD)){
            activity = new UploadAssignmentActivity();
        }else if(activityType.equals(ActivityType.EXTERNALTOOL)){
            activity=new ExternalToolActivity();
        }
        
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setMandatory(mandatory);
        activity.setMaker(currentUser);
        activity.setDateCreated(new Date());
        
        if (vis != null) {
            activity.setVisibility(vis);
        } else {
            activity.setVisibility(VisibilityType.PRIVATE);
        }
        
        if (activityType.equals(ActivityType.RESOURCE)){
            RichContent richContent = postManager.createRichContent(attachmentPreview);
            ((ResourceActivity) activity).setRichContent(richContent);
            
        } else if (activityType.equals(ActivityType.ASSIGNMENTUPLOAD)) {
            ((UploadAssignmentActivity) activity).setDuration(duration);
            ((UploadAssignmentActivity) activity).setVisibleToEveryone(uploadsVisibility);
            ((UploadAssignmentActivity) activity).setMaxFilesNumber(maxNumberOfFiles);
        }
        activity = saveEntity(activity);
        return activity;
    }
    
    @Override
    @Transactional(readOnly = false)
    public Course createCourse(String title, String description,
            Course basedOn, List<CourseCompetence> competences, 
            Collection<Tag> tags, Collection<Tag> hashtags, long makerId,
            CreatorType creatorType, boolean studentsCanAddNewCompetences, boolean pubilshed) throws ResourceCouldNotBeLoadedException {
        
        Course newCourse = new Course();
        Date now = new Date();
        newCourse.setTitle(title);
        newCourse.setDescription(description);
        newCourse.setDateCreated(now);
        newCourse.setMaker(loadResource(User.class, makerId));
        newCourse.setCreatorType(creatorType);
        
        Set<Tag> tagsSet = new HashSet<Tag>();
        if (tags != null) {
            tagsSet.addAll(tags);
        }
        
        newCourse.setTags(tagsSet);
        
        Set<Tag> hashtagsSet = new HashSet<Tag>();
        if (hashtags != null) {
            hashtagsSet.addAll(hashtags);
        }
        
        newCourse.setHashtags(hashtagsSet);
        //changed when changing relationship to OneToMany
        //newCourse.setCompetences(competences);
		if (competences != null) {
			newCourse.setCompetences(new ArrayList<>());
			
			for (CourseCompetence cc : competences) {
				cc.setCourse(newCourse);
				newCourse.getCompetences().add(cc);
			}
		}
        newCourse.setStudentsCanAddNewCompetences(studentsCanAddNewCompetences);
        newCourse.setPublished(pubilshed);
        
        newCourse.setBasedOn(basedOn);
        return saveEntity(newCourse);
    }
    
    @Override
    @Transactional (readOnly = false)
    public Course updateCourse(Course course, String title, String description,
            List<CourseCompetence> competences, Collection<Tag> tags, 
            Collection<Tag> hashtags, List<String> blogs, boolean studentsCanAddNewCompetences,
            boolean pubilshed) throws EventException {
        
        course.setTitle(title);
        course.setDescription(description);
        
        Set<Tag> tagsSet = new HashSet<Tag>();
        if (tags != null) {
            tagsSet.addAll(tags);
        }
        
        Set<Tag> hashtagsSet = new HashSet<Tag>();
        if (hashtags != null) {
            hashtagsSet.addAll(hashtags);
        }
        course.setTags(tagsSet);
        course.setHashtags(hashtagsSet);
        course.setCompetences(competences);
        course.setStudentsCanAddNewCompetences(studentsCanAddNewCompetences);
        course.setPublished(pubilshed);
        
        course.getBlogs().clear();
        
        if (blogs != null && !blogs.isEmpty()) {
            for (String blog : blogs) {
                FeedSource feedSource = feedSourceManager.getOrCreateFeedSource(null, blog);
                
                if (!course.getBlogs().contains(feedSource))
                    course.addBlog(feedSource);
            }
        }
        
        course = saveEntity(course);
        
        return course;
    }
    
    @Override
    @Transactional (readOnly = false)
    public AnonUser createAnonUser(String nickname, String name, String avatarUrl, String profileUrl, ServiceType serviceType) {
        AnonUser anonUser = new AnonUser();
        anonUser.setName(name);
        anonUser.setProfileUrl(profileUrl);
        anonUser.setNickname(nickname);
        anonUser.setAvatarUrl(avatarUrl);
        anonUser.setServiceType(serviceType);
        anonUser.setUserType(UserType.TWITTER_USER);
        return saveEntity(anonUser);
    }

    @Override
    @Transactional (readOnly = false)
    public TargetActivity createNewTargetActivity(User maker, String title, String description, 
            AttachmentPreview attachmentPreview, VisibilityType vis,
            Collection<Tag> tags, boolean save) throws EventException {
//        Activity activity = createNewResourceActivity(maker, title, description, attachmentPreview, vis, tags, save);
//        return createNewTargetActivity(activity, maker);
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Event generateEvent(EventType eventType, User actor, Node object, Node target, Node reason, Class<? extends EventObserver>[] observersToExclude) throws EventException {
        if (actor != null && object != null) {
            logger.debug("Generating "+eventType.name()+" " +
                    "event for object " + object.getId() + 
                    (target!=null?" and target "+target.getId():"") + 
                    " created by the user " + actor);
            Event genericEvent = new Event(eventType);
            genericEvent.setActorId(actor.getId());
            genericEvent.setDateCreated(new Date());
            genericEvent.setObject(object);
            genericEvent.setTarget(target);
            genericEvent.setObserversToExclude(observersToExclude);
            genericEvent=saveEntity(genericEvent);
            flush();
            return genericEvent;
        } else
            throw new EventException(
                    "Error occured while creating new "+eventType.name()+" event. Parameters given can not be null.");
    }
    
    @Override
    @Transactional (readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public User updateUserAvatar(User user, InputStream imageInputStream, String avatarFilename) { 
    	if (imageInputStream != null) {
			try {
				user.setAvatarUrl(avatarProcessor.storeUserAvatar(user.getId(), imageInputStream, avatarFilename, true));
				return saveEntity(user);
			} catch (IOException e) {
				logger.error(e);
			}
		} 
		return user;
	}

    @Override
    @Transactional (readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public User createNewUser(String name, String lastname, String emailAddress, boolean emailVerified, 
            String password, String position, boolean system, InputStream avatarStream, String avatarFilename, List<Long> roles) throws EventException {
        
    	emailAddress = emailAddress.toLowerCase();
        
        User user = new User();
        user.setName(name);
        user.setLastname(lastname);
        
        user.setEmail(emailAddress);
        user.setVerified(emailVerified);
        user.setVerificationKey(UUID.randomUUID().toString().replace("-", ""));
        
        if (password != null) {
            user.setPassword(passwordEncrypter.encodePassword(password));
            user.setPasswordLength(password.length());
        }
        
        user.setSystem(system);
        user.setPosition(position);
            
        user.setUserType(UserType.REGULAR_USER);
        if(roles == null) {
        	user.addRole(roleManager.getRoleByName("User"));
        } else {
			for(Long id : roles) {
				Role role = (Role) persistence.currentManager().load(Role.class, id);
				user.addRole(role);
			}
        }
        user = saveEntity(user);
        
        try {
        	if (avatarStream != null) {
        		user.setAvatarUrl(avatarProcessor.storeUserAvatar(user.getId(), avatarStream, avatarFilename, true));
        		user = saveEntity(user);
        	}
		} catch (IOException e) {
			logger.error(e);
		}
    
        this.flush();
        return user;
    }
    
//  private UserDefinedPriority createUserDefinedPriority(UserPriorityType topicPriority) {
//  UserDefinedPriority priority = new UserDefinedPriority();
//  priority.setPriorityType(topicPriority);
//  priority.setPriorityScale(scaleManager.getScale0To5());
//  priority.setPriorityLevel(1);
//  return saveEntity(priority);
//}
    @Override
    @Transactional (readOnly = false)
    public SimpleOutcome createSimpleOutcome(int resultValue, long targetActId, Session session) {
        SimpleOutcome sOutcome=new SimpleOutcome();
        sOutcome.setDateCreated(new Date());
        sOutcome.setResult(resultValue);
        TargetActivity1 ta = (TargetActivity1) session.load(
        		TargetActivity1.class, targetActId);
        sOutcome.setActivity(ta);
        return saveEntity(sOutcome, session);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> enrollUserInCourse(long userId, Course course, TargetLearningGoal targetGoal, String context) throws ResourceCouldNotBeLoadedException {
        return enrollUserInCourseInSameTransaction(userId, course, targetGoal, context);
    }
    
    @Override
    @Transactional(readOnly = false)
    public Map<String, Object> enrollUserInCourseInSameTransaction(long userId, Course course, TargetLearningGoal targetGoal, String context) throws ResourceCouldNotBeLoadedException {
        if (course != null) {
            // if user has previously been enrolled into this course, remove that enrollment
            CourseEnrollment oldCourseEnrollment = courseManager.getCourseEnrollment(userId, course);
            
            if (oldCourseEnrollment != null) {
                courseManager.removeEnrollmentFromCoursePortfolio(userId, oldCourseEnrollment.getId());
            }
            
            Date date = new Date();
//            List<CourseCompetence> courseCompetences = new ArrayList<CourseCompetence>();
            
//            for (CourseCompetence courseCompetence : course.getCompetences()) {
//                CourseCompetence cc = new CourseCompetence();
//                //changed relationship course - coursecompetence
////              cc.setCourse(course);
//                cc.setDateCreated(date);
//                cc.setCompetence(courseCompetence.getCompetence());
//                cc.setDaysOffset(courseCompetence.getDaysOffset());
//                cc.setDuration(courseCompetence.getDuration());
//                cc = saveEntity(cc);
//                
//                courseCompetences.add(cc);
//            }
            
            CourseEnrollment enrollment = new CourseEnrollment();
            enrollment.setCourse(course);
            enrollment.setUser(loadResource(User.class, userId));
            enrollment.setDateStarted(date);
            enrollment.setStatus(Status.ACTIVE);
//            enrollment.setAddedCompetences(courseCompetences);
            enrollment.setTargetGoal(targetGoal);
            enrollment = saveEntity(enrollment);
            
            CoursePortfolio portfolio = courseManager.getOrCreateCoursePortfolio(userId);
            portfolio.addEnrollment(enrollment);
            saveEntity(portfolio);
            
            Map<String, Object> res = null;
            
            if (!course.isManuallyAssignStudentsToInstructors()) {
                List<Long> ids = new ArrayList<>();
                ids.add(enrollment.getId());
                res = assignStudentsToInstructorAutomatically(course.getId(), ids, 0);
            }
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("enrollment", enrollment);
            
            if (res != null) {
                @SuppressWarnings("unchecked")
                Map<Long, Long> assigned = (Map<Long, Long>) res.get("assigned");
                
                if (assigned != null) {
                    resultMap.put("assigned", assigned);
                }
            }
            
            return resultMap;
        }
        return null;
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> enrollUserInCourse(User user, Course course) throws EventException, ResourceCouldNotBeLoadedException {
        
//        TargetLearningGoal targetLGoal = goalManager.createNewCourseBasedLearningGoal(user, course, null, "");
//        
//        //CourseEnrollment enrollment = enrollInCourse(user, course, targetLGoal, null);
//        Map<String, Object> res = enrollUserInCourseInSameTransaction(user, course, targetLGoal, null);
//        CourseEnrollment enrollment = null;
//        
//        if (res != null) {
//            enrollment = (CourseEnrollment) res.get("enrollment");
//        }
//        
//        targetLGoal.setCourseEnrollment(enrollment);
//        targetLGoal = saveEntity(targetLGoal);
//        return res;
        return null;
    }
    
//  @Override
//  @Transactional(readOnly = true)
//  public void assignStudentToInstructorAutomatically(long courseId, long courseEnrollmentId) {
//      List<Map<String, Object>> instructors = courseManager.getCourseInstructors(courseId);
//      
//      if(instructors != null) {
//          BigDecimal min = BigDecimal.ONE;
//          long instructorIdMinOccupied = 0;
//          for(Map<String, Object> map : instructors) {
//              int max = (int) map.get("maxNumberOfStudents");
//              int assigned = (int) map.get("numberOfAssignedStudents");
//              BigDecimal occupiedFactor = BigDecimal.valueOf(assigned).divide(BigDecimal.valueOf(max),
//                      10, RoundingMode.HALF_UP);
//              if(occupiedFactor.compareTo(min) == -1) {
//                  min = occupiedFactor;
//                  instructorIdMinOccupied = (long) map.get("instructorId");
//                  if(occupiedFactor.compareTo(BigDecimal.ZERO) == 0) {
//                      break;
//                  }
//              }
//          }
//          
//          if(min.compareTo(BigDecimal.ONE) != 0) {
//              System.out.println("MIN " + min);
//              CourseInstructor inst = (CourseInstructor) persistence.currentManager()
//                      .load(CourseInstructor.class, instructorIdMinOccupied);
//              CourseEnrollment enr = (CourseEnrollment) persistence.currentManager()
//                      .load(CourseEnrollment.class, courseEnrollmentId);
//              enr.setAssignedToInstructor(true);
//              enr.setInstructor(inst);
//          }
//      }
//  }
    
    @Override
    @Transactional(readOnly = false)
    public Map<String, Object> assignStudentsToInstructorAutomatically(long courseId, List<Long> courseEnrollmentIds,
            long instructorToExcludeId) {
        List<Map<String, Object>> instructors = courseManager.getCourseInstructors(courseId);
        Map<String, Object> resultMap = new HashMap<>();
        Map<Long, Long> assigned = new HashMap<>();
        if(instructors != null && courseEnrollmentIds != null) {
            List<Long> enrollmentIdsCopy = new ArrayList<>(courseEnrollmentIds);
            Iterator<Long> iterator = enrollmentIdsCopy.iterator();
            while(iterator.hasNext()) {
                long eid = iterator.next();
                long instructorId = assignStudentToInstructor(eid, instructors, instructorToExcludeId);
                if(instructorId == 0) {
                    break;
                }
                assigned.put(eid, instructorId);
                iterator.remove();
            }
            if(!enrollmentIdsCopy.isEmpty()) {
                resultMap.put("unassigned", enrollmentIdsCopy);
            }
            resultMap.put("assigned", assigned);
            return resultMap;
        }
        return null;
    }

    private long assignStudentToInstructor(long eid, List<Map<String, Object>> instructors,
            long instructorToExclude) {
        BigDecimal min = BigDecimal.ONE;
        Map<String, Object> minOccupiedInstructor = null;
        for(Map<String, Object> map : instructors) {
            long instructorId= (long) map.get("instructorId");
            if(instructorId != instructorToExclude) {
                int max = (int) map.get("maxNumberOfStudents");
                int assigned = (int) map.get("numberOfAssignedStudents");
                BigDecimal occupiedFactor = BigDecimal.valueOf(assigned).divide(BigDecimal.valueOf(max),
                        10, RoundingMode.HALF_UP);
                if(occupiedFactor.compareTo(min) == -1) {
                    min = occupiedFactor;
                    minOccupiedInstructor = map;
                    if(occupiedFactor.compareTo(BigDecimal.ZERO) == 0) {
                        break;
                    }
                }
            }
        }
        
        if(min.compareTo(BigDecimal.ONE) != 0) {
            System.out.println("MIN " + min);
            int assigned = (int) minOccupiedInstructor.get("numberOfAssignedStudents");
            minOccupiedInstructor.put("numberOfAssignedStudents", assigned + 1);
            long instructorIdMinOccupied = (long) minOccupiedInstructor.get("instructorId");
            CourseInstructor inst = (CourseInstructor) persistence.currentManager()
                    .load(CourseInstructor.class, instructorIdMinOccupied);
            CourseEnrollment enr = (CourseEnrollment) persistence.currentManager()
                    .load(CourseEnrollment.class, eid);
            enr.setAssignedToInstructor(true);
            enr.setInstructor(inst);
            return instructorIdMinOccupied;
        }
        return 0;
    }
    
    @Override
    @Transactional (readOnly = false)
    public Course updateCourse(long courseId, String title, String description, Collection<Tag> tags, 
            Collection<Tag> hashtags, boolean published) throws DbConnectionException {
        try {
            Course course = (Course) persistence.currentManager().load(Course.class, courseId);
            course.setTitle(title);
            course.setDescription(description);
            
            Set<Tag> tagsSet = new HashSet<Tag>();
            if (tags != null) {
                tagsSet.addAll(tags);
            }
            
            Set<Tag> hashtagsSet = new HashSet<Tag>();
            if (hashtags != null) {
                hashtagsSet.addAll(hashtags);
            }
            course.setTags(tagsSet);
            course.setHashtags(hashtagsSet);
        
            course.setPublished(published);
        
            return saveEntity(course);
        } catch(Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while updating course");
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Activity createNewActivity(ActivityData activityData) throws DbConnectionException {
        try {
        	ActivityMapper mapper = ActivityMapperFactory.getActivityMapper(activityData);
        	if(mapper != null) {
        		Activity activity = mapper.mapToActivity();
            
        		return saveEntity(activity);
        	}
        	return null;
        } catch(Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while creating new activity");
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void deleteCompetenceActivityInSeparateTransaction(long competenceActivityId) 
    		throws DbConnectionException {
    	try {
    		deleteById(CompetenceActivity.class, competenceActivityId, 
					persistence.currentManager());
    	} catch(Exception e) {
    		throw new DbConnectionException("Error while deleting competence activity");
    	}
    }
    
    @Override
	@Transactional (readOnly = true)
	public String getLinkForObjectType(String simpleClassName, long id, String linkField) 
			throws DbConnectionException {
		try{
			String query = String.format(
				"SELECT obj.%1$s " +
				"FROM %2$s obj " +
				"WHERE obj.id = :id",
				linkField, simpleClassName);
			
			String link = (String) persistence.currentManager().createQuery(query)
				.setLong("id", id)
				.uniqueResult();
			
			return link;
		}catch(Exception e){
			throw new DbConnectionException("Error while loading learning goals");
		}
	}
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Credential1 createCredential(String title, String description, String tagsString, 
    		String hashtagsString, long creatorId, LearningResourceType type, 
    		boolean compOrderMandatory, boolean published, long duration, 
    		boolean manuallyAssign, List<CompetenceData1> comps, Date scheduledDate) {
    	try {
			 Credential1 cred = new Credential1();
		     cred.setCreatedBy(loadResource(User.class, creatorId));
		     cred.setType(type);
		     cred.setTitle(title);
		     cred.setDescription(description);
		     cred.setDateCreated(new Date());
		     cred.setCompetenceOrderMandatory(compOrderMandatory);
		     cred.setPublished(published);
		     cred.setDuration(duration);
		     //cred.setVisible(visible);
		     cred.setScheduledPublishDate(scheduledDate);
		     cred.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(tagsString)));
		     cred.setHashtags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(hashtagsString)));
		     cred.setManuallyAssignStudents(manuallyAssign);
		     
		     saveEntity(cred);
		     
		     if(comps != null) {
				for(CompetenceData1 cd : comps) {
					CredentialCompetence1 cc = new CredentialCompetence1();
					cc.setOrder(cd.getOrder());
					cc.setCredential(cred);
					Competence1 comp = (Competence1) persistence.currentManager().load(
							Competence1.class, cd.getCompetenceId());
					cc.setCompetence(comp);
					saveEntity(cc);
				}
			 }
		
		     logger.info("New credential is created with id " + cred.getId());
		     return cred;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving credential");
    	}
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Result<Competence1> createCompetence(String title, String description, String tagsString, long creatorId,
			boolean studentAllowedToAddActivities, LearningResourceType type, boolean published, 
			long duration, List<org.prosolo.services.nodes.data.ActivityData> activities, 
			long credentialId) {
    	try {
    		 Result<Competence1> result = new Result<>();
			 Competence1 comp = new Competence1();
			 comp.setTitle(title);
			 comp.setDateCreated(new Date());
			 comp.setDescription(description);
		     comp.setCreatedBy(loadResource(User.class, creatorId));
		     comp.setStudentAllowedToAddActivities(studentAllowedToAddActivities);
		     comp.setType(type);
		     comp.setPublished(published);
		     comp.setDuration(duration);
		     comp.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(tagsString)));
		     saveEntity(comp);
		     
		     if(activities != null) {
				for(org.prosolo.services.nodes.data.ActivityData bad : activities) {
					CompetenceActivity1 ca = new CompetenceActivity1();
					ca.setOrder(bad.getOrder());
					ca.setCompetence(comp);
					Activity1 act = (Activity1) persistence.currentManager().load(
							Activity1.class, bad.getActivityId());
					ca.setActivity(act);
					saveEntity(ca);
				}
			 }
				
		     if(credentialId > 0) {
		    	 List<EventData> events = credentialManager.addCompetenceToCredential(credentialId, comp, 
		    			 creatorId);
		    	 result.addEvents(events);
		     }
		
		     logger.info("New competence is created with id " + comp.getId());
		     result.setResult(comp);
		     return result;
   	} catch(Exception e) {
   		e.printStackTrace();
   		logger.error(e);
   		throw new DbConnectionException("Error while saving competence");
   	}
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Result<Credential1> updateCredential(CredentialData data, long creatorId) {
    	return credentialManager.updateCredentialData(data, creatorId);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Competence1 updateCompetence(CompetenceData1 data, long userId) {
    	return competenceManager.updateCompetenceData(data, userId);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public long deleteCredentialBookmark(long credId, long userId) {
    	return credentialManager.deleteCredentialBookmark(credId, userId);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public CredentialBookmark bookmarkCredential(long credId, long userId) 
			throws DbConnectionException {
    	return credentialManager.bookmarkCredential(credId, userId);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Result<Activity1> createActivity(org.prosolo.services.nodes.data.ActivityData data, 
    		long userId) throws DbConnectionException {
    	try {
    		Result<Activity1> result = new Result<>();
    		Activity1 activity = activityFactory.getActivityFromActivityData(data);
			
    		if (data.getLinks() != null) {
    			Set<ResourceLink> activityLinks = new HashSet<>();
    			
				for (ResourceLinkData rl : data.getLinks()) {
    				ResourceLink link = new ResourceLink();
    				link.setLinkName(rl.getLinkName());
    				link.setUrl(rl.getUrl());
    				if (rl.getIdParamName() != null && !rl.getIdParamName().isEmpty()) {
    					link.setIdParameterName(rl.getIdParamName());
    				}
    				saveEntity(link);
    				activityLinks.add(link);
    			}
    			activity.setLinks(activityLinks);
    		}
    		
    		Set<ResourceLink> activityFiles = new HashSet<>();
    		
			if (data.getFiles() != null) {
				for (ResourceLinkData rl : data.getFiles()) {
    				ResourceLink link = new ResourceLink();
    				link.setLinkName(rl.getLinkName());
    				link.setUrl(rl.getUrl());
    				saveEntity(link);
    				activityFiles.add(link);
    			}
    			activity.setFiles(activityFiles);
    		}
			
			if(data.getActivityType() == org.prosolo.services.nodes.data.ActivityType.VIDEO) {
				Set<ResourceLink> captions = new HashSet<>();
	    		
				if (data.getCaptions() != null) {
					for (ResourceLinkData rl : data.getCaptions()) {
	    				ResourceLink link = new ResourceLink();
	    				link.setLinkName(rl.getLinkName());
	    				link.setUrl(rl.getUrl());
	    				saveEntity(link);
	    				captions.add(link);
	    			}
	    			((UrlActivity1) activity).setCaptions(captions);
	    		}
			}
   
    		User creator = (User) persistence.currentManager().load(User.class, userId);
    		activity.setCreatedBy(creator);
    		
    		//GradingOptions go = new GradingOptions();
    		//go.setMinGrade(0);
    		//go.setMaxGrade(data.getMaxPointsString().isEmpty() ? 0 : Integer.parseInt(data.getMaxPointsString()));
    		//saveEntity(go);
    		//activity.setGradingOptions(go);
    		activity.setMaxPoints(data.getMaxPointsString().isEmpty() ? 0 : Integer.parseInt(data.getMaxPointsString()));
    		
    		activity.setStudentCanSeeOtherResponses(data.isStudentCanSeeOtherResponses());
    		activity.setStudentCanEditResponse(data.isStudentCanEditResponse());
    		
    		saveEntity(activity);
    		
    		if(data.getCompetenceId() > 0) {
				EventData ev = competenceManager.addActivityToCompetence(data.getCompetenceId(), 
						activity, userId);
				if(ev != null) {
		    		 result.addEvent(ev);
		    	}
			}
    		result.setResult(activity);
    		return result;
    	} catch(Exception e) {
    		logger.error(e);
    		e.printStackTrace();
    		throw new DbConnectionException("Error while saving activity");
    	}
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Activity1 updateActivity(org.prosolo.services.nodes.data.ActivityData data, long userId) 
			throws DbConnectionException {
    	return activityManager.updateActivityData(data, userId);
    }
    
    @Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public Comment1 saveNewComment(CommentData data, long userId, CommentedResourceType resource) 
			throws DbConnectionException {
		try {
			Comment1 comment = new Comment1();
			comment.setDescription(data.getComment());
			comment.setCommentedResourceId(data.getCommentedResourceId());
			comment.setResourceType(resource);
			comment.setInstructor(data.isInstructor());
			//comment.setDateCreated(data.getDateCreated());
			comment.setPostDate(data.getDateCreated());
			User user = (User) persistence.currentManager().load(User.class, userId);
			comment.setUser(user);
			if(data.getParent() != null) {
				Comment1 parent = (Comment1) persistence.currentManager().load(Comment1.class, 
						data.getParent().getCommentId());
				comment.setParentComment(parent);
			}
			
			saveEntity(comment);
			
			return comment;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving comment");
		}
		
	}
    
    @Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public PostSocialActivity1 createNewPost(long userId, String text, RichContent1 richContent) 
			throws DbConnectionException {
		try {
			User user = (User) persistence.currentManager().load(User.class, userId);
			PostSocialActivity1 post = new PostSocialActivity1();
			post.setDateCreated(new Date());
			post.setLastAction(new Date());
			post.setActor(user);
			post.setText(text);
			post.setRichContent(richContent);
			post = saveEntity(post);
			
			return post;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving new post");
		}
		
	}
    
    @Override
   	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
   	public PostReshareSocialActivity sharePost(long userId, String text, long socialActivityId) 
   			throws DbConnectionException {
   		try {
   			User user = (User) persistence.currentManager().load(User.class, userId);
   			PostSocialActivity1 post = (PostSocialActivity1) persistence.currentManager().load(
   					PostSocialActivity1.class, socialActivityId);
   			PostReshareSocialActivity postShare = new PostReshareSocialActivity();
   			postShare.setDateCreated(new Date());
   			postShare.setLastAction(new Date());
   			postShare.setActor(user);
   			postShare.setText(text);
   			postShare.setPostObject(post);
   			postShare = saveEntity(postShare);
   			
   			//post.setShareCount(post.getShareCount() + 1);
   			
   			return postShare;
   		} catch(Exception e) {
   			logger.error(e);
   			e.printStackTrace();
   			throw new DbConnectionException("Error while saving new post");
   		}
   		
   	}
    
    @Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public PostSocialActivity1 updatePost(long postId, String newText) throws DbConnectionException {
		try {
			PostSocialActivity1 post = (PostSocialActivity1) persistence.currentManager()
					.load(PostSocialActivity1.class, postId);
			post.setLastAction(new Date());
			post.setText(newText);
			return post;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating post");
		}
		
	}
    
    @Override
	@Transactional (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public User updateUser(long userId, String name, String lastName, String email,
			boolean emailVerified, boolean changePassword, String password, 
			String position, List<Long> roles) throws DbConnectionException {
		try {
			User user = loadResource(User.class, userId);
			user.setName(name);
			user.setLastname(lastName);
			user.setPosition(position);
			user.setEmail(email);
			user.setVerified(true);
			
			if (changePassword) {
				user.setPassword(passwordEncrypter.encodePassword(password));
				user.setPasswordLength(password.length());
			}
			
			if(roles != null) {
				user.getRoles().clear();
				for(Long id : roles) {
					Role role = (Role) persistence.currentManager().load(Role.class, id);
					user.addRole(role);
				}
			}
			return user;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while updating user data");
		}
	}
    
    @Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public UserGroup updateGroupName(long groupId, String newName) throws DbConnectionException {
		try {
			UserGroup group = (UserGroup) persistence.currentManager().load(UserGroup.class, groupId);
			group.setName(newName);
			
			return group;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while saving user group");
		}
	}
    
    @Override
   	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public UserGroup updateGroupJoinUrl(long groupId, boolean joinUrlActive, String joinUrlPassword)
    		throws DbConnectionException {
    	try {
			UserGroup group = (UserGroup) persistence.currentManager().load(UserGroup.class, groupId);
			group.setJoinUrlActive(joinUrlActive);
			
			if (joinUrlActive) {
				group.setJoinUrlPassword(joinUrlPassword);
			} else {
				group.setJoinUrlPassword(null);
			}
			
			return group;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while saving user group");
		}
    }

    @Override
	@Transactional (readOnly = false)
	public UserGroup saveNewGroup(String name, boolean isDefault) throws DbConnectionException {
		try {
			UserGroup group = new UserGroup();
			group.setDateCreated(new Date());
			group.setDefaultGroup(isDefault);
			group.setName(name);
			
			saveEntity(group);
			return group;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while saving user group");
		}
	}
    
}
