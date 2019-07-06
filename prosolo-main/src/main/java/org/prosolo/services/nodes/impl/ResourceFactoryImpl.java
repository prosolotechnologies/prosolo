package org.prosolo.services.nodes.impl;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.activitywall.PostReshareSocialActivity;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.content.RichContent1;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.common.domainmodel.user.AnonUser;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.upload.AvatarProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author Nikola Milikic
 *
 */
@Service("org.prosolo.services.nodes.ResourceFactory")
public class ResourceFactoryImpl extends AbstractManagerImpl implements ResourceFactory {

    private static final long serialVersionUID = 2968104792929090003L;

    @Inject private CredentialManager credentialManager;
    @Inject private Competence1Manager competenceManager;
    @Inject private Activity1Manager activityManager;
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
    public SimpleOutcome createSimpleOutcome(int resultValue, long targetActId, Session session) {
        SimpleOutcome sOutcome=new SimpleOutcome();
        sOutcome.setDateCreated(new Date());
        sOutcome.setResult(resultValue);
        TargetActivity1 ta = (TargetActivity1) session.load(
                TargetActivity1.class, targetActId);
        sOutcome.setActivity(ta);
        return saveEntity(sOutcome, session);
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
			throw new DbConnectionException("Error loading learning goals");
		}
	}

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Result<Credential1> updateCredential(CredentialData data, UserContextData context)
            throws StaleDataException, IllegalDataStateException {
        return credentialManager.updateCredentialData(data, context);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Result<Competence1> updateCompetence(CompetenceData1 data, UserContextData context) throws StaleDataException,
            IllegalDataStateException {
        return competenceManager.updateCompetenceData(data, context);
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
            throw new DbConnectionException("Error saving new post");
        }

    }

}
