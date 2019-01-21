package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.activitywall.PostReshareSocialActivity;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.content.RichContent1;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.common.domainmodel.user.AnonUser;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;

import java.io.InputStream;
import java.util.List;

public interface ResourceFactory extends AbstractManager {

    Role createNewRole(String name, String description, boolean systemDefined);

    AnonUser createAnonUser(String nickname, String name, String avatarUrl, String profileUrl, ServiceType twitter);

    SimpleOutcome createSimpleOutcome(int resultValue, long targetActId, Session session);
    
    String getLinkForObjectType(String simpleClassName, long id, String linkField) 
			throws DbConnectionException;

	Result<Credential1> updateCredential(CredentialData data, UserContextData context) throws StaleDataException, IllegalDataStateException;

	Result<Competence1> updateCompetence(CompetenceData1 data, UserContextData context) throws StaleDataException,
			IllegalDataStateException;

	Activity1 updateActivity(org.prosolo.services.nodes.data.ActivityData data) 
			throws DbConnectionException, StaleDataException, IllegalDataStateException;
	
	User updateUserAvatar(User user, InputStream imageInputStream, String avatarFilename);
	
	PostSocialActivity1 createNewPost(long userId, String text, RichContent1 richContent) 
			throws DbConnectionException;
	
	PostSocialActivity1 updatePost(long postId, String newText) throws DbConnectionException;
	
	PostReshareSocialActivity sharePost(long userId, String text, long socialActivityId) 
   			throws DbConnectionException;
	
	UserGroup updateGroupJoinUrl(long groupId, boolean joinUrlActive, String joinUrlPassword) throws DbConnectionException;
	
}