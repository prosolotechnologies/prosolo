package org.prosolo.services.nodes.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.OrganizationalUnit;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.common.domainmodel.user.AnonUser;
import org.prosolo.common.domainmodel.user.Email;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.ServiceType;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserDefinedPriority;
import org.prosolo.common.domainmodel.user.UserPriorityType;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;
import org.prosolo.common.domainmodel.user.preferences.RecommendationPreferences;
import org.prosolo.common.domainmodel.user.preferences.TopicPreference;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.core.spring.TransactionDebugUtil;
import org.prosolo.services.authentication.PasswordEncrypter;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.feeds.FeedSourceManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.ScaleManager;
import org.prosolo.web.activitywall.data.AttachmentPreview;
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
	@Autowired private ScaleManager scaleManager;
	@Autowired private FeedSourceManager feedSourceManager;
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Role createNewRole(String name, boolean systemDefined, List<Long> capabilities){
		Role role = new Role();
		role.setTitle(name);
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
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Organization createNewOrganization(User currentUser, String name, String abbreviatedName, String description){
		Organization organization = new Organization();
		organization.setName(name);
		organization.setTitle(name);
		organization.setAbbreviatedName(abbreviatedName);
		organization.setDateCreated(new Date());
		organization.setDescription(description);
		organization = saveEntity(organization);
		return organization;
		//should this generate any events?
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public OrganizationalUnit createNewOrganizationalUnit(Organization organization, String name, String description, boolean system){
		OrganizationalUnit orgUnit = new OrganizationalUnit();
		orgUnit.setTitle(name);
		orgUnit.setOrganization(organization);
		orgUnit.setDescription(description);
		orgUnit.setSystem(system);
		return saveEntity(orgUnit);
	}
	
	@Override
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
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
		newCourse.setCompetences(competences);
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
					"event for object "	+ object.getId() + 
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
	@Transactional (readOnly = false)//, propagation = Propagation.REQUIRES_NEW)
	public User createNewUser(String name, String lastname, String emailAddress, boolean emailVerified, 
			String password, Organization organization, String position, boolean system) throws EventException {
		
		emailAddress = emailAddress.toLowerCase();
		
		Email email = new Email();
		email.setAddress(emailAddress);
		email.setDefaultEmail(true);
		email.setVerified(emailVerified);
		email.setVerificationKey(UUID.randomUUID().toString().replace("-", ""));
		email = saveEntity(email);
		
		User user = new User();
		user.setName(name);
		user.setLastname(lastname);
		user.setEmail(email);
		
		if (password != null) {
			user.setPassword(passwordEncrypter.encodePassword(password));
			user.setPasswordLength(password.length());
		}
		
		user.setAvatarUrl(AvatarUtils.getDefaultAvatarUrl());
		user.setSystem(system);
		user.setOrganization(organization);
		user.setPosition(position);
			
		user.setUserType(UserType.REGULAR_USER);
		user.addRole(roleManager.getRoleByName("User"));
		user = saveEntity(user);
	
		RecommendationPreferences recPref = new RecommendationPreferences();
		recPref.addUserPriority(createUserDefinedPriority(UserPriorityType.TOPIC_PRIORITY));
		recPref.addUserPriority(createUserDefinedPriority(UserPriorityType.LEARNING_GOAL_PRIORITY));
		recPref.addUserPriority(createUserDefinedPriority(UserPriorityType.LEARNING_HISTORY_PRIORITY));
		recPref.setUser(user);
		recPref = saveEntity(recPref);
		
		FeedsPreferences feedsPreferences = new FeedsPreferences();
		feedsPreferences.setUser(user);
		feedsPreferences = saveEntity(feedsPreferences);

		TopicPreference tPref = new TopicPreference();
		tPref.setUser(user);
		tPref = saveEntity(tPref);
		this.flush();
		return user;
	}
	
	private UserDefinedPriority createUserDefinedPriority(UserPriorityType topicPriority) {
		UserDefinedPriority priority = new UserDefinedPriority();
		priority.setPriorityType(topicPriority);
		priority.setPriorityScale(scaleManager.getScale0To5());
		priority.setPriorityLevel(1);
		return saveEntity(priority);
	}
	@Override
	@Transactional (readOnly = false)
	public SimpleOutcome createSimpleOutcome(double resultValue){
		SimpleOutcome sOutcome=new SimpleOutcome();
		sOutcome.setDateCreated(new Date());
		sOutcome.setResult(resultValue);
		return saveEntity(sOutcome);
	}
}
