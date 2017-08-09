package org.prosolo.services.nodes.impl;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.activitywall.PostReshareSocialActivity;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.content.RichContent1;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.common.domainmodel.user.AnonUser;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.authentication.PasswordEncrypter;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.factory.ActivityDataFactory;
import org.prosolo.services.upload.AvatarProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Nikola Milikic
 *
 */
@Service("org.prosolo.services.nodes.ResourceFactory")
public class ResourceFactoryImpl extends AbstractManagerImpl implements ResourceFactory {

    private static final long serialVersionUID = 2968104792929090003L;

    @Autowired private PasswordEncrypter passwordEncrypter;
    @Autowired private RoleManager roleManager;
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
    public User createNewUser(long organizationId, String name, String lastname, String emailAddress, boolean emailVerified,
                              String password, String position, boolean system, InputStream avatarStream, String avatarFilename, List<Long> roles) throws EventException {

        emailAddress = emailAddress.toLowerCase();

        User user = new User();
        user.setName(name);
        user.setLastname(lastname);

        user.setEmail(emailAddress);
        user.setVerified(emailVerified);
        user.setVerificationKey(UUID.randomUUID().toString().replace("-", ""));

        if (organizationId > 0) {
            user.setOrganization((Organization) persistence.currentManager().load(Organization.class, organizationId));
        }

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

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Credential1 createCredential(String title, String description, String tagsString,
                                        String hashtagsString, long creatorId, boolean compOrderMandatory, long duration,
                                        boolean manuallyAssign, List<CompetenceData1> comps) throws DbConnectionException {
        try {
            Credential1 cred = new Credential1();
            cred.setCreatedBy(loadResource(User.class, creatorId));
            cred.setType(CredentialType.Original);
            cred.setTitle(title);
            cred.setDescription(description);
            cred.setDateCreated(new Date());
            cred.setCompetenceOrderMandatory(compOrderMandatory);
            cred.setDuration(duration);
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
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            throw new DbConnectionException("Error while saving competency");
        }
    }

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
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Result<Credential1> updateCredential(CredentialData data, long creatorId) throws StaleDataException,
            IllegalDataStateException {
        return credentialManager.updateCredentialData(data, creatorId);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Competence1 updateCompetence(CompetenceData1 data, long userId) throws StaleDataException,
            IllegalDataStateException {
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
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Activity1 updateActivity(org.prosolo.services.nodes.data.ActivityData data)
            throws DbConnectionException, StaleDataException {
        return activityManager.updateActivityData(data);
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
            comment.setManagerComment(data.isManagerComment());
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
                           String position, List<Long> roles, List<Long> rolesToUpdate) throws DbConnectionException {
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
                Set<Long> rolesToAdd = new HashSet<>(roles);
                /*
                roles that should be deleted (if user had them) are all roles that should be updated
                except for roles that should be added
                 */
                Set<Long> rolesToDelete = new HashSet<>(rolesToUpdate);
                rolesToDelete.removeAll(rolesToAdd);

                //update only roles that should be updated based on a rolesToUpdate argument
                Iterator<Role> roleIterator = user.getRoles().iterator();
                while (roleIterator.hasNext()) {
                    Role r = roleIterator.next();
                    boolean keepRole = rolesToAdd.remove(r.getId());
                    if (!keepRole) {
                        if (rolesToDelete.contains(r.getId())) {
                            roleIterator.remove();
                        }
                    }
                }
                //assign new roles to user
                for (Long roleId : rolesToAdd) {
                    Role role = (Role) persistence.currentManager().load(Role.class, roleId);
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

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Result<Competence1> duplicateCompetence(long compId, long userId)
            throws DbConnectionException {
        try {
            Competence1 comp = (Competence1) persistence.currentManager().get(Competence1.class, compId);
            User user = (User) persistence.currentManager().load(User.class, userId);

            Competence1 competence = new Competence1();

            competence.setTitle("Copy of " + comp.getTitle());
            competence.setDescription(comp.getDescription());
            competence.setDateCreated(new Date());
            competence.setTags(new HashSet<Tag>(comp.getTags()));
            competence.setCreatedBy(user);
            competence.setDuration(comp.getDuration());
            competence.setStudentAllowedToAddActivities(comp.isStudentAllowedToAddActivities());
            competence.setType(comp.getType());
            competence.setOriginalVersion(comp);
            competence.setArchived(false);
            competence.setPublished(false);
            saveEntity(competence);

            Result<Competence1> res = new Result<>();
            res.setResult(competence);
            EventData ev = new EventData();
            ev.setEventType(EventType.Create);
            ev.setActorId(userId);
            Competence1 c = new Competence1();
            c.setId(competence.getId());
            ev.setObject(c);
            res.addEvent(ev);

            List<CompetenceActivity1> activities = comp.getActivities();

            for (CompetenceActivity1 compActivity : activities) {
                Result<CompetenceActivity1> actRes = activityManager.cloneActivity(compActivity, competence.getId(),
                        userId, null);
                competence.getActivities().add(actRes.getResult());
                res.addEvents(actRes.getEvents());
            }
            return res;
        } catch(Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while creating competence duplicate");
        }
    }

}
