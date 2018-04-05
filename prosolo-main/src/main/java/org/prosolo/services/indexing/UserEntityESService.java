package org.prosolo.services.indexing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.elasticsearch.AbstractESIndexer;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */
public interface UserEntityESService extends AbstractESIndexer {

	void saveUserNode(User user, Session session);

	void addUserToOrganization(User user, long organizationId, Session session);

	void removeUserFromOrganization(User user, long organizationId);

	void updateCredentials(long orgId, long userId, Session session);

	void assignInstructorToUserInCredential(long orgId, long userId, long credId, long instructorId);

	void updateCredentialsWithInstructorRole(long orgId, long userId);
	
	void changeCredentialProgress(long orgId, long userId, long credId, int progress);
	
	void updateBasicUserData(User user, Session session);

	void updateFollowers(long orgId, long userId, Session session);

	void updateCompetences(long orgId, long userId, Session session);

	void updateCompetenceProgress(long orgId, long userId, TargetCompetence1 tComp);

	void updateRoles(long userId, Session session);

	void updateGroups(long orgId, long userId);

	void removeUserFromIndex(User user);

}
