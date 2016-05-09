package org.prosolo.services.nodes.impl;

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

import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.common.domainmodel.activities.ExternalToolActivity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.CompetenceType;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.course.CourseInstructor;
import org.prosolo.common.domainmodel.course.CoursePortfolio;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.course.Status;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialBookmark;
import org.prosolo.common.domainmodel.credential.CredentialType1;
import org.prosolo.common.domainmodel.credential.ResourceLink;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.common.domainmodel.user.AnonUser;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.spring.TransactionDebugUtil;
import org.prosolo.services.authentication.PasswordEncrypter;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.feeds.FeedSourceManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.LearningGoalManager;
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
import org.prosolo.web.competences.data.ActivityFormData;
import org.prosolo.web.competences.data.ActivityType;
import org.prosolo.web.util.AvatarUtils;
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
    @Inject private LearningGoalManager goalManager;
    @Inject private CredentialManager credentialManager;
    @Inject private Competence1Manager competenceManager;
    @Inject private Activity1Manager activityManager;
    @Inject private ActivityDataFactory activityFactory;
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Role createNewRole(String name, String description, boolean systemDefined, List<Long> capabilities){
        Role role = new Role();
        role.setTitle(name);
        role.setDescription(description);
        role.setDateCreated(new Date());
        role.setSystem(systemDefined);
        role = saveEntity(role);
        for(long capId:capabilities){
            Capability cap = (Capability) persistence.currentManager().load(Capability.class, capId);
            cap.getRoles().add(role);
            saveEntity(cap);
        }
        return role;
    }

    @Override
