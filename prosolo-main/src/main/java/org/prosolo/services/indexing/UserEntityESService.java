package org.prosolo.services.indexing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.elasticsearch.AbstractESIndexer;

import java.util.Optional;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */
public interface UserEntityESService extends AbstractESIndexer {

	void saveUserNode(User user, Session session);

	void addUserToOrganization(User user, long organizationId, Session session);

	void removeUserFromOrganization(User user, long organizationId);

	void updateCredentials(long orgId, long userId, Session session);

	void assignInstructorToUserInCredential(long orgId, long userId, long credId, long instructorId, Optional<CredentialAssessment> instructorAssessment);

	void updateCredentialsWithInstructorRole(long orgId, long userId);
	
	void changeCredentialProgress(long orgId, long userId, long credId, int progress);
	
	void updateBasicUserData(User user, Session session);

	void updateFollowers(long orgId, long userId, Session session);

	void updateFollowingUsers(long orgId, long userId, Session session);

	void updateCompetences(long orgId, long userId, Session session);

	void updateCompetenceProgress(long orgId, long userId, TargetCompetence1 tComp);

	void updateRoles(long userId, Session session);

	void updateGroups(long orgId, long userId, Session session);

	void removeUserFromIndex(User user);

	void updateCredentialAssessmentInfo(long orgId, CredentialAssessment assessment);

	void updateGroupsWithInstructorRole(long orgId, long userId);

}