//  @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Transactional(readOnly = false)
    public LearningGoal createNewLearningGoal(User currentUser,
            String name, String description, Date deadline, 
            Collection<Tag> tags,
            Collection<Tag> hashtags) throws EventException {
            
        TransactionDebugUtil.transactionRequired("ResourceFactory.createNewLearningGoal");
        
        Date currentTime = new Date();
        
        LearningGoal newLearningGoal = new LearningGoal();
        newLearningGoal.setTitle(name);
        newLearningGoal.setDescription(description);
        newLearningGoal.setMaker(currentUser);
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
    public TargetCompetence createNewTargetCompetence(User user, Competence comp, VisibilityType visibility) {
        comp = merge(comp);
        
        TargetCompetence newTargetCompetence = new TargetCompetence();
        newTargetCompetence.setDateCreated(new Date());
        newTargetCompetence.setDateStarted(new Date());
        newTargetCompetence.setDateCreated(new Date());
        newTargetCompetence.setCompetence(comp);
        newTargetCompetence.setMaker(user);
        newTargetCompetence.setVisibility(visibility);
        newTargetCompetence.setTitle(comp.getTitle());
        newTargetCompetence.setDescription(comp.getDescription());
        newTargetCompetence = saveEntity(newTargetCompetence);
        
        List<CompetenceActivity> activities = comp.getActivities();
        
        if (activities != null && !activities.isEmpty()) {
            for (CompetenceActivity compActivity : activities) {
                TargetActivity targetActivity = createNewTargetActivity(compActivity.getActivity(), user);
                targetActivity.setTaPosition(compActivity.getActivityPosition());
                targetActivity.setParentCompetence(newTargetCompetence);
                targetActivity = saveEntity(targetActivity);
                
                newTargetCompetence.getTargetActivities().add(targetActivity);
            }
        }
        
        return saveEntity(newTargetCompetence);
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Competence createCompetence(User user, String title,
            String description, int validity, int duration, Collection<Tag> tags, 
            List<Competence> prerequisites, List<Competence> corequisites, Date dateCreated) {
        Competence newCompetence = new Competence();
        newCompetence.setMaker(user);
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
    public TargetActivity createNewTargetActivity(Activity activity, User maker) {
        TargetActivity targetActivity = new TargetActivity();
        targetActivity.setMaker(maker);
        targetActivity.setActivity(activity);
        targetActivity.setDateCreated(new Date());
        targetActivity.setTitle(activity.getTitle());
        
        if (activity instanceof UploadAssignmentActivity) {
            UploadAssignmentActivity uploadActivity = (UploadAssignmentActivity) activity;
            
            targetActivity.setVisibility(uploadActivity.isVisibleToEveryone() ? VisibilityType.PUBLIC : VisibilityType.PRIVATE);
        }
        return saveEntity(targetActivity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Activity createNewResourceActivity(User currentUser, 
            String title, String description,
            AttachmentPreview attachmentPreview,
            VisibilityType vis,
            Collection<Tag> tags,
            boolean save) throws EventException {
        
        RichContent richContent = postManager.createRichContent(attachmentPreview);
        
        ResourceActivity activity = new ResourceActivity();
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setRichContent(richContent);
        activity.setMaker(currentUser);
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
    public Activity createNewActivity(User currentUser,  ActivityFormData activityFormData, VisibilityType vis)
            throws EventException {

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
        activity.setMaker(currentUser);
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
            Collection<Tag> tags, Collection<Tag> hashtags, User maker,
            CreatorType creatorType, boolean studentsCanAddNewCompetences, boolean pubilshed) {
        
        Course newCourse = new Course();
        Date now = new Date();
        newCourse.setTitle(title);
        newCourse.setDescription(description);
        newCourse.setDateCreated(now);
        newCourse.setMaker(maker);
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
        Activity activity = createNewResourceActivity(maker, title, description, attachmentPreview, vis, tags, save);
        return createNewTargetActivity(activity, maker);
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
            genericEvent.setActor(actor);
            genericEvent.setDateCreated(new Date());
            genericEvent.setObject(object);
            genericEvent.setTarget(target);
            genericEvent.setReason(reason);
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
    public User createNewUser(String name, String lastname, String emailAddress, boolean emailVerified, 
            String password, String position, boolean system) throws EventException {
        
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
        
        user.setAvatarUrl(AvatarUtils.getDefaultAvatarUrl());
        user.setSystem(system);
        user.setPosition(position);
            
        user.setUserType(UserType.REGULAR_USER);
        user.addRole(roleManager.getRoleByName("User"));
        user = saveEntity(user);
    
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
    public SimpleOutcome createSimpleOutcome(double resultValue){
        SimpleOutcome sOutcome=new SimpleOutcome();
        sOutcome.setDateCreated(new Date());
        sOutcome.setResult(resultValue);
        return saveEntity(sOutcome);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> enrollUserInCourse(User user, Course course, TargetLearningGoal targetGoal, String context) {
        return enrollUserInCourseInSameTransaction(user, course, targetGoal, context);
    }
    
    @Override
    @Transactional(readOnly = false)
    public Map<String, Object> enrollUserInCourseInSameTransaction(User user, Course course, TargetLearningGoal targetGoal, String context) {
        if (course != null) {
            // if user has previously been enrolled into this course, remove that enrollment
            CourseEnrollment oldCourseEnrollment = courseManager.getCourseEnrollment(user, course);
            
            if (oldCourseEnrollment != null) {
                courseManager.removeEnrollmentFromCoursePortfolio(user, oldCourseEnrollment.getId());
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
            enrollment.setUser(user);
            enrollment.setDateStarted(date);
            enrollment.setStatus(Status.ACTIVE);
//            enrollment.setAddedCompetences(courseCompetences);
            enrollment.setTargetGoal(targetGoal);
            enrollment = saveEntity(enrollment);
            
            CoursePortfolio portfolio = courseManager.getOrCreateCoursePortfolio(user);
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
        
        TargetLearningGoal targetLGoal = goalManager.createNewCourseBasedLearningGoal(user, course, null, "");
        
        //CourseEnrollment enrollment = enrollInCourse(user, course, targetLGoal, null);
        Map<String, Object> res = enrollUserInCourseInSameTransaction(user, course, targetLGoal, null);
        CourseEnrollment enrollment = null;
        
        if (res != null) {
            enrollment = (CourseEnrollment) res.get("enrollment");
        }
        
        targetLGoal.setCourseEnrollment(enrollment);
        targetLGoal = saveEntity(targetLGoal);
        return res;
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
    @Transactional(readOnly = false)
    public Credential1 createCredential(String title, String description, Set<Tag> tags, 
    		Set<Tag> hashtags, User createdBy, CredentialType1 type, 
    		boolean compOrderMandatory, boolean published, long duration) {
    	try {
			 Credential1 cred = new Credential1();
		     cred.setCreatedBy(createdBy);
		     cred.setType(type);
		     cred.setTitle(title);
		     cred.setDescription(description);
		     cred.setDateCreated(new Date());
		     cred.setTags(tags);		     
		     cred.setHashtags(hashtags);
		     cred.setCompetenceOrderMandatory(compOrderMandatory);
		     cred.setPublished(published);
		     cred.setDuration(duration);
		     
		     saveEntity(cred);
		
		     logger.info("New credential is created with id " + cred.getId());
		     return cred;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving credential");
    	}
    }
    
    public Competence1 createCompetence(String title, String description, Set<Tag> tags, User createdBy,
			boolean studentAllowedToAddActivities, boolean published, long duration) {
    	try {
			 Competence1 comp = new Competence1();
			 comp.setTitle(title);
			 comp.setDateCreated(new Date());
			 comp.setDescription(description);
			 comp.setTags(tags);
		     comp.setCreatedBy(createdBy);
		     comp.setStudentAllowedToAddActivities(studentAllowedToAddActivities);
		     comp.setPublished(published);
		     comp.setDuration(duration);
		     saveEntity(comp);
		
		     logger.info("New competence is created with id " + comp.getId());
		     return comp;
   	} catch(Exception e) {
   		e.printStackTrace();
   		logger.error(e);
   		throw new DbConnectionException("Error while saving competence");
   	}
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Credential1 updateCredential(CredentialData data) {
    	return credentialManager.updateCredential(data);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Competence1 updateCompetence(CompetenceData1 data) {
    	return competenceManager.updateCompetence(data);
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
    public Activity1 createActivity(org.prosolo.services.nodes.data.ActivityData activityData, 
    		long userId) throws DbConnectionException {
    	try {
    		Activity1 act = activityFactory.getActivityFromActivityData(activityData);
    		if(activityData.getLinks() != null) {
    			Set<ResourceLink> activityLinks = new HashSet<>();
    			for(ResourceLinkData rl : activityData.getLinks()) {
    				ResourceLink link = new ResourceLink();
    				link.setLinkName(rl.getLinkName());
    				link.setUrl(rl.getUrl());
    				saveEntity(link);
    				activityLinks.add(link);
    			}
    			act.setLinks(activityLinks);
    		}
    		
    		Set<ResourceLink> activityFiles = new HashSet<>();
    		if(activityData.getFiles() != null) {		
    			for(ResourceLinkData rl : activityData.getFiles()) {
    				ResourceLink link = new ResourceLink();
    				link.setLinkName(rl.getLinkName());
    				link.setUrl(rl.getUrl());
    				saveEntity(link);
    				activityFiles.add(link);
    			}
    			act.setFiles(activityFiles);
    		}
   
    		User creator = (User) persistence.currentManager().load(User.class, userId);
    		act.setCreatedBy(creator);
    		saveEntity(act);
    		
    		if(activityData.getCompetenceId() > 0) {
				competenceManager.addActivityToCompetence(activityData.getCompetenceId(), act);
			}
    		
    		return act;
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
}
